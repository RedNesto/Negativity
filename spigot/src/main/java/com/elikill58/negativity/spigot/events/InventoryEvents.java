package com.elikill58.negativity.spigot.events;

import com.elikill58.negativity.spigot.Inv;
import com.elikill58.negativity.spigot.inventories.*;
import com.elikill58.negativity.spigot.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryEvents implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getCurrentItem() == null || e.getClickedInventory() == null || !(e.getWhoClicked() instanceof Player))
			return;
		Player p = (Player) e.getWhoClicked();
		Material m = e.getCurrentItem().getType();
		String invName = e.getClickedInventory().getName();
		if (invName.equals(Inv.NAME_CHECK_MENU)) {
			CheckMenuInventory.manageCheckMenu(e, m, p);
		} else if (invName.equals(Inv.NAME_ACTIVED_CHEAT_MENU)) {
			ActivedCheatInventory.manageActivedCheatMenu(e, m, p);
		} else if (invName.equals(Inv.NAME_FREEZE_MENU))
			e.setCancelled(true);
		else if (invName.equals(Inv.NAME_MOD_MENU)) {
			ModInventory.manageModMenu(e, m, p);
		} else if (invName.equals(Inv.NAME_ALERT_MENU)) {
			AlertInventory.manageAlertMenu(e, m, p);
		} else if (invName.equals(Inv.CHEAT_MANAGER)) {
			CheatManagerInventory.manageCheatManagerMenu(e, m, p);
		} else if (invName.equals(Inv.NAME_FORGE_MOD_MENU)) {
			ForgeModsInventory.manageForgeModsMenu(e, m, p);
		} else if (Utils.getCheatFromName(invName).isPresent()) {
			OneCheatInventory.manageOneCheatMenu(e, m, p);
		}
	}
}
