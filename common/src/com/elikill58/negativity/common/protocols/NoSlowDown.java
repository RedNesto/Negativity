package com.elikill58.negativity.common.protocols;

import static com.elikill58.negativity.universal.CheatKeys.NO_SLOW_DOWN;

import com.elikill58.negativity.api.GameMode;
import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.EventListener;
import com.elikill58.negativity.api.events.Listeners;
import com.elikill58.negativity.api.events.packets.PacketReceiveEvent;
import com.elikill58.negativity.api.events.player.PlayerItemConsumeEvent;
import com.elikill58.negativity.api.events.player.PlayerMoveEvent;
import com.elikill58.negativity.api.item.Enchantment;
import com.elikill58.negativity.api.item.ItemStack;
import com.elikill58.negativity.api.item.Materials;
import com.elikill58.negativity.api.location.Location;
import com.elikill58.negativity.api.potion.PotionEffectType;
import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.Negativity;
import com.elikill58.negativity.universal.PacketType;
import com.elikill58.negativity.universal.Version;
import com.elikill58.negativity.universal.report.ReportType;
import com.elikill58.negativity.universal.utils.UniversalUtils;

public class NoSlowDown extends Cheat implements Listeners {

	public NoSlowDown() {
		super(NO_SLOW_DOWN, CheatCategory.MOVEMENT, Materials.SOUL_SAND, false, false, "slowdown");
	}

	@EventListener
	public void onPlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if (!p.getGameMode().equals(GameMode.SURVIVAL) && !p.getGameMode().equals(GameMode.ADVENTURE))
			return;
		NegativityPlayer np = NegativityPlayer.getNegativityPlayer(p);
		if (!np.hasDetectionActive(this) || p.hasElytra())
			return;
		Location loc = p.getLocation();
		Location from = e.getFrom(), to = e.getTo();
		Location fl = from.clone().sub(to.clone());
		double xSpeed = Math.abs(from.getX() - to.getX());
	    double zSpeed = Math.abs(from.getZ() - to.getZ());
	    double xzSpeed = Math.sqrt(xSpeed * xSpeed + zSpeed * zSpeed);
	    double maxSpeed = (xSpeed >= zSpeed ? xSpeed : zSpeed);
	    if(maxSpeed < xzSpeed)
	    	maxSpeed = xzSpeed;
	    double distance = to.toVector().distance(from.toVector());
	    np.doubles.set(NO_SLOW_DOWN, "eating-distance", maxSpeed);
	    if(Version.getVersion().isNewerOrEquals(Version.V1_16)) {
		    ItemStack boots = p.getInventory().getBoots();
		    if(boots != null && boots.hasEnchant(Enchantment.SOUL_SPEED))
		    		return;
	    }
	    
	    boolean mayCancel = false;
	    if(checkActive("move") && loc.getBlock().getType().equals(Materials.SOUL_SAND) && !p.hasPotionEffect(PotionEffectType.SPEED)) {
			if (distance > 0.2) {
				int relia = UniversalUtils.parseInPorcent(distance * 400);
				if((from.getY() - to.getY()) < -0.001)
					return;
				mayCancel = Negativity.alertMod(ReportType.WARNING, p, this, relia, "move",
						"Soul sand under player. Distance from/to : " + distance + ".",
						hoverMsg("main", "%distance%", String.format("%.2f", distance)));
			}
	    }
	    if(checkActive("walk-speed")) {
			double dif = to.getY() - from.getY();
			if (dif == 0 && distance >= p.getWalkSpeed() && np.booleans.get(NO_SLOW_DOWN, "eating", false)) {
				mayCancel = Negativity.alertMod(distance >= (p.getWalkSpeed() * 1.5) ? ReportType.VIOLATION : ReportType.WARNING, p,
						this, UniversalUtils.parseInPorcent(distance * 350), "walk-speed", "Distance: " + distance + ", walkSpeed: " + p.getWalkSpeed());
			}
	    }
		if (isSetBack() && mayCancel)
			e.setTo(from.clone().add(fl.getX() / 2, (fl.getY() / 2) + 0.5, fl.getZ()));
	}

	@EventListener
	public void foodCheck(PlayerItemConsumeEvent e) {
		Player p = e.getPlayer();
		NegativityPlayer np = NegativityPlayer.getNegativityPlayer(p);
		if (!np.hasDetectionActive(this) || p.hasElytra())
			return;
	    if(!checkActive("eat"))
	    	return;
		if(p.isInsideVehicle())
			return;
		double dis = np.doubles.get(NO_SLOW_DOWN, "eating-distance", 0.0);
		if (dis > p.getWalkSpeed() || p.isSprinting()) {
			boolean mayCancel = Negativity.alertMod(ReportType.WARNING, p, this, UniversalUtils.parseInPorcent(dis * 200), "item",
					"Distance while eating: " + dis + ", WalkSpeed: " + p.getWalkSpeed(), hoverMsg("main", "%distance%", String.format("%.2f", dis)));
			if(isSetBack() && mayCancel)
				e.setCancelled(true);
		}
	}
	
	@EventListener
	public void onPacket(PacketReceiveEvent e) {
		Player p = e.getPlayer();
		NegativityPlayer np = NegativityPlayer.getNegativityPlayer(p);
		PacketType type = e.getPacket().getPacketType();
		if(type.equals(PacketType.Client.ARM_ANIMATION)) {
			ItemStack item = e.getPlayer().getItemInHand();
			if(item != null && item.getType().isConsumable())
				np.booleans.set(NO_SLOW_DOWN, "eating", true);
			
		} else if(type.equals(PacketType.Client.BLOCK_DIG) || type.equals(PacketType.Client.BLOCK_PLACE)) {
			np.booleans.remove(NO_SLOW_DOWN, "eating");
		}
	}
}
