package com.elikill58.negativity.universal.ban;

import java.util.UUID;

import javax.annotation.Nullable;

public class LoggedBan extends BaseBan {

	private final boolean isRevoked;

	public LoggedBan(UUID playerId, String reason, String bannedBy, boolean isDefinitive, BanType banType, long expirationTime, @Nullable String cheatName, boolean isRevoked) {
		super(playerId, reason, bannedBy, isDefinitive, banType, expirationTime, cheatName);
		this.isRevoked = isRevoked;
	}

	public boolean isRevoked() {
		return isRevoked;
	}

	public static LoggedBan from(BaseBan from, boolean isRevoked) {
		return new LoggedBan(from.getPlayerId(), from.getReason(), from.getBannedBy(), from.isDefinitive(), from.getBanType(), from.getExpirationTime(), from.getCheatName(), isRevoked);
	}
}