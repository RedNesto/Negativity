package com.elikill58.negativity.spigot.support;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EssentialsSupport {

    private static Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

    public static boolean checkEssentialsPrecondition(Player p) {
		return essentials.getUser(p).isGodModeEnabled();
	}
}
