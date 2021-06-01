package com.elikill58.negativity.common.commands;

import static com.elikill58.negativity.universal.verif.VerificationManager.CONSOLE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

import com.elikill58.negativity.api.GameMode;
import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.colors.ChatColor;
import com.elikill58.negativity.api.commands.Command;
import com.elikill58.negativity.api.commands.CommandRoot;
import com.elikill58.negativity.api.commands.CommandSender;
import com.elikill58.negativity.api.entity.OfflinePlayer;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.inventory.AbstractInventory.NegativityInventory;
import com.elikill58.negativity.api.inventory.InventoryManager;
import com.elikill58.negativity.api.utils.Utils;
import com.elikill58.negativity.api.yaml.config.Configuration;
import com.elikill58.negativity.universal.Adapter;
import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.Cheat.CheatCategory;
import com.elikill58.negativity.universal.CheatKeys;
import com.elikill58.negativity.universal.Messages;
import com.elikill58.negativity.universal.Negativity;
import com.elikill58.negativity.universal.Scheduler;
import com.elikill58.negativity.universal.account.NegativityAccount;
import com.elikill58.negativity.universal.ban.BanManager;
import com.elikill58.negativity.universal.ban.OldBansDbMigrator;
import com.elikill58.negativity.universal.bypass.BypassManager;
import com.elikill58.negativity.universal.permissions.Perm;
import com.elikill58.negativity.universal.playerModifications.PlayerModifications;
import com.elikill58.negativity.universal.playerModifications.PlayerModificationsManager;
import com.elikill58.negativity.universal.report.ReportType;
import com.elikill58.negativity.universal.translation.MessagesUpdater;
import com.elikill58.negativity.universal.verif.VerificationManager;
import com.elikill58.negativity.universal.verif.Verificator;
import com.elikill58.negativity.universal.webhooks.Webhook;
import com.elikill58.negativity.universal.webhooks.WebhookManager;

@CommandRoot("negativity")
public final class NegativityCommand {
	
	@Command
	public static void negativity(Player sender, OfflinePlayer targetPlayer) {
		// TODO workaround for a bug in SpigotAdapter#getOfflinePlayer never returning null
		if (!targetPlayer.hasPlayedBefore()) {
			return;
		}
		
		// TODO add permission to Command annotation and check for it upstream
		if (!Perm.hasPerm(sender, Perm.CHECK)) {
			Messages.sendMessage(sender, "not_permission");
			return;
		}
		
		if (targetPlayer instanceof Player) {
			InventoryManager.open(NegativityInventory.CHECK_MENU, sender, targetPlayer);
		} else {
			InventoryManager.open(NegativityInventory.CHECK_MENU_OFFLINE, sender, targetPlayer);
		}
	}
	
