package com.elikill58.negativity.spigot.packets;

import com.elikill58.negativity.spigot.packets.ChannelInjector.ChannelWrapper;
import org.bukkit.entity.Player;

public class ReceivedPacket extends PacketAbstract {

	public ReceivedPacket(Object packet, Player player) {
		super(packet, player);
	}

	public ReceivedPacket(Object packet, ChannelWrapper<?> channelWrapper) {
		super(packet, channelWrapper);
	}
}
