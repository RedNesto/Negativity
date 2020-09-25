package com.elikill58.negativity.universal.ban;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.elikill58.negativity.api.yaml.config.Configuration;
import com.elikill58.negativity.universal.Database;
import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.ban.BanResult.BanResultType;
import com.elikill58.negativity.universal.ban.processor.BanProcessor;
import com.elikill58.negativity.universal.ban.processor.CommandBanProcessor;
import com.elikill58.negativity.universal.ban.processor.NegativityBanProcessor;
import com.elikill58.negativity.universal.ban.storage.DatabaseActiveBanStorage;
import com.elikill58.negativity.universal.ban.storage.DatabaseBanLogsStorage;
import com.elikill58.negativity.universal.ban.storage.FileActiveBanStorage;
import com.elikill58.negativity.universal.ban.storage.FileBanLogsStorage;
import com.elikill58.negativity.universal.utils.UniversalUtils;

public class BanManager {

	public static boolean banActive;
	private static Configuration banConfig;

	private static String processorId;
	private static Map<String, BanProcessor> processors = new HashMap<>();

	public static List<Ban> getLoggedBans(UUID playerId) {
		BanProcessor processor = getProcessor();
		if (processor == null)
			return Collections.emptyList();

		return processor.getLoggedBans(playerId);
	}

	public static boolean isBanned(UUID playerId) {
		BanProcessor processor = getProcessor();
		if (processor == null)
			return false;

		return processor.isBanned(playerId);
	}

	@Nullable
	public static Ban getActiveBan(UUID playerId) {
		BanProcessor processor = getProcessor();
		if (processor == null) {
			Adapter.getAdapter().debug("Cannot find ban processor while trying to get active ban from " + playerId.toString());
			return null;
		}

		return processor.getActiveBan(playerId);
	}

	/**
	 * Executes the given ban. The executed ban may contain different information than the one you provided.
	 * Therefore, it is advised to use the returned {@link ActiveBan} data instead of what you gave in this method parameters.
	 * <p>
	 * The ban may not be executed if bans are disabled, or for any {@link BanProcessor}-specific reason, like if the player bypassed the ban.
	 *
	 * @return the result of the ban with success informations
	 */
	public static BanResult executeBan(Ban ban) {
		BanProcessor processor = getProcessor();
		if (processor == null) {
			Adapter.getAdapter().debug("Cannot find ban processor while trying to execute ban from " + ban.getPlayerId().toString());
			return new BanResult(BanResultType.UNKNOW_PROCESSOR, null);
		}

		return processor.executeBan(ban);
	}

	/**
	 * Revokes the active ban of the player identified by the given UUID.
	 * <p>
	 * The revocation may fail if the player is not banned or bans are disabled.
	 * <p>
	 * If ban logging is disabled, a LoggedBan will still be returned even though it will not be saved.
	 *
	 * @param playerId the UUID of the player to unban
	 *
	 * @return the logged revoked ban or {@code null} if the revocation failed.
	 */
	public static BanResult revokeBan(UUID playerId) {
		BanProcessor processor = getProcessor();
		if (processor == null) {
			Adapter.getAdapter().debug("Cannot find ban processor while trying to revoke ban from " + playerId.toString());
			return null;
		}

		return processor.revokeBan(playerId);
	}

	public static String getProcessorId() {
		return processorId;
	}

	@Nullable
	public static BanProcessor getProcessor() {
		if (!banActive)
			return null;

		return processors.get(processorId);
	}

	public static void registerProcessor(String id, BanProcessor processor) {
		processors.put(id, processor);
	}

	public static void init() {
		processors.clear();
		
		Adapter adapter = Adapter.getAdapter();
		
		banConfig = UniversalUtils.loadConfig(new File(adapter.getDataFolder(), "bans.yml"), "bans.yml");
		
		banActive = banConfig.getBoolean("active");
		if (!banActive)
			return;

		processorId = banConfig.getString("processor");

		Path dataDir = adapter.getDataFolder().toPath();
		Path banDir = dataDir.resolve("bans");
		Path banLogsDir = banDir.resolve("logs");
		boolean fileLogBans = banConfig.getBoolean("file.log_bans");
		registerProcessor("file", new NegativityBanProcessor(new FileActiveBanStorage(banDir), fileLogBans ? new FileBanLogsStorage(banLogsDir) : null));

		if (Database.hasCustom) {
			boolean dbLogBans = banConfig.getBoolean("database.log_bans");
			registerProcessor("database", new NegativityBanProcessor(new DatabaseActiveBanStorage(), dbLogBans ? new DatabaseBanLogsStorage() : null));
		}

		List<String> banCommands = banConfig.getStringList("command.ban");
		List<String> unbanCommands = banConfig.getStringList("command.unban");
		registerProcessor("command", new CommandBanProcessor(banCommands, unbanCommands));

		BansMigration.migrateBans(banDir, banLogsDir);
	}
	
	public static Configuration getBanConfig() {
		return banConfig;
	}
}
