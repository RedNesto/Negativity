package com.elikill58.negativity.universal.ban;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.elikill58.negativity.universal.NegativityAccount;
import com.elikill58.negativity.universal.NegativityPlayer;
import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.ban.storage.ActiveBanStorage;
import com.elikill58.negativity.universal.ban.storage.DatabaseActiveBanStorage;
import com.elikill58.negativity.universal.ban.storage.DatabaseLoggedBanStorage;
import com.elikill58.negativity.universal.ban.storage.FileActiveBanStorage;
import com.elikill58.negativity.universal.ban.storage.FileLoggedBanStorage;
import com.elikill58.negativity.universal.ban.storage.LoggedBanStorage;
import com.elikill58.negativity.universal.permissions.Perm;

public class BanManager {

	private static final Map<String, LoggedBanStorage> LOG_STORAGES = new HashMap<>();
	private static final Map<String, ActiveBanStorage> BAN_STORAGES = new HashMap<>();

	static {
		LOG_STORAGES.put("file", new FileLoggedBanStorage());
		LOG_STORAGES.put("database", new DatabaseLoggedBanStorage());

		BAN_STORAGES.put("file", new FileActiveBanStorage());
		BAN_STORAGES.put("database", new DatabaseActiveBanStorage());
	}

	private static String banStorageId = "file";
	private static String logStorageId = "file";

	private static boolean logBans = true;

	public static List<LoggedBan> getLoggedBans(UUID playerId) {
		return getLogStorage().load(playerId);
	}

	@Nullable
	public static ActiveBan getActiveBan(UUID playerId) {
		if (!Ban.banActive)
			return null;

		ActiveBan activeBan = getBanStorage().load(playerId);
		if (activeBan == null) {
			return null;
		}

		long now = System.currentTimeMillis();
		if (activeBan.isDefinitive() || activeBan.getExpirationTime() > now) {
			return activeBan;
		}

		getBanStorage().remove(playerId);
		if (logBans) {
			getLogStorage().save(LoggedBan.from(activeBan, false));
		}

		return null;
	}

	/**
	 * Executes the given ban.
	 * <p>
	 * The ban may not be executed if bans are disabled or the player bypassed it via a permission node for example.
	 *
	 * @return the ban that has been executed, or {@code null} if the ban has not been executed.
	 */
	@Nullable
	public static ActiveBan banPlayer(UUID playerId, String reason, String bannedBy, boolean isDefinitive, BanType banType, long expirationTime, @Nullable String cheatName) {
		if (!Ban.banActive)
			return null;

		NegativityAccount nAccount = Adapter.getAdapter().getNegativityAccount(playerId);
		if (nAccount instanceof NegativityPlayer && Perm.hasPerm((NegativityPlayer) nAccount, "notBanned"))
			return null;

		ActiveBan ban = new ActiveBan(playerId, reason, bannedBy, isDefinitive, banType, expirationTime, cheatName);
		getBanStorage().save(ban);

		if (nAccount instanceof NegativityPlayer) {
			NegativityPlayer np = (NegativityPlayer) nAccount;
			np.banEffect();
			String formattedExpTime = new Timestamp(ban.getExpirationTime()).toString().split("\\.", 2)[0];
			np.kickPlayer(ban.getReason(), formattedExpTime, ban.getBannedBy(), ban.isDefinitive());
		}

		return ban;
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
	@Nullable
	public static LoggedBan revokeBan(UUID playerId) {
		if (!Ban.banActive)
			return null;

		ActiveBan activeBan = getBanStorage().load(playerId);
		if (activeBan == null)
			return null;

		getBanStorage().remove(playerId);
		LoggedBan revokedLoggedBan = LoggedBan.from(activeBan, true);

		if (logBans) {
			getLogStorage().save(revokedLoggedBan);
		}

		return revokedLoggedBan;
	}

	public static String getBanStorageId() {
		return banStorageId;
	}

	public static void setBanStorageId(String banStorage) {
		BanManager.banStorageId = banStorage;
	}

	public static String getLogStorageId() {
		return logStorageId;
	}

	public static void setLogStorageId(String logStorage) {
		BanManager.logStorageId = logStorage;
	}

	public static ActiveBanStorage getBanStorage() {
		return BAN_STORAGES.get(banStorageId);
	}

	public static LoggedBanStorage getLogStorage() {
		return LOG_STORAGES.get(logStorageId);
	}

	public static Collection<String> getAvailableBanStorageIds() {
		return BAN_STORAGES.keySet();
	}

	public static Collection<String> getAvailableLogStorageIds() {
		return LOG_STORAGES.keySet();
	}

	public static boolean isLogBans() {
		return logBans;
	}

	public static void setLogBans(boolean logBans) {
		BanManager.logBans = logBans;
	}
}
