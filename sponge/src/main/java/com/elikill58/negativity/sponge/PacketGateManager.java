package com.elikill58.negativity.sponge;

import eu.crushedpixel.sponge.packetgate.api.listener.PacketListener.ListenerPriority;
import eu.crushedpixel.sponge.packetgate.api.registry.PacketGate;
import org.spongepowered.api.Sponge;

import java.util.Optional;

public class PacketGateManager {

	public static void check() {
		new Thread(() -> {
			Optional<PacketGate> packetGateOpt = Sponge.getServiceManager().provide(PacketGate.class);
			packetGateOpt.ifPresent((packetGate) -> {
				packetGate.registerListener(new PacketManager(), ListenerPriority.DEFAULT);
			});
		}).run();
	}

}
