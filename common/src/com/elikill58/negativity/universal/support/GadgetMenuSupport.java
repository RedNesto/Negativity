package com.elikill58.negativity.universal.support;

import com.elikill58.negativity.api.entity.Player;
import com.yapzhenyie.GadgetsMenu.api.GadgetsMenuAPI;
import com.yapzhenyie.GadgetsMenu.player.PlayerManager;

public class GadgetMenuSupport {

	public static boolean checkGadgetsMenuPreconditions(Player p) {
		PlayerManager pm = GadgetsMenuAPI.getPlayerManager((org.bukkit.entity.Player) p.getDefault());
		return pm.isFallDamageDisabled() || pm.isFireDamageDisabled() || pm.isBlockDamageDisabled();
	}
	
}
