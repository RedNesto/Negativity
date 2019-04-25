package com.elikill58.negativity.universal.ban.processor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.elikill58.negativity.universal.ban.ActiveBan;
import com.elikill58.negativity.universal.ban.BanType;
import com.elikill58.negativity.universal.ban.LoggedBan;
import com.elikill58.negativity.universal.ban.storage.ActiveBanStorage;
import com.elikill58.negativity.universal.ban.storage.DatabaseActiveBanStorage;
import com.elikill58.negativity.universal.ban.storage.DatabaseLoggedBanStorage;
import com.elikill58.negativity.universal.ban.storage.FileActiveBanStorage;
import com.elikill58.negativity.universal.ban.storage.FileLoggedBanStorage;
import com.elikill58.negativity.universal.ban.storage.LoggedBanStorage;

/**
 * This ban processor simply saves bans (active and logged) in a configurable storage ({@link ActiveBanStorage} and {@link LoggedBanStorage} respectively).
 * <p>
 * It is important to know its sole purpose is to manage bans, and will not do anything on the game server,
 * like kicking the player when {@link #banPlayer(UUID, String, String, boolean, BanType, long, String) executing a ban}.
 * If you want direct actions on the game server use {@link NegativityBanProcessor} instead.
 */
public class BaseNegativityBanProcessor implements BanProcessor {

	private static final Map<String, LoggedBanStorage> LOG_STORAGES = new HashMap<>();
	private static final Map<String, ActiveBanStorage> BAN_STORAGES = new HashMap<>();

	private static String banStorageId = "file";
	private static String logStorageId = "file";

	private static boolean logBans = true;

	static {
		LOG_STORAGES.put("file", new FileLoggedBanStorage());
		LOG_STORAGES.put("database", new DatabaseLoggedBanStorage());

		BAN_STORAGES.put("file", new FileActiveBanStorage());
		BAN_STORAGES.put("database", new DatabaseActiveBanStorage());
	}

	@Nullable
	@Override
	public ActiveBan banPlayer(UUID playerId, String reason, String bannedBy, boolean isDefinitive, BanType banType, long expirationTime, @Nullable String cheatName) {
		ActiveBan ban = new ActiveBan(playerId, reason, bannedBy, isDefinitive, banType, expirationTime, cheatName);
		getBanStorage().save(ban);
		return ban;
	}

	@Nullable
	@Override
	public LoggedBan revokeBan(UUID playerId) {
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

	@Nullable
	@Override
	public ActiveBan getActiveBan(UUID playerId) {
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

	@Override
	public List<LoggedBan> getLoggedBans(UUID playerId) {
		return getLogStorage().load(playerId);
	}

	public static String getBanStorageId() {
		return banStorageId;
	}

	public static void setBanStorageId(String banStorage) {
		banStorageId = banStorage;
	}

	public static String getLogStorageId() {
		return logStorageId;
	}

	public static void setLogStorageId(String logStorage) {
		logStorageId = logStorage;
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
		BaseNegativityBanProcessor.logBans = logBans;
	}
}
