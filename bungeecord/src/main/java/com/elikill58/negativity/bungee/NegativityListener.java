package com.elikill58.negativity.bungee;

import com.elikill58.negativity.universal.Stats;
import com.elikill58.negativity.universal.Stats.StatsType;
import com.elikill58.negativity.universal.ban.Ban;
import com.elikill58.negativity.universal.ban.BanRequest;
import com.elikill58.negativity.universal.permissions.Perm;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class NegativityListener implements Listener {

	public static boolean sendBungeecordInfo = false;
	public static List<Report> report = new ArrayList<>();

	@EventHandler
	public void onMessageReceived(PluginMessageEvent event) {
		if (!event.getTag().equals("Negativity"))
			return;

		try (ByteArrayInputStream ba = new ByteArrayInputStream(event.getData());
				DataInputStream in = new DataInputStream(ba)) {
			String line = in.readUTF();
			ProxiedPlayer p = (event.getSender() instanceof ProxiedPlayer ? (ProxiedPlayer) event.getSender()
					: (event.getReceiver() instanceof ProxiedPlayer ? (ProxiedPlayer) event.getReceiver() : null));
			if (p == null)
				System.err.println("Error in BungeeNegativity ! Sender and receiver not proxied (Sender: "
						+ event.getSender() + " Receiver: " + event.getReceiver() + ")");
			String[] parts = line.split("/\\*\\*/");
			if (parts.length > 3) {
				String[] place = new String[] { "%name%", parts[0], "%cheat%", parts[1], "%reliability%", parts[2],
						"%ping%", parts[3] };
				String cmd = "/server " + p.getServer().getInfo().getName();
				for (ProxiedPlayer pp : ProxyServer.getInstance().getPlayers())
					if (Perm.hasPerm(BungeeNegativityPlayer.getNegativityPlayer(pp), "showAlert")) {
						TextComponent msg = new TextComponent(BungeeMessages.getMessage(pp, "alert", place));
						String hover = BungeeMessages.getMessage(pp, "alert_hover", place);
						if (hover.contains("\n")) {
							ArrayList<TextComponent> components = new ArrayList<>();
							TextComponent hoverMessage = new TextComponent(
									new ComponentBuilder(hover.split("\n")[hover.split("\n").length - 2])
											.color(ChatColor.GOLD).create());
							hoverMessage.addExtra(new TextComponent(ComponentSerializer.parse("{text: \"\n\"}")));
							hoverMessage.addExtra(new TextComponent(
									new ComponentBuilder(hover.split("\n")[hover.split("\n").length - 1]).create()));
							components.add(hoverMessage);
							msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
									components.toArray(new BaseComponent[components.size()])));
						} else
							msg.setHoverEvent(
									new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
						msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
						pp.sendMessage(msg);
					}
			} else {
				String cmd = "/server " + p.getServer().getInfo().getName();
				String[] place = new String[] { "%name%", parts[0], "%reason%", parts[1], "%report%", parts[2] };
				boolean hasPermitted = false;
				for (ProxiedPlayer pp : ProxyServer.getInstance().getPlayers())
					if (Perm.hasPerm(BungeeNegativityPlayer.getNegativityPlayer(pp), "showReport")) {
						hasPermitted = true;
						TextComponent msg = new TextComponent(BungeeMessages.getMessage(pp, "report", place));
						msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
						pp.sendMessage(msg);
					}
				if (!hasPermitted)
					report.add(new Report(cmd, place));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onPostLogin(PostLoginEvent e) {
		ProxiedPlayer p = e.getPlayer();
		BungeeNegativityPlayer np = BungeeNegativityPlayer.getNegativityPlayer(p);
		if(Ban.isBanned(np)) {
			if(Ban.canConnect(np))
				return;
			boolean isDef = false;
			for(BanRequest br : np.getBanRequest())
				if(br.isDef())
					isDef = true;
			p.disconnect(new ComponentBuilder(BungeeMessages.getMessage(e.getPlayer(), "ban.kick_" + (isDef ? "def" : "time"), "%reason%", np.getBanReason(), "%time%" , np.getBanTime(), "%by%", np.getBanBy())).create());
		}
		Stats.updateStats(StatsType.PLAYERS, ProxyServer.getInstance().getPlayers().size());
		if (sendBungeecordInfo)
			return;
		if (Perm.hasPerm(np, "showAlert"))
			for (Report msg : report) {
				p.sendMessage(msg.toMessage(p));
				report.remove(msg);
			}
		try {
			ByteArrayOutputStream ba = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(ba);
			out.writeUTF("bungeecord");
			byte[] data = ba.toByteArray();
			p.sendData("Negativity", data);
			sendBungeecordInfo = true;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@EventHandler
	public void onDisconnect(PlayerDisconnectEvent e) {
		Stats.updateStats(StatsType.PLAYERS, ProxyServer.getInstance().getPlayers().size());
	}

	public static class Report {

		private String[] place;
		private String cmd;

		public Report(String cmd, String... parts) {
			place = new String[] { "%name%", parts[0], "%cheat%", parts[1], "%reliability%", parts[2], "%ping%",
					parts[3] };
			this.cmd = cmd;
		}

		public TextComponent toMessage(ProxiedPlayer p) {
			TextComponent msg = new TextComponent(BungeeMessages.getMessage(p, "alert", place));
			String hover = BungeeMessages.getMessage(p, "alert_hover", place);
			if (hover.contains("\\n")) {
				ArrayList<TextComponent> components = new ArrayList<>();
				TextComponent hoverMessage = new TextComponent(
						new ComponentBuilder(hover.split("\\n")[hover.split("\\n").length - 2]).color(ChatColor.GOLD)
								.create());
				hoverMessage.addExtra(new TextComponent(ComponentSerializer.parse("{text: \"\n\"}")));
				hoverMessage.addExtra(new TextComponent(
						new ComponentBuilder(hover.split("\\n")[hover.split("\\n").length - 1]).create()));
				components.add(hoverMessage);
				msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						components.toArray(new BaseComponent[components.size()])));
			} else
				msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
			return msg;
		}
	}
}
