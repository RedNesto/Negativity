package com.elikill58.negativity.universal.ban.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.elikill58.negativity.universal.adapter.Adapter;

public class BanStorageManager {

	private static final Map<String, LoggedBanStorage> LOG_STORAGES = new HashMap<>();
	private static final Map<String, ActiveBanStorage> BAN_STORAGES = new HashMap<>();

	/**
	 * Tries to register the given {@link ActiveBanStorage}. Registration may fail if a storage with the given ID is already registered.
	 *
	 * @param id the ID used to identify the storage to register
	 * @param storage the storage to register
	 *
	 * @return {@code true} if the storage has been successfully registered, {@code false} otherwise.
	 */
	public static boolean registerStorage(String id, ActiveBanStorage storage) {
		if (BAN_STORAGES.containsKey(id)) {
			return false;
		}

		BAN_STORAGES.put(id, storage);
		return true;
	}

	/**
	 * Tries to register the given {@link LoggedBanStorage}. Registration may fail if a storage with the given ID is already registered.
	 *
	 * @param id the ID used to identify the storage to register
	 * @param storage the storage to register
	 *
	 * @return {@code true} if the storage has been successfully registered, {@code false} otherwise.
	 */
	public static boolean registerStorage(String id, LoggedBanStorage storage) {
		if (LOG_STORAGES.containsKey(id)) {
			return false;
		}

		LOG_STORAGES.put(id, storage);
		return true;
	}

	public static ActiveBanStorage getActiveBanStorage(String id) {
		return BAN_STORAGES.get(id);
	}

	public static LoggedBanStorage getLoggedBanStorage(String id) {
		return LOG_STORAGES.get(id);
	}

	public static Collection<String> getAvailableBanStorageIds() {
		return BAN_STORAGES.keySet();
	}

	public static Collection<String> getAvailableLogStorageIds() {
		return LOG_STORAGES.keySet();
	}

	public static void init() {
		registerStorage("file", new FileLoggedBanStorage(Adapter.getAdapter().getConfig().fileLoggedBanStorageConfig));
		registerStorage("database", new DatabaseLoggedBanStorage());

		registerStorage("file", new FileActiveBanStorage());
		registerStorage("database", new DatabaseActiveBanStorage());
	}
}
