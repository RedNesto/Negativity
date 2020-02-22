package com.elikill58.negativity.universal.dataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.elikill58.negativity.universal.NegativityAccount;
import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.dataStorage.database.DatabaseNegativityAccountStorage;

public abstract class NegativityAccountStorage {

	private static final Map<String, NegativityAccountStorage> storages = new HashMap<>();
	private static String storageId;

	private static boolean proxySync = false;

	@Nullable
	public abstract NegativityAccount loadAccount(UUID playerId);

	public abstract void saveAccount(NegativityAccount account);

	public NegativityAccount getOrCreateAccount(UUID playerId) {
		NegativityAccount existingAccount = loadAccount(playerId);
		if (existingAccount != null) {
			return existingAccount;
		}

		NegativityAccount createdAccount = new NegativityAccount(playerId);
		saveAccount(createdAccount);
		return createdAccount;
	}

	/**
	 * @return the storage to use, usually the one selected in the configuration.
	 * 		May be null if accounts are not persistent or are synced with the proxy.
	 */
	@Nullable
	public static NegativityAccountStorage getStorage() {
		if (proxySync) {
			return null;
		}
		return storages.get(storageId);
	}

	public static void register(String id, NegativityAccountStorage storage) {
		storages.put(id, storage);
	}

	public static String getStorageId() {
		return storageId;
	}

	public static void setStorageId(String storageId) {
		NegativityAccountStorage.storageId = storageId;
	}

	public static boolean isProxySync() {
		return proxySync;
	}

	public static void setProxySync(boolean proxySync) {
		NegativityAccountStorage.proxySync = proxySync;
	}

	public static void init() {
		Adapter adapter = Adapter.getAdapter();
		storageId = adapter.getStringInConfig("accounts.storage.id");
		NegativityAccountStorage.register("database", new DatabaseNegativityAccountStorage());
	}
}
