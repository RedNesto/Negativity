package com.elikill58.negativity.universal.translation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.annotation.Nullable;

public class ResourceBundleTranslationProvider implements TranslationProvider {

	private final ResourceBundle bundle;
	@Nullable
	private final Locale locale;

	public ResourceBundleTranslationProvider(ResourceBundle bundle, @Nullable Locale locale) {
		this.bundle = bundle;
		this.locale = locale;
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
		MessageFormat message;
		if (locale != null) {
			message = new MessageFormat(raw, locale);
		} else {
			message = new MessageFormat(raw);
		}
		return message.format(placeholders);
	}
}
