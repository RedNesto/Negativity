package com.elikill58.negativity.velocity;

import java.util.Locale;
import java.util.Optional;

import com.elikill58.negativity.universal.permissions.Perm;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.ChannelMessageSink;

import net.kyori.text.TextComponent;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;

public class VelocityListener {

	private final VelocityNegativity plugin;

	public VelocityListener(VelocityNegativity plugin) {
		this.plugin = plugin;
	}

	@Subscribe
	public void onMessageReceived(PluginMessageEvent event) {
		ChannelIdentifier channelId = event.getIdentifier();
		if (!channelId.getId().toLowerCase(Locale.ROOT).contains("negativity:")) {
			return;
		}

		String line = event.dataAsDataStream().readLine();
		// It seems the line we get starts with a space and another character
		// like '+', '-', ',', and digits for example.
		// From my experience it always appears, so let's drop them.
		line = line.substring(2);
		String[] parts = line.split("/\\*\\*/");
		Optional<String> targetServerCommand = findServerName(event.getTarget())
				.map(serverName -> "/server " + serverName);
		if (parts.length > 3) {
			String[] alertPlaceholders = new String[]{"%name%", parts[0], "%cheat%", parts[1], "%reliability%", parts[2], "%ping%", parts[3]};
			String alertMessageKey = parts.length > 5 ? parts[5] : "alert";
			for (Player player : plugin.getProxyServer().getAllPlayers()) {
				if (!Perm.hasPerm(VelocityNegativityPlayer.getNegativityPlayer(player.getUniqueId()), "showAlert")) {
					continue;
				}
				TextComponent alertHoverMessage = VelocityMessages.getMessage(player, "alert_hover", alertPlaceholders);
				if (parts.length > 4) {
					String hoverInfo = parts[4];
					alertHoverMessage = alertHoverMessage.append(TextComponent.of(hoverInfo));
				}
				TextComponent alertMessage = VelocityMessages.getMessage(player, alertMessageKey, alertPlaceholders)
						.hoverEvent(HoverEvent.showText(alertHoverMessage));
				if (targetServerCommand.isPresent()) {
					alertMessage = alertMessage.clickEvent(ClickEvent.runCommand(targetServerCommand.get()));
				}
				player.sendMessage(alertMessage);
			}
		} else {
			String[] alertPlaceholders = new String[]{"%name%", parts[0], "%reason%", parts[1], "%report%", parts[2]};
			boolean alertSent = false;
			for (Player player : plugin.getProxyServer().getAllPlayers()) {
				// TODO check for the permission showReport
				TextComponent alertMessage = VelocityMessages.getMessage(player, "report", alertPlaceholders);
				if (targetServerCommand.isPresent()) {
					alertMessage = alertMessage.clickEvent(ClickEvent.runCommand(targetServerCommand.get()));
				}
				player.sendMessage(alertMessage);
				alertSent = true;
			}

			if (!alertSent) {
				// TODO store a report
			}
		}
	}

	private static Optional<String> findServerName(ChannelMessageSink sink) {
		if (sink instanceof Player) {
			return ((Player) sink).getCurrentServer()
					.map(server -> server.getServerInfo().getName());
		}

		if (sink instanceof ServerConnection) {
			return Optional.of(((ServerConnection) sink).getServerInfo().getName());
		}

		return Optional.empty();
	}
}
