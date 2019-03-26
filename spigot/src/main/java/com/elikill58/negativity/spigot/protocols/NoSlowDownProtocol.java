package com.elikill58.negativity.spigot.protocols;

import com.elikill58.negativity.spigot.SpigotNegativity;
import com.elikill58.negativity.spigot.SpigotNegativityPlayer;
import com.elikill58.negativity.spigot.utils.Cheat;
import com.elikill58.negativity.spigot.utils.ReportType;
import com.elikill58.negativity.spigot.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NoSlowDownProtocol implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if (!p.getGameMode().equals(GameMode.SURVIVAL) && !p.getGameMode().equals(GameMode.ADVENTURE))
			return;
		SpigotNegativityPlayer np = SpigotNegativityPlayer.getNegativityPlayer(p);
		if (!np.ACTIVE_CHEAT.contains(Cheat.NOSLOWDOWN))
			return;
		Location loc = p.getLocation();
		if (!loc.getBlock().getType().equals(Material.SOUL_SAND))
			return;
		for (PotionEffect pe : p.getActivePotionEffects())
			if (pe.getType().equals(PotionEffectType.SPEED) && pe.getAmplifier() > 1)
				return;
		Location from = e.getFrom(), to = e.getTo();
		Location fl = from.clone().subtract(to.clone());
		double distance = to.toVector().distance(from.toVector());
		if (distance > 0.2) {
			int ping = Utils.getPing(p), relia = Utils.parseInPorcent(distance * 400);
			if((from.getY() - to.getY()) < -0.001)
				return;
			np.addWarn(Cheat.NOSLOWDOWN);
			boolean mayCancel = SpigotNegativity.alertMod(ReportType.WARNING, p, Cheat.NOSLOWDOWN, relia,
					"Soul sand under player. Distance from/to : " + distance + ". Ping: " + ping);
			if (Cheat.NOSLOWDOWN.isSetBack() && mayCancel)
				e.setTo(from.clone().add(new Location(fl.getWorld(), fl.getX() / 2, fl.getY() / 2, fl.getZ())).add(0, 0.5, 0));
		}
	}
}
