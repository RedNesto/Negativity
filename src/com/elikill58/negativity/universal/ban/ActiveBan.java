package com.elikill58.negativity.universal.ban;

import java.util.UUID;

import javax.annotation.Nullable;

public class ActiveBan extends BaseBan {

	public ActiveBan(UUID playerId, String reason, String bannedBy, boolean isDefinitive, BanType banType, long expirationTime, @Nullable String cheatName) {
		super(playerId, reason, bannedBy, isDefinitive, banType, expirationTime, cheatName);
	}

	public static ActiveBan from(BaseBan from) {
		return new ActiveBan(from.getPlayerId(), from.getReason(), from.getBannedBy(), from.isDefinitive(), from.getBanType(), from.getExpirationTime(), from.getCheatName());
	}
}