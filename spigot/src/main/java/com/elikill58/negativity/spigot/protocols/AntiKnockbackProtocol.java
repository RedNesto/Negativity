package com.elikill58.negativity.spigot.protocols;

import com.elikill58.negativity.spigot.SpigotNegativity;
import com.elikill58.negativity.spigot.SpigotNegativityPlayer;
import com.elikill58.negativity.spigot.utils.Cheat;
import com.elikill58.negativity.spigot.utils.ReportType;
import com.elikill58.negativity.spigot.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@SuppressWarnings("deprecation")
public class AntiKnockbackProtocol implements Listener {

    @EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;
		Player p = (Player) e.getEntity();
		SpigotNegativityPlayer np = SpigotNegativityPlayer.getNegativityPlayer(p);
		if (!np.ACTIVE_CHEAT.contains(Cheat.ANTIKNOCKBACK))
			return;
		if (!p.getGameMode().equals(GameMode.SURVIVAL) && !p.getGameMode().equals(GameMode.ADVENTURE))
			return;
		if(e.getDamager().getType().equals(EntityType.EGG))
			return;
		Bukkit.getScheduler().runTaskLater(SpigotNegativity.getInstance(), new BukkitRunnable() {

			@Override
			public void run() {
				final Location last = p.getLocation();
				p.damage(0D);
				p.setLastDamageCause(new EntityDamageEvent(p, DamageCause.CUSTOM, 0D));
				Bukkit.getScheduler().runTaskLater(SpigotNegativity.getInstance(), new BukkitRunnable() {
					@Override
					public void run() {
						Location actual = p.getLocation();
						double d = last.distance(actual);
						int ping = Utils.getPing(p), relia = Utils.parseInPorcent(100 - d);
						if (d < 0.1 && !actual.getBlock().getType().equals(Utils.getMaterialWith1_13_Compatibility("WEB", "COBWEB")) && !p.isSneaking()){
							np.addWarn(Cheat.ANTIKNOCKBACK);
							boolean mayCancel = SpigotNegativity.alertMod(ReportType.WARNING, p, Cheat.ANTIKNOCKBACK, relia,
									"Distance after damage: " + d + "; Damager: " + e.getDamager().getType().name().toLowerCase() + " Ping: " + ping, "Distance after damage: " + d);
							if(Cheat.ANTIKNOCKBACK.isSetBack() && mayCancel)
								p.setVelocity(p.getVelocity().add(new Vector(0, 1, 0)));
						}
					}
				}, 5);
			}
		}, 0);
	}
}
