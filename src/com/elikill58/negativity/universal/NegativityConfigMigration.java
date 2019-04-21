package com.elikill58.negativity.universal;

import com.elikill58.negativity.universal.adapter.Adapter;

public class NegativityConfigMigration {

	/**
	 * This is not a 'true' migration as in 'updates the configuration file',
	 * it just updates the adapter's configuration.
	 */
	public static void migrateConfig(Adapter adapter) {
		if (!adapter.containsConfigValue("ban.log_storage")) {
			String banStorageId = adapter.getStringInConfig("ban.storage");
			adapter.set("ban.log_storage", banStorageId);
		}

		if (!adapter.containsConfigValue("ban.log_bans")) {
			boolean destroyWhenUnban = adapter.getBooleanInConfig("ban.destroy_when_unban");
			adapter.set("ban.log_bans", !destroyWhenUnban);
		}

		if (!adapter.containsConfigValue("ban.file.logs_dir")) {
			String banFileDir = adapter.getStringInConfig("ban.file.dir");
			if (!banFileDir.endsWith("/"))
				banFileDir += "/";

			adapter.set("ban.file.logs_dir", banFileDir + "logs");
		}
	}
}
