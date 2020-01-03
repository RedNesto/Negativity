package com.elikill58.negativity.bungee;

import java.util.List;

import javax.annotation.Nullable;

import com.elikill58.negativity.universal.translation.TranslationProvider;

import net.md_5.bungee.config.Configuration;

public class BungeeTranslationProvider extends TranslationProvider {

	private final Configuration msgConfig;

	public BungeeTranslationProvider(Configuration msgConfig) {
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
