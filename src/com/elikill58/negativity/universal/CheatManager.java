package com.elikill58.negativity.universal;

import java.util.List;
import java.util.UUID;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.ban.BanManager;
import com.elikill58.negativity.universal.ban.BanType;
import com.elikill58.negativity.universal.ban.LoggedBan;

public class CheatManager {

	public static void banForCheat(Cheat cheat, NegativityPlayer np, int reliability) {
		if (!BanManager.banActive || !cheat.isActive())
			return;

		Adapter ada = Adapter.getAdapter();
		if (ada.getIntegerInConfig("ban.reliability_need") > reliability
				|| ada.getIntegerInConfig("ban.alert_need") > np.getWarn(cheat))
			return;

		UUID playerId = np.getAccount().getPlayerId();
		List<LoggedBan> loggedBans = BanManager.getLoggedBans(playerId);
		boolean isDefinitive = loggedBans.size() >= ada.getIntegerInConfig("ban.def.ban_time");
		long banExpiration = 0;
		if (!isDefinitive) {
			try {
				String calculator = ada.getStringInConfig("ban.time.calculator")
						.replaceAll("%reliability%", String.valueOf(reliability))
						.replaceAll("%alert%", String.valueOf(np.getWarn(cheat)));

				ScriptEngineManager factory = new ScriptEngineManager();
				ScriptEngine engine = factory.getEngineByName("JavaScript");

				String calculatorOutput = engine.eval(calculator).toString();
				banExpiration = System.currentTimeMillis() + Long.parseLong(calculatorOutput);
			} catch (ScriptException e) {
				ada.error("An error occurred when computing ban duration");
				e.printStackTrace();
			}
		}

		String reason = "Cheat (" + cheat.getName() + ")";
		BanManager.banPlayer(playerId, reason, "Negativity", isDefinitive, BanType.PLUGIN, banExpiration, cheat.getName());
	}
}