	//@Command
	// TODO missing ability to define time default value and get a Set<Cheat>
	public static void verif(CommandSender sender, Player target, int time, Set<Cheat> cheatsToVerify) {
		if (Perm.hasPerm(sender, Perm.VERIF)) {
			Messages.sendMessage(sender, "not_permission");
			return;
		}
		
		// TODO send proper error messages upstream about requiring a Player sender and an invalid target Player
		//if (arg.length < 2) {
		//	Messages.sendMessage(sender, "not_forget_player");
		//	return;
		//}
		//
		//if (target == null) {
		//	Messages.sendMessage(sender, "invalid_player", "%arg%", arg[1]);
		//	return;
		//}
		
		NegativityPlayer nTarget = NegativityPlayer.getNegativityPlayer(target);
		if (cheatsToVerify.isEmpty()) {
			nTarget.startAllAnalyze();
			Messages.sendMessage(sender, "negativity.verif.start_all", "%name%", target.getName(), "%time%", time);
			cheatsToVerify = new HashSet<>(Cheat.CHEATS);
		} else {
			StringJoiner cheatsList = new StringJoiner(", ");
			for (Cheat cheat : cheatsToVerify) {
				cheatsList.add(cheat.getName());
			}
			Messages.sendMessage(sender, "negativity.verif.start", "%name%", target.getName(), "%cheat%", cheatsList, "%time%", time);
		}
		//int time = UniversalUtils.getFirstInt(arg).orElse(VerificationManager.getTimeVerif() / 20);
		//Set<Cheat> cheatsToVerify = new LinkedHashSet<>();
		//if (arg.length == 2 || (arg.length == 3 && UniversalUtils.isInteger(arg[2]))) {
		//	nTarget.startAllAnalyze();
		//	Messages.sendMessage(sender, "negativity.verif.start_all", "%name%", target.getName(), "%time%", time);
		//	cheatsToVerify.addAll(Cheat.CHEATS);
		//} else {
		//	StringJoiner cheatNamesJoiner = new StringJoiner(", ");
		//	for (int i = 2; i < arg.length; i++) {
		//		Cheat cheat = Cheat.fromString(arg[i]);
		//		if (cheat != null) {
		//			nTarget.startAnalyze(cheat);
		//			cheatNamesJoiner.add(cheat.getName());
		//			cheatsToVerify.add(cheat);
		//		}
		//	}
		//
		//	String cheatsList = cheatNamesJoiner.toString();
		//	if (cheatsList.isEmpty()) {
		//		Messages.sendMessage(sender, "negativity.verif.start_none");
		//		return;
		//	} else {
		//		Messages.sendMessage(sender, "negativity.verif.start", "%name%", target.getName(), "%cheat%", cheatsList, "%time%", time);
		//	}
		//}
		UUID senderId = sender instanceof Player ? ((Player) sender).getUniqueId() : CONSOLE;
		VerificationManager.create(senderId, target.getUniqueId(), new Verificator(nTarget, sender.getName(), cheatsToVerify));
		Scheduler.getInstance().runDelayed(() -> {
			Verificator verif = VerificationManager.getVerificationsFrom(target.getUniqueId(), senderId).get();
			verif.generateMessage();
			verif.getMessages().forEach((s) -> sender.sendMessage(Utils.coloredMessage("&a[&2Verif&a] " + s)));
			verif.save();
			VerificationManager.remove(senderId, target.getUniqueId());
		}, time * 20);
	}
	
	@Command
	public static void alert(Player sender) {
		NegativityPlayer np = NegativityPlayer.getCached(sender.getUniqueId());
		np.disableShowingAlert = !np.disableShowingAlert;
		Messages.sendMessage(sender, np.disableShowingAlert ? "negativity.see_no_longer_alert" : "negativity.see_alert");
	}
	
	@Command
	public static void reload(CommandSender sender) {
		if (Perm.hasPerm(sender, Perm.RELOAD)) {
			Messages.sendMessage(sender, "not_permission");
			return;
		}
		Negativity.loadNegativity();
		Adapter.getAdapter().reload();
		Messages.sendMessage(sender, "negativity.reload_done");
	}
	
	@Command
	public static void mod(Player sender) {
		if (!Perm.hasPerm(sender, Perm.MOD)) {
			Messages.sendMessage(sender, "not_permission");
			return;
		}
		InventoryManager.getInventory(NegativityInventory.MOD).ifPresent(inv -> inv.openInventory(sender));
	}
	
	@Command({"admin", "manage"})
	public static void admin(CommandSender sender, Optional<String> subcommand) {
		// TODO proper subcommand support, in an Admin nested class probably
		if (subcommand.isPresent() && subcommand.get().equalsIgnoreCase("updateMessages")) {
			if (!Perm.hasPerm(sender, Perm.MANAGE_CHEAT)) {
				Messages.sendMessage(sender, "not_permission");
				return;
			}
			
			MessagesUpdater.performUpdate("lang", (message, placeholders) -> Messages.sendMessage(sender, message, (Object[]) placeholders));
			return;
		}
		
		if (!(sender instanceof Player)) {
			Messages.sendMessage(sender, "only_player");
			return;
		}
		
		Player p = (Player) sender;
		if (!Perm.hasPerm(p, Perm.MANAGE_CHEAT)) {
			Messages.sendMessage(sender, "not_permission");
			return;
		}
		
		InventoryManager.open(NegativityInventory.ADMIN, p);
	}
	
