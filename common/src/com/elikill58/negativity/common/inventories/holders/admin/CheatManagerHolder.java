package com.elikill58.negativity.common.inventories.holders.admin;

import com.elikill58.negativity.api.inventory.NegativityHolder;

public class CheatManagerHolder extends NegativityHolder {

	private boolean isFromAdmin = false;

	public CheatManagerHolder(boolean isFromAdmin) {
		this.isFromAdmin = isFromAdmin;
	}

	public boolean isFromAdmin() {
		return isFromAdmin;
	}
}
