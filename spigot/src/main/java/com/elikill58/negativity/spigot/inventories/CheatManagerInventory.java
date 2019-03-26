package com.elikill58.negativity.spigot.inventories;

import com.elikill58.negativity.spigot.Inv;
import com.elikill58.negativity.spigot.Messages;
import com.elikill58.negativity.spigot.SpigotNegativity;
import com.elikill58.negativity.spigot.utils.Cheat;
import com.elikill58.negativity.spigot.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Optional;

public class CheatManagerInventory {

	public static void openCheatManagerMenu(Player p){
		Inventory inv = Bukkit.createInventory(null, Utils.getMultipleOf(Cheat.values().length + 3, 9, 1), Inv.CHEAT_MANAGER);
		int slot = 0;
		for(Cheat c : Cheat.values())
			if(c.getMaterial() != null && c.getProtocolClass() != null)
				inv.setItem(slot++, Utils.createItem(c.getMaterial(), c.getName()));

		inv.setItem(inv.getSize() - 2, Utils.createItem(Material.ARROW, Messages.getMessage(p, "inventory.back")));
		inv.setItem(inv.getSize() - 1, Utils.createItem(SpigotNegativity.MATERIAL_CLOSE, Messages.getMessage(p, "inventory.close")));
		p.openInventory(inv);
	}

    public static void manageCheatManagerMenu(InventoryClickEvent e, Material m, Player p) {
		e.setCancelled(true);
		if (m.equals(SpigotNegativity.MATERIAL_CLOSE))
			p.closeInventory();
		else if (m.equals(Material.ARROW))
			ModInventory.openModMenu(p);
		else {
			Optional<Cheat> c = Utils.getCheatFromItem(m);
			if (c.isPresent())
				OneCheatInventory.openOneCheatMenu(p, c.get());
		}
	}
}
