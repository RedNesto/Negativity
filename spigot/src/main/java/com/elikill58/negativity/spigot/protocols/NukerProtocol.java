package com.elikill58.negativity.spigot.protocols;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.elikill58.negativity.spigot.SpigotNegativity;
import com.elikill58.negativity.spigot.SpigotNegativityPlayer;
import com.elikill58.negativity.spigot.utils.Utils;
import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.NegativityPlayer;
import com.elikill58.negativity.universal.ReportType;

public class NukerProtocol extends Cheat implements Listener {

	public NukerProtocol() {
		super("NUKER", true, Material.BEDROCK, false, true, "breaker", "bed breaker", "bedbreaker");
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		SpigotNegativityPlayer np = SpigotNegativityPlayer.getNegativityPlayer(p);
		if (!np.ACTIVE_CHEAT.contains(this))
			return;
		if(e.isCancelled())
			p.sendMessage(ChatColor.GREEN + "Cancelled");
		Block target = p.getTargetBlock(null, 5);
		if ((target.getType() != e.getBlock().getType())) {
			//e.setCancelled(true);
			boolean mayCancel = SpigotNegativity.alertMod(ReportType.WARNING, p, this, Utils.parseInPorcent(100), "BlockDig " + e.getBlock().getType().name() + ", player see " + target.getType().name() + ". Ping: " + Utils.getPing(p) + ". Warn: " + np.getWarn(this));				
			if(isSetBack() && mayCancel)
				e.setCancelled(true);
		}
		long temp = System.currentTimeMillis(), dis = temp - np.LAST_BLOCK_BREAK;
		if(dis < 50) {
			boolean mayCancel = SpigotNegativity.alertMod(ReportType.VIOLATION, p, this, (int) (100 - dis),
					"Type: " + e.getBlock().getType().name() + ". Last: " + np.LAST_BLOCK_BREAK + ", Now: " + temp + ", diff: " + dis + " (ping: " + Utils.getPing(p) + "). Warn: " + np.getWarn(this));
			if(isSetBack() && mayCancel)
				e.setCancelled(true);
		}
		np.LAST_BLOCK_BREAK = temp;
	}
	
	@Override
	public String getHoverFor(NegativityPlayer p) {
		return "";
	}
}
