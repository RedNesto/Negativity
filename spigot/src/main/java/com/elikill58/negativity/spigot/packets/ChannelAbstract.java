package com.elikill58.negativity.spigot.packets;

import com.elikill58.negativity.spigot.packets.PacketAbstract.IPacketListener;
import org.bukkit.entity.Player;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class ChannelAbstract {

	final Executor addChannelExecutor = Executors.newSingleThreadExecutor(), removeChannelExecutor = Executors.newSingleThreadExecutor();

	public static final String KEY_HANDLER = "packet_handler", KEY_PLAYER = "packet_listener_player", KEY_SERVER = "packet_listener_server";

	private IPacketListener iPacketListener;

	public ChannelAbstract(IPacketListener iPacketListener) {
		this.iPacketListener = iPacketListener;
	}

	public abstract void addChannel(Player player);

	public abstract void removeChannel(Player player);

	protected final Object onPacketReceive(Object sender, Object packet) {
		return iPacketListener.onPacketReceive(sender, packet);
	}
}
