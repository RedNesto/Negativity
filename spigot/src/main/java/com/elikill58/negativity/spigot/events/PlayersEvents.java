package com.elikill58.negativity.spigot.events;

import com.elikill58.negativity.spigot.Messages;
import com.elikill58.negativity.spigot.SpigotNegativity;
import com.elikill58.negativity.spigot.SpigotNegativityPlayer;
import com.elikill58.negativity.spigot.commands.ReportCommand;
import com.elikill58.negativity.spigot.utils.Cheat;
import com.elikill58.negativity.spigot.utils.Utils;
import com.elikill58.negativity.universal.AbstractCheat;
import com.elikill58.negativity.universal.Minerate.MinerateType;
import com.elikill58.negativity.universal.Stats;
import com.elikill58.negativity.universal.Stats.StatsType;
import com.elikill58.negativity.universal.SuspectManager;
import com.elikill58.negativity.universal.UniversalUtils;
import com.elikill58.negativity.universal.ban.Ban;
import com.elikill58.negativity.universal.ban.BanRequest;
import com.elikill58.negativity.universal.permissions.Perm;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import java.util.ArrayList;
import java.util.List;

public class PlayersEvents implements Listener {

	@EventHandler
	public void onLogin(PlayerLoginEvent e) {
		SpigotNegativityPlayer np = SpigotNegativityPlayer.getNegativityPlayer(e.getPlayer());
		if(Ban.isBanned(np)) {
			if(Ban.canConnect(np))
				return;
			boolean isDef = false;
			for(BanRequest br : np.getBanRequest())
				if(br.isDef())
					isDef = true;
			e.setResult(Result.KICK_BANNED);
			e.setKickMessage(Messages.getMessage(e.getPlayer(), "ban.kick_" + (isDef ? "def" : "time"), "%reason%", np.getBanReason(), "%time%" , np.getBanTime(), "%by%", np.getBanBy()));
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		Stats.updateStats(StatsType.PLAYERS, Utils.getOnlinePlayers().size());
		if(UniversalUtils.isMe(p.getUniqueId()))
			p.sendMessage(ChatColor.GREEN + "Ce serveur utilise Negativity ! Waw :')");
		SpigotNegativityPlayer np = SpigotNegativityPlayer.getNegativityPlayer(e.getPlayer());
		np.TIME_INVINCIBILITY = System.currentTimeMillis() + 8000;
		if (Perm.hasPerm(np, "showAlert")) {
			if(ReportCommand.REPORT_LAST.size() > 0) {
			for (String msg : ReportCommand.REPORT_LAST)
				p.sendMessage(msg);
				ReportCommand.REPORT_LAST.clear();
			}
			Utils.sendUpdateMessageIfNeed(p);
		}
		SpigotNegativity.manageAutoVerif(p);
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		Stats.updateStats(StatsType.PLAYERS, Utils.getOnlinePlayers().size() - 1);
		if (!SpigotNegativityPlayer.contains(p))
			return;
		SpigotNegativityPlayer.getNegativityPlayer(p).destroy(false);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e){
		Player p = e.getPlayer();
		SpigotNegativityPlayer np = SpigotNegativityPlayer.getNegativityPlayer(p);
		if(np.isFreeze && !p.getLocation().subtract(0, 1, 0).getBlock().getType().equals(Material.AIR))
			e.setCancelled(true);
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent e){
		if(!(SuspectManager.ENABLED && SuspectManager.CHAT))
			return;
		String msg = e.getMessage().toLowerCase();
		String[] content = msg.split(" ");
		List<Player> suspected = new ArrayList<>();
		List<AbstractCheat> cheats = new ArrayList<>();
		for(String s : content) {
			for(Cheat c : Cheat.values())
				for(String alias : c.getAliases())
					if(alias.equalsIgnoreCase(s) || alias.contains(s) || alias.startsWith(s))
						cheats.add(c);
			for(Player p : Utils.getOnlinePlayers()) {
				if(p.getName().equalsIgnoreCase(s) || p.getName().toLowerCase().startsWith(s) || p.getName().contains(s))
					suspected.add(p);
				else if(p.getDisplayName() != null)
					if(p.getDisplayName().equalsIgnoreCase(s) || p.getDisplayName().toLowerCase().startsWith(s) || p.getDisplayName().contains(s))
						suspected.add(p);
			}
		}
		for(Player suspect : suspected)
			SuspectManager.analyzeText(SpigotNegativityPlayer.getNegativityPlayer(suspect), cheats);
	}

	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent e) {
		SpigotNegativityPlayer.getNegativityPlayer(e.getPlayer()).mineRate.addMine(MinerateType.getMinerateType(e.getBlock().getType().name()));
	}
}
