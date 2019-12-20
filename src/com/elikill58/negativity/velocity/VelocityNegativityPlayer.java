package com.elikill58.negativity.velocity;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.NegativityPlayer;
import com.velocitypowered.api.proxy.Player;

public class VelocityNegativityPlayer extends NegativityPlayer {

	private static final Map<UUID, VelocityNegativityPlayer> PLAYERS = new HashMap<>();
	private WeakReference<Player> player;

	public static NegativityPlayer getNegativityPlayer(UUID playerId) {
		return PLAYERS.computeIfAbsent(playerId, VelocityNegativityPlayer::new);
	}

	public static NegativityPlayer getNegativityPlayer(Player player) {
		return PLAYERS.computeIfAbsent(player.getUniqueId(), id -> new VelocityNegativityPlayer(player));
	}

	public VelocityNegativityPlayer(UUID playerId) {
		super(playerId);
		this.player = new WeakReference<>(null);
	}

	public VelocityNegativityPlayer(Player player) {
		super(player.getUniqueId());
		this.player = new WeakReference<>(player);
	}

	@Override
	public Player getPlayer() {
		Player playerRef = this.player.get();
		if (playerRef != null) {
			return playerRef;
		}
		Player player = VelocityNegativity.getInstance().getProxyServer().getPlayer(getUUID()).get();
		this.player = new WeakReference<>(player);
		return player;
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
