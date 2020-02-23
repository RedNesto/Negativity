package com.elikill58.negativity.spigot.protocols;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

import com.elikill58.negativity.spigot.SpigotNegativity;
import com.elikill58.negativity.spigot.SpigotNegativityPlayer;
import com.elikill58.negativity.spigot.utils.Utils;
import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.CheatKeys;
import com.elikill58.negativity.universal.NegativityPlayer;
import com.elikill58.negativity.universal.ReportType;
import com.elikill58.negativity.universal.adapter.Adapter;

public class NoFallProtocol extends Cheat implements Listener {
	
	public NoFallProtocol() {
		super(CheatKeys.NO_FALL, false, Utils.getMaterialWith1_15_Compatibility("WOOL", "RED_WOOL"), false, true, "fall");
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		SpigotNegativityPlayer np = SpigotNegativityPlayer.getNegativityPlayer(p);
		if (!np.ACTIVE_CHEAT.contains(this) || e.isCancelled())
			return;
		if (!p.getGameMode().equals(GameMode.SURVIVAL) && !p.getGameMode().equals(GameMode.ADVENTURE))
			return;
		if(p.getAllowFlight() || Utils.hasElytra(p))
			return;
		Location from = e.getFrom(), to = e.getTo();
		double distance = to.toVector().distance(from.toVector());
		if (!(p.getVehicle() != null || distance == 0.0D || from.getY() < to.getY()))
			if (p.getFallDistance() == 0.0F && !p.hasPotionEffect(PotionEffectType.SPEED)
					&& p.getLocation().clone().subtract(0, 1, 0).getBlock().getType().equals(Material.AIR)) {
				int relia = Utils.parseInPorcent(distance * 100);
				if (p.isOnGround()) {
					if (distance > 0.79D) {
						boolean mayCancel = SpigotNegativity.alertMod(ReportType.VIOLATION, p, this, relia,
								"Player in ground. FallDamage: " + p.getFallDistance() + ", DistanceBetweenFromAndTo: "
										+ distance + " (ping: " + Utils.getPing(p) + "). Warn: "
										+ np.getWarn(this), "");
						if(mayCancel)
							np.NO_FALL_DAMAGE += 1;
					} else if (np.NO_FALL_DAMAGE != 0) {
						if (isSetBack())
							manageDamage(p, np.NO_FALL_DAMAGE, relia);
						np.NO_FALL_DAMAGE = 0;
					}
				} else {
					if (distance > 2D) {
						boolean mayCancel = SpigotNegativity.alertMod(ReportType.VIOLATION, p, this, relia,
								"Player not in ground no fall Damage. FallDistance: " + p.getFallDistance()
										+ ", DistanceBetweenFromAndTo: " + distance + " (ping: " + Utils.getPing(p)
										+ "). Warn: " + np.getWarn(this), "");
						if(mayCancel)
							np.NO_FALL_DAMAGE += 1;
					} else if (np.NO_FALL_DAMAGE != 0) {
						if (isSetBack())
							manageDamage(p, np.NO_FALL_DAMAGE, relia);
						np.NO_FALL_DAMAGE = 0;
					}
				}
			}
	}
	
	private void manageDamage(Player p, int damage, int relia) {
		Adapter ada = Adapter.getAdapter();
		p.damage(damage >= p.getHealth() ? (ada.getBooleanInConfig("cheats.nofall.kill") && ada.getDoubleInConfig("cheats.nofall.kill-reliability") >= relia ? damage : p.getHealth() - 0.5) : p.getHealth());
	}
	
	@Override
	public String getHoverFor(NegativityPlayer p) {
		return "";
	}
}
