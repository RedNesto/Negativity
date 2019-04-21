package com.elikill58.negativity.universal.ban;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.NegativityPlayer;
import com.elikill58.negativity.universal.adapter.Adapter;

public class Ban {

	public static File banDir;
	public static File banLogsDir;
	public static boolean banActive;
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

		List<LoggedBan> loggedBans = BanManager.getLoggedBans(np.getPlayerId());
		boolean isDefinitive = loggedBans.size() >= ada.getIntegerInConfig("ban.def.ban_time");
		BanManager.banPlayer(np.getPlayerId(), "Cheat (" + cheat.getName() + ")", "Negativity", isDefinitive, BanType.PLUGIN, i + System.currentTimeMillis(), cheat.getName());
	}

	public static void init() {
		Adapter adapter = Adapter.getAdapter();
		banDir = new File(adapter.getDataFolder(), adapter.getStringInConfig("ban.file.dir"));
		banLogsDir = new File(adapter.getDataFolder(), adapter.getStringInConfig("ban.file.logs_dir"));
		if(!(banActive = adapter.getBooleanInConfig("ban.active")))
			return;

		loadStorages("ban.storage", BanManager.getAvailableBanStorageIds(), BanManager::setBanStorageId);
		loadStorages("ban.log_storage", BanManager.getAvailableLogStorageIds(), BanManager::setLogStorageId);

		BanManager.setLogBans(adapter.getBooleanInConfig("ban.log_bans"));

		DB_CONTENT.putAll(adapter.getKeysListInConfig("ban.db.other"));

		BansMigration.migrateBans();
	}

	private static void loadStorages(String propertyName, Collection<String> availableStorages, Consumer<String> storageSetter) {
		Adapter adapter = Adapter.getAdapter();
		String banStorage = adapter.getStringInConfig(propertyName);
		if (banStorage == null) {
			adapter.log("The property " + propertyName + " is missing from the configuration file. Please add it and restart the server.");
			return;
		}

		if (banStorage.equalsIgnoreCase("db"))
			banStorage = "database";

		if (!availableStorages.contains(banStorage)) {
			adapter.error("Error while loading ban system. '" + banStorage + "' is an unknown storage type.");
			adapter.error("Please set a valid storage type, then restart you server.");
			return;
		}

		storageSetter.accept(banStorage);
	}
}
