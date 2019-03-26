package com.elikill58.negativity.spigot.protocols;

import com.elikill58.negativity.spigot.SpigotNegativityPlayer;
import com.elikill58.negativity.spigot.SpigotNegativityPlayer.FlyingReason;
import com.elikill58.negativity.spigot.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AntiPotionProtocol implements Listener {

	@EventHandler (ignoreCancelled = true)
	public void onRegen(EntityRegainHealthEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			SpigotNegativityPlayer np = SpigotNegativityPlayer.getNegativityPlayer(p);
			boolean hasPotion = false;
			for (PotionEffect pe : p.getActivePotionEffects())
				if (pe.getType().equals(PotionEffectType.POISON) || pe.getType().equals(PotionEffectType.BLINDNESS)
						|| pe.getType().equals(PotionEffectType.WITHER)
						|| pe.getType().equals(PotionEffectType.SLOW_DIGGING)
						|| pe.getType().equals(PotionEffectType.WEAKNESS)
						|| pe.getType().equals(PotionEffectType.CONFUSION)
						|| pe.getType().equals(PotionEffectType.HUNGER)){
					hasPotion = true;
					np.POTION_EFFECTS.add(pe);
				}
			if (hasPotion)
				np.flyingReason = FlyingReason.POTION;
			else
				np.flyingReason = FlyingReason.REGEN;
		}
	}

    @EventHandler (ignoreCancelled = true)
	public void onProjectileHit(ProjectileHitEvent e) {
		if(!e.getEntity().getType().equals(EntityType.SPLASH_POTION))
			return;
		Location loc = e.getEntity().getLocation();
		for(Player p : Utils.getOnlinePlayers()){
			if(loc.getWorld().equals(p.getLocation().getWorld()))
				if(loc.distance(p.getLocation()) < 9)
					SpigotNegativityPlayer.getNegativityPlayer(p).flyingReason = FlyingReason.POTION;
		}
	}
}
