package com.elikill58.negativity.common.timers;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.universal.Adapter;
import com.elikill58.negativity.universal.account.NegativityAccount;

public class ClickManagerTimer implements Runnable {

	@Override
	public void run() {
		for (Player p : Adapter.getAdapter().getOnlinePlayers()) {
			NegativityPlayer np = NegativityPlayer.getNegativityPlayer(p);
			NegativityAccount account = np.getAccount();
			if (account.getMostClicksPerSecond() < np.ACTUAL_CLICK) {
				account.setMostClicksPerSecond(np.ACTUAL_CLICK);
			}
			np.LAST_CLICK = np.ACTUAL_CLICK;
			np.ACTUAL_CLICK = 0;
			if (np.SEC_ACTIVE < 2) {
				np.SEC_ACTIVE++;
				return;
			}
		}
	}
}
