package com.elikill58.negativity.sponge.protocols;

import com.elikill58.negativity.sponge.NeedListener;
import com.elikill58.negativity.sponge.SpongeNegativity;
import com.elikill58.negativity.sponge.SpongeNegativityPlayer;
import com.elikill58.negativity.sponge.utils.Cheat;
import com.elikill58.negativity.sponge.utils.ReportType;
import com.elikill58.negativity.sponge.utils.Utils;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.text.NumberFormat;

public class SpiderProtocol implements NeedListener {

	@Listener
	public void onPlayerMove(MoveEntityEvent e, @First Player p) {
		if (!p.gameMode().get().equals(GameModes.SURVIVAL) && !p.gameMode().get().equals(GameModes.ADVENTURE))
			return;
		SpongeNegativityPlayer np = SpongeNegativityPlayer.getNegativityPlayer(p);
		Location<World> loc = p.getLocation();
		if (!np.hasDetectionActive(Cheat.SPIDER))
			return;
		if (np.getFallDistance() != 0.0F)
			return;
		BlockType playerLocType = loc.getBlock().getType(),
				underPlayer = loc.copy().sub(0, 1, 0).getBlock().getType(),
				underUnder = loc.copy().sub(0, 2, 0).getBlock().getType(),
				m3 = loc.copy().add(0, 3, 0).getBlock().getType();
		if (!underPlayer.equals(BlockTypes.AIR) || !underUnder.equals(BlockTypes.AIR) || playerLocType.equals(BlockTypes.VINE) || playerLocType.equals(BlockTypes.LADDER)
				|| underPlayer.equals(BlockTypes.VINE) || underPlayer.equals(BlockTypes.LADDER) || m3.equals(BlockTypes.VINE)
				|| m3.equals(BlockTypes.LADDER) || !playerLocType.equals(BlockTypes.AIR))
			return;
		double y = e.getToTransform().getLocation().getY() - e.getFromTransform().getLocation().getY(), last = np.lastY;
		np.lastY = y;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumIntegerDigits(4);
		boolean isAris = ((float) y) == p.get(Keys.WALKING_SPEED).get();
		if (((y > 0.499 && y < 0.7) || isAris || last == y) && hasOtherThan(loc, BlockTypes.AIR)) {
			boolean hasSlabStairs = false;
			for (int u = 0; u < 360; u += 3) {
				Location<World> flameloc = loc.copy();
				flameloc.add(Math.sin(u) * 3, 0, Math.cos(u) * 3);
				String name = flameloc.copy().getBlock().getType().getName(),
						secondname = flameloc.copy().add(0, 1, 0).getBlock().getType().getName();
				if (name.contains("SLAB") || name.contains("STAIRS") || secondname.contains("SLAB")
						|| secondname.contains("STAIRS"))
					hasSlabStairs = true;
			}
			if (hasSlabStairs)
				return;
			int relia = (int) (y * 450);
			if (isAris)
				relia = relia + 39;
			ReportType type =  (np.getWarn(Cheat.SPIDER) > 6) ? ReportType.VIOLATION : ReportType.WARNING;
			boolean mayCancel = SpongeNegativity.alertMod(type, p, Cheat.SPIDER, Utils.parseInPorcent(relia),
					"Nothing around him. To > From: " + y + " isAris: " + isAris + " has not stab slairs.");
			if(Cheat.SPIDER.isSetBack() && mayCancel){
				Location<World> locc = p.getLocation();
				while(locc.getBlock().getType().equals(BlockTypes.AIR))
					locc.sub(0, 1, 0);
				p.setLocation(locc.add(0, 1, 0));
			}
		}
	}

	public boolean hasOtherThan(Location<World> loc, BlockType m) {
		if (!loc.copy().add(0, 0, 1).getBlock().getType().equals(m))
			return true;
		if (!loc.copy().add(1, 0, -1).getBlock().getType().equals(m))
			return true;
		if (!loc.copy().add(-1, 0, -1).getBlock().getType().equals(m))
			return true;
		return !loc.copy().add(-1, 0, 1).getBlock().getType().equals(m);
	}
}
