package com.elikill58.negativity.velocity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.elikill58.negativity.universal.NegativityAccount;
import com.elikill58.negativity.universal.NegativityPlayer;
import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.ban.Ban;
import com.elikill58.negativity.universal.ban.BanRequest;
import com.elikill58.negativity.universal.permissions.Perm;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
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
	private final List<PendingReport> pendingReports = new ArrayList<>();

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
				if (!Perm.hasPerm(VelocityNegativityPlayer.getNegativityPlayer(player), "showAlert")) {
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
				if (!Perm.hasPerm(VelocityNegativityPlayer.getNegativityPlayer(player), "showReport")) {
					continue;
				}
				TextComponent alertMessage = VelocityMessages.getMessage(player, "report", alertPlaceholders);
				if (targetServerCommand.isPresent()) {
					alertMessage = alertMessage.clickEvent(ClickEvent.runCommand(targetServerCommand.get()));
				}
				player.sendMessage(alertMessage);
				alertSent = true;
			}

			if (!alertSent) {
				pendingReports.add(new PendingReport(targetServerCommand.orElse(null), parts));
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

	@Subscribe
	public void onLogin(LoginEvent event) {
		Player player = event.getPlayer();
		pendingReports.add(new PendingReport(null, player.getUsername(), "SPEED", "50", "3"));
		NegativityAccount account = Adapter.getAdapter().getNegativityAccount(player.getUniqueId());
		if (Ban.isBanned(account)) {
			if (Ban.canConnect(account)) {
				return;
			}

			boolean isBanDefinitive = false;
			for (BanRequest br : account.getBanRequest()) {
				if (br.isDef()) {
					isBanDefinitive = true;
					break;
				}
			}

			String msgKey = isBanDefinitive ? "ban.kick_def" : "ban.kick_time";
			TextComponent kickMessage = VelocityMessages.getMessage(player, msgKey,
					"%reason%", account.getBanReason(),
					"%time%", account.getBanTime(),
					"%by%", account.getBanBy());
			event.setResult(ResultedEvent.ComponentResult.denied(kickMessage));
			return;
		}

		plugin.getProxyServer().getScheduler().buildTask(plugin, () -> {
			player.getCurrentServer().ifPresent(server -> {
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				int modsCount = player.getModInfo().map(modInfo -> modInfo.getMods().size()).orElse(0);
				out.writeUTF("mod:" + modsCount);
				player.getModInfo().ifPresent(modInfo -> {
					modInfo.getMods().forEach(mod -> out.writeUTF(mod.getId() + ':' + mod.getVersion()));
				});
				server.sendPluginMessage(VelocityNegativity.NEGATIVITY_MOD_CHANNEL_ID, out.toByteArray());
			});
		});

		NegativityPlayer nPlayer = VelocityNegativityPlayer.getNegativityPlayer(player);
		if (Perm.hasPerm(nPlayer, "showAlert")) {
			for (PendingReport report : pendingReports) {
				player.sendMessage(report.messageFor(player));
			}
			pendingReports.clear();
		}
	}

	private static class PendingReport {

		@Nullable
		private String command;
		private String[] placeholders;

		public PendingReport(@Nullable String command, String... parts) {
			this.command = command;
			this.placeholders = new String[]{"%name%", parts[0], "%cheat%", parts[1], "%reliability%", parts[2], "%ping%", parts[3]};
		}

		public TextComponent messageFor(Player player) {
			TextComponent alertHoverMessage = VelocityMessages.getMessage(player, "alert_hover", placeholders);
			TextComponent alertMessage = VelocityMessages.getMessage(player, "alert", placeholders)
					.hoverEvent(HoverEvent.showText(alertHoverMessage));
			if (command != null) {
				alertMessage = alertMessage.clickEvent(ClickEvent.runCommand(command));
			}
			return alertMessage;
		}
	}
}
