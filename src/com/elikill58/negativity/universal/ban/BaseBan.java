package com.elikill58.negativity.universal.ban;

import java.util.UUID;

import javax.annotation.Nullable;

// TODO rename to Ban when we get rid of the existing Ban helper class
public class BaseBan {

	private final UUID playerId;
	private final String reason;
	private final String bannedBy;
	private final boolean isDefinitive;
	private final BanType banType;
	private final long expirationTime;
	@Nullable
	private final String cheatName;

	public BaseBan(UUID playerId, String reason, String bannedBy, boolean isDefinitive, BanType banType, long expirationTime, @Nullable String cheatName) {
		this.playerId = playerId;
		this.reason = reason;
		this.bannedBy = bannedBy;
		this.isDefinitive = isDefinitive;
		this.banType = banType;
		this.expirationTime = expirationTime;
		this.cheatName = cheatName;
	}

	public UUID getPlayerId() {
		return playerId;
	}

	public String getReason() {
		return reason;
	}

	public String getBannedBy() {
		return bannedBy;
	}

	public boolean isDefinitive() {
		return isDefinitive;
	}

	public BanType getBanType() {
		return banType;
	}

	public long getExpirationTime() {
		return expirationTime;
	}

	@Nullable
	public String getCheatName() {
		return cheatName;
	}
}
