package com.elikill58.negativity.spigot;

import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.configuration.file.YamlConfiguration;

import com.elikill58.negativity.universal.translation.TranslationProvider;

public class SpigotTranslationProvider extends TranslationProvider {

	private final YamlConfiguration msgConfig;

	public SpigotTranslationProvider(YamlConfiguration msgConfig) {
		this.msgConfig = msgConfig;
	}

	@Nullable
	@Override
	public String get(String key) {
		return msgConfig.getString(key);
	}

	@Nullable
	@Override
	public List<String> getList(String key) {
		return msgConfig.getStringList(key);
	}
}
