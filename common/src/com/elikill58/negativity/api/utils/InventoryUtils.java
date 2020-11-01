package com.elikill58.negativity.api.utils;

import com.elikill58.negativity.api.inventory.Inventory;
import com.elikill58.negativity.api.item.ItemStack;

public class InventoryUtils {

	public static void fillInventory(Inventory inv, ItemStack item) {
		for (int i = 0; i < inv.getSize(); i++)
			if (inv.get(i) == null)
				inv.set(i, item);
	}
}
