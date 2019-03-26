package com.elikill58.negativity.spigot.protocols;

import com.elikill58.negativity.spigot.SpigotNegativity;
import com.elikill58.negativity.spigot.SpigotNegativityPlayer;
import com.elikill58.negativity.spigot.utils.Cheat;
import com.elikill58.negativity.spigot.utils.ReportType;
import com.elikill58.negativity.spigot.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ScaffoldProtocol implements Listener {

	@SuppressWarnings("deprecation")
	@EventHandler (ignoreCancelled = true)
	public void onBlockBreak(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		SpigotNegativityPlayer np = SpigotNegativityPlayer.getNegativityPlayer(p);
		if (!np.ACTIVE_CHEAT.contains(Cheat.SCAFFOLD))
			return;
		int ping = Utils.getPing(p), slot = p.getInventory().getHeldItemSlot();
		if(ping > 120)
			return;
		Bukkit.getScheduler().runTaskLater(SpigotNegativity.getInstance(), new BukkitRunnable() {
			@Override
			public void run() {
				Material m = p.getItemInHand().getType(), placed = e.getBlockPlaced().getType();
				if ((m == null || (!np.isBlock(m) && !m.equals(placed))) && slot != p.getInventory().getHeldItemSlot()) {
					int localPing = ping;
					if(localPing == 0)
						localPing = 1;
					np.addWarn(Cheat.SCAFFOLD);
					boolean mayCancel = SpigotNegativity.alertMod(ReportType.WARNING, p, Cheat.SCAFFOLD, Utils.parseInPorcent(120 / localPing),
							"Item in hand: " + m.name() + " Block placed: " + placed.name() + " Ping: " + ping,
							"Item in hand: " + m.name().toLowerCase() + " \nBlock placed: " + placed.name().toLowerCase());
					if(Cheat.SCAFFOLD.isSetBack() && mayCancel) {
						p.getInventory().addItem(new ItemStack(placed));
						e.getBlockPlaced().setType(Material.AIR);
					}
				}
			}
		}, 0);
	}
}
