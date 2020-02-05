package com.elikill58.negativity.universal;

import java.util.HashMap;

import com.elikill58.negativity.universal.adapter.Adapter;

public class DefaultConfigValue {

	public static final HashMap<String, Integer> INTS = new HashMap<>();
	public static final HashMap<String, String> STRINGS = new HashMap<>();
	public static final HashMap<String, Boolean> BOOLEANS = new HashMap<>();
	public static final HashMap<String, Double> DOUBLES = new HashMap<>();

	public static int getDefaultValueInt(String dir) {
		if(INTS.containsKey(dir))
			return INTS.get(dir);
		else {
			Adapter.getAdapter().warn("Unknow default int value: " + dir);
			return -1;
		}
	}

	public static String getDefaultValueString(String dir) {
		if(STRINGS.containsKey(dir))
			return STRINGS.get(dir);
		else {
			Adapter.getAdapter().warn("Unknow default string value: " + dir);
			return dir;
		}
	}

	public static boolean getDefaultValueBoolean(String dir) {
		if(BOOLEANS.containsKey(dir))
			return BOOLEANS.get(dir);
		else {
			Adapter.getAdapter().warn("Unknow default boolean value: " + dir);
			return false;
		}
	}

	public static double getDefaultValueDouble(String dir) {
		if(DOUBLES.containsKey(dir))
			return DOUBLES.get(dir);
		else {
			Adapter.getAdapter().warn("Unknow default double value: " + dir);
			return -1;
		}
	}

	public static void init() {
		BOOLEANS.clear();
		INTS.clear();
		DOUBLES.clear();
		STRINGS.clear();

		BOOLEANS.put("log_alerts", true);
		BOOLEANS.put("log_alerts_in_console", true);
		INTS.put("tps_alert_stop", 18);
		INTS.put("time_between_alert", 2000);
		BOOLEANS.put("report_command", true);
		BOOLEANS.put("ban_command", true);
		BOOLEANS.put("unban_command", true);
		BOOLEANS.put("kick_command", true);
		BOOLEANS.put("Database.isActive", true);

		STRINGS.put("Database.url", "127.0.0.1/myDb");
		STRINGS.put("Database.user", "root");
		STRINGS.put("Database.password", "myPassword");
		STRINGS.put("Database.table_perm", "myTable");
		STRINGS.put("Database.table_lang", "myTable");
		BOOLEANS.put("Database.saveInCache", true);

		BOOLEANS.put("inventory.alerts.see.no_started_verif_cheat", false);
		BOOLEANS.put("inventory.alerts.no_started_verif_cheat", false);
		BOOLEANS.put("inventory.alerts.see.only_cheat_active", true);
		BOOLEANS.put("inventory.alerts.only_cheat_active", true);
		BOOLEANS.put("inventory.inv_freeze_active", true);

		BOOLEANS.put("Permissions.defaultActive", true);
		BOOLEANS.put("Permissions.canBeHigher", false);

		STRINGS.put("Permissions.showAlert.default", "negativity.alert");
		STRINGS.put("Permissions.showAlert.custom", "MOD,ADMIN");
		STRINGS.put("Permissions.reload.default", "negativity.reload");
		STRINGS.put("Permissions.reload.custom", "MOD,ADMIN");
		STRINGS.put("Permissions.verif.default", "negativity.verif");
		STRINGS.put("Permissions.verif.custom", "MOD,ADMIN");
		STRINGS.put("Permissions.manageCheat.default", "negativity.managecheat");
		STRINGS.put("Permissions.manageCheat.custom", "MOD,ADMIN");
		STRINGS.put("Permissions.report_wait.default", "negativity.reportwait");
		STRINGS.put("Permissions.report_wait.custom", "negativity.reportwait");
		STRINGS.put("Permissions.notBanned.default", "negativity.notbanned");
		STRINGS.put("Permissions.notBanned.custom", "ADMIN");
		BOOLEANS.put("Permissions.bypass.active", false);

		String[] cheats = new String[] {"all", "forcefield", "fastplace", "speedhack", "autoclick", "fly", "antipotion", "autoeat", "autoregen", "antiknockback", "jesus", "nofall", "blink", "spider", "fastbow", "scaffold", "step", "noslowdown", "fastladders", "phase", "autosteal", "timer"};
		for(String localCheat : cheats) {
			//STRINGS.put("Permissions.bypass." + localCheat, "negativity.bypass." + localCheat);
			STRINGS.put("Permissions.bypass." + localCheat + ".default", "negativity.bypass." + localCheat);
			STRINGS.put("Permissions.bypass." + localCheat + ".custom", "ADMIN");
		}

		BOOLEANS.put("Translation.active", false);
		BOOLEANS.put("Translation.use_db", false);
		STRINGS.put("Translation.default", "en_US");
		STRINGS.put("Translation.provider", TranslatedMessages.PLATFORM_PROVIDER_ID);

		BOOLEANS.put("hasBungeecord", false);

		INTS.put("time_between_report", 1000);

		BOOLEANS.put("ban.active", false);
		STRINGS.put("ban.processor", "file");
		INTS.put("ban.reliability_need", 90);
		INTS.put("ban.alert_need", 5);
		STRINGS.put("ban.time.calculator", "360000000 + (%reliability% * 10 * %alert%)");
		INTS.put("ban.def.ban_time", 4);
		BOOLEANS.put("ban.file.log_bans", true);
		BOOLEANS.put("ban.database.log_bans", true);

		for(String lc : cheats) {
			INTS.put("cheats." + lc + ".ping", 150);
			STRINGS.put("cheats." + lc + ".exact_name", lc);
			BOOLEANS.put("cheats." + lc + ".isActive", true);
			INTS.put("cheats." + lc + ".reliability_alert", 60);
			BOOLEANS.put("cheats." + lc + ".autoVerif", true);
			BOOLEANS.put("cheats." + lc + ".setBack", false);
			BOOLEANS.put("cheats." + lc + ".kick", false);
			INTS.put("cheats." + lc + ".alert_kick", 5);
		}

		DOUBLES.put("cheats.forcefield.reach", 3.9);
		BOOLEANS.put("cheats.forcefield.ghost_disabled", false);
		INTS.put("cheats.autoclick.click_alert", 20);
	}
}
