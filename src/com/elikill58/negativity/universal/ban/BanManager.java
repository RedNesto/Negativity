package com.elikill58.negativity.universal.ban;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.elikill58.negativity.universal.Database;
import com.elikill58.negativity.universal.NegativityPlayer;
import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.ban.storage.DatabaseLoggedBanStorage;
import com.elikill58.negativity.universal.ban.storage.FileLoggedBanStorage;
import com.elikill58.negativity.universal.ban.storage.LoggedBanStorage;
import com.elikill58.negativity.universal.permissions.Perm;

public class BanManager {

	private static final LoggedBanStorage FILE_STORAGE = new FileLoggedBanStorage();
	private static final LoggedBanStorage DB_STORAGE = new DatabaseLoggedBanStorage();

	public static List<LoggedBan> getLoggedBans(UUID playerId) {
		if (Ban.banActiveIsFile) {
			return FILE_STORAGE.load(playerId);
		} else {
			return DB_STORAGE.load(playerId);
		}
	}

	private static void saveLoggedBans(Collection<LoggedBan> bans) {
		Set<UUID> tracker = new HashSet<>();
		for (LoggedBan ban : bans) {
			if (tracker.add(ban.getPlayerId())) {
				// Delete existing file to ensure we have no duplicated entries
				File bansFile = new File(Ban.banDir, ban.getPlayerId() + ".txt");
				bansFile.delete();
			}

			saveLoggedBan(ban);
		}
	}

	private static void saveLoggedBan(LoggedBan ban) {
		if (Ban.banActiveIsFile) {
			FILE_STORAGE.save(ban);
		} else {
			DB_STORAGE.save(ban);
		}
	}

	@Nullable
	public static ActiveBan getActiveBan(UUID playerId) {
		if (!Ban.banActive)
			return null;

		// TODO for now we save active bans with the logged ones, but once we separate them
		//  we will no longer need to use this method and will be able to get the active ban directly.
		LoggedBan loggedActive = getActiveBanFromLoggedBans(getLoggedBans(playerId));
		if (loggedActive == null)
			return null;

		return ActiveBan.from(loggedActive);
	}

	@Nullable
	private static LoggedBan getActiveBanFromLoggedBans(List<LoggedBan> loggedBans) {
		if (loggedBans.isEmpty())
			return null;

		final long now = System.currentTimeMillis();
		for (LoggedBan loggedBan : loggedBans) {
			if (!loggedBan.isRevoked() && (loggedBan.isDefinitive() || loggedBan.getExpirationTime() > now)) {
				return loggedBan;
			}
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

		NegativityPlayer nPlayer = Adapter.getAdapter().getNegativityPlayer(playerId);
		if (nPlayer != null && Perm.hasPerm(nPlayer, "notBanned"))
			return null;

		ActiveBan ban = new ActiveBan(playerId, reason, bannedBy, isDefinitive, banType, expirationTime, cheatName);

		// TODO for now we save active bans with logged ones
		saveLoggedBan(LoggedBan.from(ban, false));

		if (nPlayer != null) {
			nPlayer.banEffect();
			String formattedExpTime = new Timestamp(ban.getExpirationTime()).toString().split("\\.", 2)[0];
			nPlayer.kickPlayer(ban.getReason(), formattedExpTime, ban.getBannedBy(), ban.isDefinitive());
		}

		return ban;
	}

	/**
	 * Revokes the active ban of the player identified by the given UUID.
	 * <p>
	 * The revocation may fail if the player is not banned or bans are disabled.
	 *
	 * @param playerId the UUID of the player to unban
	 *
	 * @return the logged revoked ban or {@code null} if the revocation failed.
	 */
	@Nullable
	public static LoggedBan revokeBan(UUID playerId) {
		if (!Ban.banActive)
			return null;

		List<LoggedBan> loggedBans = getLoggedBans(playerId);
		// TODO same as TODO comment in #getActiveBan
		LoggedBan activeLoggedBan = getActiveBanFromLoggedBans(loggedBans);
		if (activeLoggedBan == null)
			return null;

		LoggedBan revokedLoggedBan = LoggedBan.from(activeLoggedBan, true);

		try {
			Adapter ada = Adapter.getAdapter();
			if (ada.getBooleanInConfig("ban.destroy_when_unban")) {
				if (Ban.banActiveIsFile) {
					File f = new File(Ban.banDir, playerId + ".txt");
					if (f.exists() && !f.delete())
						Files.write(f.toPath(), Collections.emptyList());
				} else {
					PreparedStatement stm = Database.getConnection()
							.prepareStatement("DELETE FROM " + Database.table_ban + " WHERE uuid = ?");
					stm.setString(1, playerId.toString());
					stm.execute();
				}
			} else {
				if (Ban.banActiveIsFile) {
					// TODO We need to replace the active LoggedBan by the revoked one since LoggedBan is immutable,
					//  but we have to set LoggedBan#isRevoked to true.
					//  This will no longer be needed once we separate logged and active bans storage.
					loggedBans.remove(activeLoggedBan);
					loggedBans.add(revokedLoggedBan);

					saveLoggedBans(loggedBans);
				} else {
					String uc = ada.getStringInConfig("ban.db.column.uuid");
					PreparedStatement stm = Database.getConnection().prepareStatement("UPDATE " + Database.table_ban
							+ " SET " + ada.getStringInConfig("ban.db.column.time") + " = ? WHERE " + uc + " = ?");
					stm.setInt(1, 0);
					stm.setString(2, playerId.toString());
					stm.execute();
					PreparedStatement stm2 = Database.getConnection().prepareStatement("UPDATE " + Database.table_ban
							+ " SET " + ada.getStringInConfig("ban.db.column.def") + " = ? WHERE " + uc + " = ?");
					stm2.setBoolean(1, false);
					stm2.setString(2, playerId.toString());
					stm2.execute();
				}
			}
		} catch (NoSuchFileException e) {
			// already deleted
		} catch (Exception e) {
			e.printStackTrace();
		}

		return revokedLoggedBan;
	}
}
