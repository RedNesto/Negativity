package com.elikill58.negativity.velocity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.NegativityPlayer;
import com.velocitypowered.api.proxy.Player;

public class VelocityNegativityPlayer extends NegativityPlayer {

	private static final Map<UUID, VelocityNegativityPlayer> PLAYERS = new HashMap<>();

	public static NegativityPlayer getNegativityPlayer(UUID playerId) {
		return PLAYERS.computeIfAbsent(playerId, VelocityNegativityPlayer::new);
	}

	public VelocityNegativityPlayer(UUID playerId) {
		super(playerId);
	}

	@Override
	public Player getPlayer() {
		return VelocityNegativity.getInstance().getProxyServer().getPlayer(getUUID()).get();
	}

	@Override
	public boolean hasDefaultPermission(String s) {
		return getPlayer().hasPermission(s);
	}

	@Override
	public int getWarn(Cheat c) {
		return -1;
	}

	@Override
	public int getAllWarn(Cheat c) {
		return -1;
	}

	@Override
	public double getLife() {
		return -1;
	}

	@Override
	public String getName() {
		return getPlayer().getUsername();
	}

	@Override
	public String getGameMode() {
		return "unknown";
	}

	@Override
	public float getWalkSpeed() {
		return -1;
	}

	@Override
	public int getLevel() {
		return -1;
	}

	@Override
	public void kickPlayer(String reason, String time, String by, boolean def) {
		String msgKey = def ? "ban.kick_def" : "ban.kick_time";
		getPlayer().disconnect(VelocityMessages.getMessage(getPlayer(), msgKey, "%reason%", reason, "%time%", String.valueOf(time), "%by%", by));
	}

	@Override
	public void banEffect() {
	}

	@Override
	public void startAnalyze(Cheat c) {
	}

	@Override
	public void startAllAnalyze() {
	}

	@Override
	public void updateMinerateInFile() {
	}

	@Override
	public boolean isOp() {
		return false;
	}

	@Override
	public String getIP() {
		return getPlayer().getRemoteAddress().getHostName();
	}

	@Override
	public String getReason(Cheat c) {
		return null;
	}
}
