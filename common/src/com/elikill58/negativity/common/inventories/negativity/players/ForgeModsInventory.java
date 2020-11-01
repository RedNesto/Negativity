package com.elikill58.negativity.common.inventories.negativity.players;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.colors.ChatColor;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.inventory.InventoryClickEvent;
import com.elikill58.negativity.api.inventory.AbstractInventory;
import com.elikill58.negativity.api.inventory.Inventory;
import com.elikill58.negativity.api.inventory.InventoryManager;
import com.elikill58.negativity.api.item.ItemBuilder;
import com.elikill58.negativity.api.item.Material;
import com.elikill58.negativity.api.item.Materials;
import com.elikill58.negativity.api.utils.InventoryUtils;
import com.elikill58.negativity.common.inventories.holders.negativity.players.ForgeModsHolder;
import com.elikill58.negativity.universal.Messages;
import com.elikill58.negativity.universal.utils.UniversalUtils;

public class ForgeModsInventory extends AbstractInventory<ForgeModsHolder> {

	public ForgeModsInventory() {
		super(NegativityInventory.FORGE_MODS, ForgeModsHolder.class);
	}
	
	public int slot = 0;
	
	@Override
	public void openInventory(Player mod, Object... args) {
		Player cible = (Player) args[0];
		slot = 0;
		NegativityPlayer np = NegativityPlayer.getNegativityPlayer(cible);
		Inventory inv = Inventory.createInventory(Inventory.NAME_FORGE_MOD_MENU, UniversalUtils.getMultipleOf(np.MODS.size() + 1, 9, 1, 54), new ForgeModsHolder(cible));
		if(np.MODS.size() == 0) {
			inv.set(4, ItemBuilder.Builder(Materials.DIAMOND).displayName("No mods").build());
		} else {
			np.MODS.forEach((name, version) -> {
				inv.set(slot++, ItemBuilder.Builder(Materials.GRASS).displayName(name).lore(ChatColor.GRAY + "Version: " + version).build());
			});
		}
		inv.set(inv.getSize() - 1, ItemBuilder.Builder(Materials.ARROW).displayName(Messages.getMessage(mod, "inventory.back")).build());
		InventoryUtils.fillInventory(inv, Inventory.EMPTY);
		mod.openInventory(inv);
	}

	@Override
	public void manageInventory(InventoryClickEvent e, Material m, Player p, ForgeModsHolder nh) {
		if(m.equals(Materials.ARROW))
			InventoryManager.open(NegativityInventory.CHECK_MENU, p, ((ForgeModsHolder) nh).getCible());
	}
}
