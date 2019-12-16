package com.elikill58.negativity.universal.translation;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Provides messages, usually from a specific language.
 */
public abstract class TranslationProvider {

	/**
	 * Returns a message of a single line.
	 * @param key the key of the requested message
	 */
	@Nullable
	public abstract String get(String key);

	/**
	 * Returns a message of one or more lines.
	 * @param key the key of the requested message
	 */
	@Nullable
	public abstract List<String> getList(String key);
}
