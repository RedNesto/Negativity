package com.elikill58.negativity.sponge.protocols;

import java.util.Collection;
import java.util.Optional;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.elikill58.negativity.sponge.SpongeNegativity;
import com.elikill58.negativity.sponge.SpongeNegativityPlayer;
import com.elikill58.negativity.sponge.utils.Utils;
import com.elikill58.negativity.universal.*;
import com.flowpowered.math.vector.Vector3i;

public class JesusProtocol extends Cheat {

	public JesusProtocol() {
		super("JESUS", false, ItemTypes.WATER_BUCKET, false, true, "waterwalk", "water");
	}

	@Listener
	public void onPlayerMove(MoveEntityEvent e, @First Player p) {
		if (!p.gameMode().get().equals(GameModes.SURVIVAL) && !p.gameMode().get().equals(GameModes.ADVENTURE)) {
			return;
		}

		if (p.get(Keys.IS_FLYING).orElse(false) || p.get(Keys.IS_ELYTRA_FLYING).orElse(false)) {
			return;
		}

		SpongeNegativityPlayer np = SpongeNegativityPlayer.getNegativityPlayer(p);
		if (!np.hasDetectionActive(this)) {
			return;
		}

		Location<World> loc = p.getLocation();
		BlockType m = loc.getBlockType();
		BlockType under = loc.sub(Vector3i.UNIT_Y).getBlockType();
		boolean isInWater = m.equals(BlockTypes.WATER);
		boolean isOnWater = under.equals(BlockTypes.WATER);
		Optional<EntitySnapshot> vehicle = p.get(Keys.VEHICLE);
		if (vehicle.isPresent() && vehicle.get().getType() == EntityTypes.BOAT) {
			return;
		}

		if (isOnWater) {
			// We count how many events the player in on top of water because
			// it is possible to trigger an alert when falling straight into
			// water or jumping with a good angle and timing from a border at
			// the same height than the water.
			np.movementsOnWater++;
			// 1 is enough from my tests, but we can increment it if
			// it is too sensitive
			if (np.movementsOnWater <= 1) {
				return;
			}
		} else {
			np.movementsOnWater = 0;
		}

		if (!isInWater && isOnWater && !hasBoatAroundHim(loc) && !np.hasOtherThan(loc.sub(0, 1, 0), BlockTypes.WATER)
				&& !p.getLocation().getBlockType().equals(BlockTypes.WATERLILY)) {
			if (hasWaterLily(loc.sub(0, 1, 0))) {
				return;
			}

			double reliability = 0;

			double dif = e.getFromTransform().getPosition().getY() - e.getToTransform().getPosition().getY();
			ReportType type = ReportType.VIOLATION;
			if (dif < 0.0005 && dif > 0.00000005)
				reliability = dif * 10000000 - 1;
			else if (dif < 0.1 && dif > 0.08)
				reliability = dif * 1000;
			else if (dif == 0.5) {
				reliability = 50;
				type = ReportType.WARNING;
			} else if (dif < 0.30001 && dif > 0.3000)
				reliability = dif * 100 * 2.5;
			else if (dif < 0.002 && dif > -0.002 && dif != 0.0)
				reliability = Math.abs(dif * 5000);
			else if (dif == 0.0)
				reliability = 95;
			else if (dif == p.get(Keys.WALKING_SPEED).get())
				reliability = 90;
			else return;
			boolean mayCancel = SpongeNegativity.alertMod(type, p, this, Utils.parseInPorcent(reliability),
					"Warn for Jesus: " + np.getWarn(this) + " (Stationary_water aroud him) WalkSpeed: " + p.get(Keys.WALKING_SPEED).get() + ". Diff: " + dif + " and ping: " + Utils.getPing(p));
			if (isSetBack() && mayCancel) {
				p.setLocation(p.getLocation().sub(Vector3i.UNIT_Y));
			}
		}
	}

	private boolean hasWaterLily(Location<?> loc) {
		int fX = loc.getBlockX(), fY = loc.getBlockY(), fZ = loc.getBlockZ();
		for (int y = (fY - 1); y != (fY + 2); y++) {
			for (int x = (fX - 2); x != (fX + 3); x++) {
				for (int z = (fZ - 2); z != (fZ + 3); z++) {
					if (loc.getExtent().getBlockType(x, y, z) == BlockTypes.WATERLILY)
						return true;
				}
			}
		}
		return false;
	}

	private boolean hasBoatAroundHim(Location<World> loc) {
		Collection<Entity> nearbyEntities = loc.getExtent().getNearbyEntities(loc.getPosition(), 3);
		for (Entity entity : nearbyEntities) {
			if (entity instanceof Boat) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getHoverFor(NegativityPlayer p) {
		return "";
	}
}
