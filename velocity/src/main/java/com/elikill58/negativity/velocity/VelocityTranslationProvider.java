package com.elikill58.negativity.velocity;

import java.util.List;

import javax.annotation.Nullable;

import com.elikill58.negativity.universal.translation.BaseNegativityTranslationProvider;

import net.md_5.bungee.config.Configuration;

public class VelocityTranslationProvider extends BaseNegativityTranslationProvider {

	private final Configuration msgConfig;

	public VelocityTranslationProvider(Configuration msgConfig) {
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
