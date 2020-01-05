package com.elikill58.negativity.universal.translation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.annotation.Nullable;

public class ResourceBundleTranslationProvider extends BaseNegativityTranslationProvider {

	private final ResourceBundle bundle;

	public ResourceBundleTranslationProvider(ResourceBundle bundle) {
		this.bundle = bundle;
	}

	@Nullable
	@Override
	public String get(String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException ignore) {
		}
		return null;
	}

	@Nullable
	@Override
	public List<String> getList(String key) {
		List<String> lines = new ArrayList<>();
		try {
			// Arbitrary limit, should be enough for all cases
			for (int i = 0; i < 32; i++) {
				lines.add(bundle.getString(key + '.' + i));
			}
		} catch (MissingResourceException ignore) {
		}
		return lines;
	}

	@Override
	public String applyPlaceholders(String raw, Object... placeholders) {
		if (!raw.contains("{0}")) {
			return super.applyPlaceholders(raw, placeholders);
		}

		// Collects every placeholders values
		Object[] formatPlaceholders = new Object[placeholders.length / 2];
		for (int i = 0; i < formatPlaceholders.length; i++) {
			formatPlaceholders[i] = placeholders[i * 2 + 1];
		}
		return MessageFormat.format(raw, formatPlaceholders);
	}
}
