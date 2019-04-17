package com.elikill58.negativity.universal.ban;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.NegativityPlayer;
import com.elikill58.negativity.universal.adapter.Adapter;

public class Ban {

	public static File banDir;
	public static boolean banActive, banActiveIsFile;
	public static final HashMap<String, String> DB_CONTENT = new HashMap<>();

	public static void manageBan(Cheat cheat, NegativityPlayer np, int relia) {
		Adapter ada = Adapter.getAdapter();
		if (!cheat.isActive() || !ada.getBooleanInConfig("ban.active"))
			return;
		if (!(ada.getIntegerInConfig("ban.reliability_need") <= relia
				&& ada.getIntegerInConfig("ban.alert_need") <= np.getWarn(cheat)))
			return;
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("JavaScript");
		int i = -1;
		try {
			i = Integer.parseInt(engine.eval(
					ada.getStringInConfig("ban.time.calculator").replaceAll("%reliability%", String.valueOf(relia))
							.replaceAll("%alert%", String.valueOf(np.getWarn(cheat))))
					.toString());
		} catch (ScriptException e) {
			e.printStackTrace();
		}

		List<LoggedBan> loggedBans = BanManager.getLoggedBans(np.getAccount().getPlayerId());
		boolean isDefinitive = loggedBans.size() >= ada.getIntegerInConfig("ban.def.ban_time");
		BanManager.banPlayer(np.getAccount().getPlayerId(), "Cheat (" + cheat.getName() + ")", "Negativity", isDefinitive, BanType.PLUGIN, i + System.currentTimeMillis(), cheat.getName());
	}

	public static void init() {
		Adapter adapter = Adapter.getAdapter();
		banDir = new File(adapter.getDataFolder(), adapter.getStringInConfig("ban.file.dir"));
		if(!(banActive = adapter.getBooleanInConfig("ban.active")))
			return;
		String storage = adapter.getStringInConfig("ban.storage");
		if(storage == null) {
			adapter.log("Some line is missing in the configuration file. Please, remove it then restart your server to get all configuration line.");
			return;
		}
		if(storage.equalsIgnoreCase("file")) {
			banActiveIsFile = true;
		} else if(storage.equalsIgnoreCase("db") || storage.equalsIgnoreCase("database")) {
			banActiveIsFile = false;
		} else {
			adapter.error("Error while loading ban system. " + storage + " is an undefined storage type.");
			adapter.error("Please, write a good storage type in the configuration, then restart you server.");
			return;
		}
		if (banActiveIsFile)
			if (!banDir.exists())
				banDir.mkdirs();
		DB_CONTENT.putAll(adapter.getKeysListInConfig("ban.db.other"));
	}
}
