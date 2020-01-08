package com.elikill58.negativity.spigot.packets;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elikill58.negativity.spigot.FakePlayer;
import com.elikill58.negativity.spigot.SpigotNegativityPlayer;
import com.elikill58.negativity.spigot.utils.Utils;

public abstract class PacketHandler {

	private static final List<PacketHandler> handlers = new ArrayList<>();

	public static boolean addHandler(PacketHandler handler) {
		boolean b = handlers.contains(handler);
		handlers.add(handler);
		return !b;
	}

	public static boolean removeHandler(PacketHandler handler) {
		return handlers.remove(handler);
	}

	public static void notifyHandlers(ReceivedPacket packet) {
		for (PacketHandler handler : handlers)
			handler.onReceive(packet);
	}

	public abstract void onReceive(ReceivedPacket packet);

	public static void run(Plugin pl) {
		new PacketListenerAPI(pl);
		PacketListenerAPI.addPacketHandler(new PacketHandler() {

			@Override
			public void onReceive(ReceivedPacket packet) {
				Player p = packet.getPlayer();
				if (!SpigotNegativityPlayer.INJECTED.contains(p.getUniqueId()))
					return;
				SpigotNegativityPlayer np = SpigotNegativityPlayer.getNegativityPlayer(p);
				np.ALL++;
				switch (packet.getPacketName()) {
				case "PacketPlayInFlying":
					np.FLYING++;
					break;
				case "PacketPlayInKeepAlive":
					np.KEEP_ALIVE++;
					break;
				case "PacketPlayInPositionLook":
					np.POSITION_LOOK++;
					break;
				case "PacketPlayInBlockPlace":
					np.BLOCK_PLACE++;
					break;
				case "PacketPlayInBlockDig":
					np.BLOCK_DIG++;
					break;
				case "PacketPlayInPosition":
					np.POSITION++;
					break;
				case "PacketPlayInArmAnimation":
					np.ARM++;
					break;
				case "PacketPlayInUseEntity":
					np.USE_ENTITY++;
					try {
						Object pa = Class.forName("net.minecraft.server." + Utils.VERSION + ".PacketPlayInUseEntity").cast(packet.getPacket());
						Field f = pa.getClass().getDeclaredField("a");
						f.setAccessible(true);
						int id = f.getInt(pa);
						for(FakePlayer fp : np.getFakePlayers())
							if(fp.getEntityId() == id)
								np.removeFakePlayer(fp, true);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case "PacketPlayInEntityAction":
					np.ENTITY_ACTION++;
					break;
				default:
				}
				if (!packet.getPacketName().equals("PacketPlayInKeepAlive")) { // PacketPlayInFlying
					np.TIME_OTHER_KEEP_ALIVE = System.currentTimeMillis();
					np.LAST_OTHER_KEEP_ALIVE = packet.getPacketName();
				}
			}
		});
	}
}