	@Command
	public static void migrateOldBans(CommandSender sender) {
		// TODO add ability to create "hidden" commands for this one
		try {
			OldBansDbMigrator.performMigration();
		} catch (Exception e) {
			sender.sendMessage("An error occurred when performing migration: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Command
	public static void clear(CommandSender sender, Player target) {
		if (!Perm.hasPerm(sender, Perm.MOD)) {
			Messages.sendMessage(sender, "not_permission");
			return;
		}
		
		NegativityAccount account = NegativityAccount.get(target.getUniqueId());
		for (Cheat c : Cheat.values()) {
			account.setWarnCount(c, 0);
		}
		
		Adapter.getAdapter().getAccountManager().update(account);
		Messages.sendMessage(sender, "negativity.cleared", "%name%", target.getName());
	}
	
	@Command
	public static void webhook(CommandSender sender) {
		if (!Perm.hasPerm(sender, Perm.ADMIN)) {
			Messages.sendMessage(sender, "not_permission");
			return;
		}
		
		if (WebhookManager.isEnabled()) {
			List<Webhook> webhooks = WebhookManager.getWebhooks();
			if (webhooks.isEmpty()) {
				sender.sendMessage(ChatColor.YELLOW + "No webhook configurated.");
			} else {
				for (Webhook hook : webhooks) {
					if (hook.ping(sender.getName())) {
						sender.sendMessage(ChatColor.GREEN + hook.getWebhookName() + " well configurated.");
					} else {
						sender.sendMessage(ChatColor.RED + hook.getWebhookName() + " seems to don't work.");
					}
				}
			}
		} else {
			sender.sendMessage(ChatColor.YELLOW + "Webhooks are disabled.");
		}
	}
	
	@Command
	public static void debug(Player sender, Optional<Player> optionalTarget, Optional<Cheat> cheat) {
		Adapter ada = Adapter.getAdapter();
		Player target = optionalTarget.orElse(sender);
		String name = target == sender ? "You" : target.getName();
		NegativityPlayer np = NegativityPlayer.getNegativityPlayer(target);
		sender.sendMessage(ChatColor.YELLOW + "--- Checking debug for bypass | no alert ---");
		sender.sendMessage(ChatColor.GOLD + ada.getName() + ": " + ada.getServerVersion() + ". Negativity " + ada.getVersion());
		long time = System.currentTimeMillis();
		boolean hasBypass = false;
		Cheat c = Cheat.values().stream().filter(Cheat::isActive).findFirst().get();
		if (np.TIME_INVINCIBILITY > time) {
			sender.sendMessage(ChatColor.RED + "Invincibility (stay " + (time - np.TIME_INVINCIBILITY) + "ms)");
			hasBypass = true;
		}
		if (np.isFreeze) {
			sender.sendMessage(ChatColor.RED + name + " are currently freezed.");
			hasBypass = true;
		}
		double tps = ada.getLastTPS();
		if (ada.getConfig().getDouble("tps_alert_stop") > tps) {
			sender.sendMessage(ChatColor.RED + "Too low TPS : " + tps);
			hasBypass = true;
		}
		if (!(target.getGameMode().equals(GameMode.SURVIVAL) || target.getGameMode().equals(GameMode.ADVENTURE))) {
			sender.sendMessage(ChatColor.RED + "Lot of detection are disabled if you're not on survival/adventure.");
			hasBypass = true;
		}
		int ping = target.getPing();
		if (np.isInFight)
			hasBypass = true;
		if (cheat.isPresent()) {
			c = cheat.get();
			sender.sendMessage(ChatColor.GREEN + "Checking for cheat " + c.getName() + ".");
			if (!c.isActive()) {
				sender.sendMessage(ChatColor.RED + "Cheat disabled.");
				hasBypass = true;
			}
			if (!np.already_blink && c.getKey().equals(CheatKeys.BLINK)) {
				sender.sendMessage(ChatColor.RED + "Bypass for blink.");
				hasBypass = true;
			}
			if (BypassManager.hasBypass(sender, c)) {
				sender.sendMessage(ChatColor.RED + "You have a bypass actually");
				hasBypass = true;
			}
			if (c.getCheatCategory().equals(CheatCategory.MOVEMENT)) {
				for (PlayerModifications modification : PlayerModificationsManager.getModifications()) {
					if (modification.shouldIgnoreMovementChecks(sender)) {
						sender.sendMessage(ChatColor.RED + modification.getDisplayname() + " movement bypass.");
						hasBypass = true;
					}
				}
			}
			if (c.getKey().equals(CheatKeys.FLY)) {
				for (PlayerModifications modification : PlayerModificationsManager.getModifications()) {
					if (modification.canFly(sender)) {
						sender.sendMessage(ChatColor.RED + modification.getDisplayname() + " fly bypass.");
						hasBypass = true;
					}
				}
			}
			if (np.isInFight && c.isBlockedInFight()) {
				sender.sendMessage(ChatColor.RED + "Bypass because in fight.");
				hasBypass = true;
			}
			if (ping > c.getMaxAlertPing()) {
				sender.sendMessage(ChatColor.RED + "To high ping ! " + ChatColor.YELLOW + "(" + ping + " > " + c.getMaxAlertPing() + ")");
				hasBypass = true;
			}
			if (!np.hasDetectionActive(c)) {
				sender.sendMessage(ChatColor.RED + "Detection of " + c.getName() + " not active.");
				hasBypass = true;
			}
		} else
			sender.sendMessage(ChatColor.YELLOW + (np.isInFight ? "In fight, " : "") + "Ping: " + ping + "ms (by default, at 200ms you bypass it)");
		if (ping > c.getMaxAlertPing() || ping > 200)
			hasBypass = true;
		sender.sendMessage(hasBypass ? ChatColor.RED + "Warn: " + name + " have bypass, so you cannot be detected." : ChatColor.GREEN + "Good news: " + name + " can be detected !");
		if (!hasBypass)
			Negativity.alertMod(ReportType.INFO, target, c, 100, "test", "");
	}
	
	//@Override
	public boolean onCommand(CommandSender sender, String[] arg, String prefix) {
		if (arg.length == 0 || arg[0].equalsIgnoreCase("help")) {
			sendHelp(sender);
			return true;
		}
		
		if (arg[0].equalsIgnoreCase("verif")) {
			return true;
		} else if (arg[0].equalsIgnoreCase("alert")) {
			return true;
		} else if (arg[0].equalsIgnoreCase("reload")) {
			return true;
		} else if (arg[0].equalsIgnoreCase("mod")) {
			return true;
		} else if (arg[0].equalsIgnoreCase("admin") || arg[0].toLowerCase(Locale.ROOT).contains("manage")) {
			return true;
		} else if (arg[0].equalsIgnoreCase("migrateoldbans") && !(sender instanceof Player)) {
			return true;
		} else if (arg[0].equalsIgnoreCase("clear")) {
		} else if (arg[0].equalsIgnoreCase("webhook")) {
			return true;
		} else if (arg[0].equalsIgnoreCase("debug")) {
			return true;
		}
		
		sendHelp(sender);
		return true;
	}
	
	private void sendHelp(CommandSender sender) {
		if (Perm.hasPerm(sender, Perm.VERIF))
			Messages.sendMessageList(sender, "negativity.verif.help");
		if (Perm.hasPerm(sender, Perm.CHECK))
			Messages.sendMessageList(sender, "negativity.help");
		if (sender instanceof Player)
			Messages.sendMessageList(sender, "negativity.alert.help");
		if (Perm.hasPerm(sender, Perm.RELOAD))
			Messages.sendMessageList(sender, "negativity.reload.help");
		if (sender instanceof Player && Perm.hasPerm(sender, Perm.MOD))
			Messages.sendMessageList(sender, "negativity.mod.help");
		if (Perm.hasPerm(sender, Perm.MOD))
			Messages.sendMessageList(sender, "negativity.clear.help");
		if (sender instanceof Player && Perm.hasPerm(sender, Perm.MANAGE_CHEAT))
			Messages.sendMessageList(sender, "negativity.admin.help");
		if (Perm.hasPerm(sender, Perm.ADMIN) && WebhookManager.isEnabled())
			Messages.sendMessageList(sender, "negativity.webhook.help");
		if (sender instanceof Player)
			Messages.sendMessageList(sender, "negativity.debug.help");
		Configuration conf = Adapter.getAdapter().getConfig();
		if (conf.getBoolean("commands.report") && Perm.hasPerm(sender, Perm.REPORT))
			Messages.sendMessage(sender, "report.help");
		if (conf.getBoolean("commands.kick") && Perm.hasPerm(sender, Perm.MOD))
			Messages.sendMessage(sender, "kick.help");
		if (Perm.hasPerm(sender, Perm.LANG))
			Messages.sendMessage(sender, "lang.help");
		Configuration banConfig = BanManager.getBanConfig();
		if (banConfig.getBoolean("commands.ban") && Perm.hasPerm(sender, Perm.BAN))
			Messages.sendMessageList(sender, "ban.help");
		if (banConfig.getBoolean("commands.unban") && Perm.hasPerm(sender, Perm.UNBAN))
			Messages.sendMessage(sender, "unban.help");
	}
	
	//@Override
	public List<String> onTabComplete(CommandSender sender, String[] arg, String prefix) {
		List<String> suggestions = new ArrayList<>();
		if (arg.length == 1) {
			// /negativity |
			for (com.elikill58.negativity.api.entity.Player p : Adapter.getAdapter().getOnlinePlayers()) {
				if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT)) || prefix.isEmpty()) {
					suggestions.add(p.getName());
				}
			}
			if ("verif".startsWith(prefix))
				suggestions.add("verif");
			if ("reload".startsWith(prefix))
				suggestions.add("reload");
			if ("alert".startsWith(prefix))
				suggestions.add("alert");
			if ("admin".startsWith(prefix) && (sender instanceof Player) && Perm.hasPerm(NegativityPlayer.getCached(((Player) sender).getUniqueId()), Perm.MANAGE_CHEAT))
				suggestions.add("admin");
			if ("debug".startsWith(prefix))
				suggestions.add("debug");
		} else {
			if (arg[0].equalsIgnoreCase("verif") || arg[0].equalsIgnoreCase("debug")) {
				// both command use tab arguments to works
				if (arg.length == 2) {
					// /negativity verif | OR /negativity debug |
					for (Player p : Adapter.getAdapter().getOnlinePlayers()) {
						if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT)) || prefix.isEmpty()) {
							suggestions.add(p.getName());
						}
					}
				} else if (Adapter.getAdapter().getPlayer(arg[1]) != null) {
					// /negativity verif <target> |
					for (Cheat c : Cheat.values()) {
						if (c.getName().toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT)) || prefix.isEmpty()) {
							suggestions.add(c.getName());
						}
					}
				}
			} else if (arg[0].equalsIgnoreCase("admin") && arg.length == 2) {
				if (sender instanceof Player && Perm.hasPerm(NegativityPlayer.getCached(((Player) sender).getUniqueId()), Perm.MANAGE_CHEAT)) {
					suggestions.add("updateMessages");
				}
			}
		}
		return suggestions;
	}
}
