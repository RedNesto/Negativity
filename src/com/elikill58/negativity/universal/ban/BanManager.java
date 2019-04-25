package com.elikill58.negativity.universal.ban;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.elikill58.negativity.universal.ban.processor.BanProcessor;
import com.elikill58.negativity.universal.ban.processor.NegativityBanProcessor;

public class BanManager {

	private static BanProcessor banProcessor = new NegativityBanProcessor();

	public static List<LoggedBan> getLoggedBans(UUID playerId) {
		if (!Ban.banActive)
			return Collections.emptyList();

		return banProcessor.getLoggedBans(playerId);
	}

	public static boolean isBanned(UUID playerId) {
		if (!Ban.banActive)
			return false;

		return banProcessor.isBanned(playerId);
	}

	@Nullable
	public static ActiveBan getActiveBan(UUID playerId) {
		if (!Ban.banActive)
			return null;

		return banProcessor.getActiveBan(playerId);
	}

	/**
	 * Executes the given ban. The executed ban may contain different information than the one you provided.
	 * Therefore, it is advised to use the returned {@link ActiveBan} data instead of what you gave in this method parameters.
	 * <p>
	 * The ban may not be executed if bans are disabled, or for any {@link BanProcessor}-specific reason, like if the player bypassed the ban.
	 *
	 * @return the ban that has been executed, or {@code null} if the ban has not been executed.
	 */
	@Nullable
	public static ActiveBan banPlayer(UUID playerId, String reason, String bannedBy, boolean isDefinitive, BanType banType, long expirationTime, @Nullable String cheatName) {
		if (!Ban.banActive)
			return null;

		return banProcessor.banPlayer(playerId, reason, bannedBy, isDefinitive, banType, expirationTime, cheatName);
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

		return banProcessor.revokeBan(playerId);
	}
}
