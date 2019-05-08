package com.elikill58.negativity.tests;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.NegativityAccount;
import com.elikill58.negativity.universal.NegativityConfig;
import com.elikill58.negativity.universal.NegativityPlayer;
import com.elikill58.negativity.universal.ReportType;
import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.ban.storage.FileLoggedBanStorage;

public class DummyTestAdapter extends Adapter {

	private NegativityConfig config = new NegativityConfig(new FileLoggedBanStorage.Config(null));

	@Override
	public String getName() {
		return "test";
	}

	@Override
	public NegativityConfig getConfig() {
		return config;
	}

	@Override
	public File getDataFolder() {
		return null;
	}

	@Override
	public String getStringInConfig(String dir) {
		return null;
	}

	@Override
	public boolean getBooleanInConfig(String dir) {
		return false;
	}

	@Override
	public int getIntegerInConfig(String dir) {
		return 0;
	}

	@Override
	public double getDoubleInConfig(String dir) {
		return 0;
	}

	@Override
	public List<String> getStringListInConfig(String dir) {
		return null;
	}

	@Override
	public HashMap<String, String> getKeysListInConfig(String dir) {
		return null;
	}

	@Override
	public String getStringInOtherConfig(String fileDir, String valueDir, String fileName) {
		return null;
	}

	@Override
	public boolean containsConfigValue(String dir) {
		return false;
	}

	@Override
	public File copy(String lang, File f) {
		return null;
	}

	@Override
	public void log(String msg) {
		System.out.println("info: " + msg);
	}

	@Override
	public void warn(String msg) {
		System.out.println("warn: " + msg);
	}

	@Override
	public void error(String msg) {
		System.out.println("error: " + msg);

	}

	@Override
	public void set(String dir, Object value) {
	}

	@Override
	public void loadLang() {
	}

	@Override
	public String getStringFromLang(String lang, String key) {
		return null;
	}

	@Override
	public List<String> getStringListFromLang(String lang, String key) {
		return null;
	}

	@Override
	public void reload() {
	}

	@Override
	public Object getItem(String itemName) {
		return null;
	}

	@Override
	public String getVersion() {
		return null;
	}

	@Override
	public void reloadConfig() {
	}

	@Nonnull
	@Override
	public NegativityAccount getNegativityAccount(UUID playerId) {
		return null;
	}

	@Nullable
	@Override
	public NegativityPlayer getNegativityPlayer(UUID playerId) {
		return null;
	}

	@Override
	public void invalidateAccount(UUID playerId) {
	}

	@Override
	public void alertMod(ReportType type, Object p, Cheat c, int reliability, String proof, String hover_proof) {
	}
}
