package com.elikill58.negativity.universal.ban;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.ban.processor.BanProcessor;
import com.elikill58.negativity.universal.ban.processor.CommandBanProcessor;
import com.elikill58.negativity.universal.ban.processor.NegativityBanProcessor;
import com.elikill58.negativity.universal.ban.storage.DatabaseActiveBanStorage;
import com.elikill58.negativity.universal.ban.storage.DatabaseBanLogsStorage;
import com.elikill58.negativity.universal.ban.storage.FileActiveBanStorage;
import com.elikill58.negativity.universal.ban.storage.FileBanLogsStorage;

public class BanManager {

	public static boolean banActive;

	private static String processorId;
	private static final Map<String, BanProcessor> processors = new HashMap<>();

	public static CompletableFuture<List<LoggedBan>> getLoggedBans(UUID playerId) {
		BanProcessor processor = getProcessor();
		if (processor == null) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}

		return processor.getLoggedBans(playerId);
	}

	public static CompletableFuture<Boolean> isBanned(UUID playerId) {
		BanProcessor processor = getProcessor();
		if (processor == null) {
			return CompletableFuture.completedFuture(false);
		}

		return processor.isBanned(playerId);
	}

	public static CompletableFuture<@Nullable ActiveBan> getActiveBan(UUID playerId) {
		BanProcessor processor = getProcessor();
		if (processor == null) {
			return CompletableFuture.completedFuture(null);
		}

		return processor.getActiveBan(playerId);
	}

	/**
	 * Executes the given ban. The executed ban may contain different information than the one you provided.
	 * Therefore, it is advised to use the returned {@link ActiveBan} data instead of what you gave in this method parameters.
	 * <p>
	 * The ban may not be executed if bans are disabled, or for any {@link BanProcessor}-specific reason, like if the player bypassed the ban.
	 *
	 * @return the ban that has been executed, or {@code null} if the ban has not been executed.
	 */
	public static CompletableFuture<@Nullable ActiveBan> executeBan(ActiveBan ban) {
		BanProcessor processor = getProcessor();
		if (processor == null) {
			return CompletableFuture.completedFuture(null);
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
	public static CompletableFuture<@Nullable LoggedBan> revokeBan(UUID playerId) {
		BanProcessor processor = getProcessor();
		if (processor == null) {
			return CompletableFuture.completedFuture(null);
		}

		return processor.revokeBan(playerId);
	}

	public static String getProcessorId() {
		return processorId;
	}

	@Nullable
	public static BanProcessor getProcessor() {
		if (!banActive) {
			return null;
		}

		return processors.get(processorId);
	}

	public static void registerProcessor(String id, BanProcessor processor) {
		processors.put(id, processor);
	}

	public static void init() {
		Adapter adapter = Adapter.getAdapter();
		banActive = adapter.getBooleanInConfig("ban.active");
		if (!banActive) {
			return;
		}

		processorId = adapter.getStringInConfig("ban.processor");

		Path dataDir = adapter.getDataFolder().toPath();
		Path banDir = dataDir.resolve("bans");
		Path banLogsDir = banDir.resolve("logs");
		boolean fileLogBans = adapter.getBooleanInConfig("ban.file.log_bans");
		registerProcessor("file", new NegativityBanProcessor(new FileActiveBanStorage(banDir), fileLogBans ? new FileBanLogsStorage(banLogsDir) : null));

		boolean dbLogBans = adapter.getBooleanInConfig("ban.database.log_bans");
		registerProcessor("database", new NegativityBanProcessor(new DatabaseActiveBanStorage(), dbLogBans ? new DatabaseBanLogsStorage() : null));

		List<String> banCommands = adapter.getStringListInConfig("ban.command.ban");
		List<String> unbanCommands = adapter.getStringListInConfig("ban.command.unban");
		registerProcessor("command", new CommandBanProcessor(banCommands, unbanCommands));

		BansMigration.migrateBans(banDir, banLogsDir);
	}
}
