package com.elikill58.negativity.api.events.negativity;

import java.util.HashMap;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.Event;
import com.elikill58.negativity.universal.PacketType;

public class PlayerPacketsClearEvent implements Event {

	private final Player p;
	private final NegativityPlayer np;
	private final HashMap<PacketType, Integer> packets;
	
	public PlayerPacketsClearEvent(Player p, NegativityPlayer np) {
		this.p = p;
		this.np = np;
		this.packets = new HashMap<>(np.PACKETS);
	}
	
	public Player getPlayer() {
		return p;
	}
	
	public NegativityPlayer getNegativityPlayer() {
		return np;
	}
	
	public HashMap<PacketType, Integer> getPackets(){
		return packets;
	}
}
