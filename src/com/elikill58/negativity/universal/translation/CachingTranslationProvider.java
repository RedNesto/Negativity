package com.elikill58.negativity.universal.translation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * A {@link TranslationProvider} that caches results returned from its wrapped provider.
 */
public class CachingTranslationProvider extends TranslationProvider {

	private final Map<String, String> cachedMessages = new HashMap<>();
	private final Map<String, List<String>> cachedMessageLists = new HashMap<>();

	private final TranslationProvider backingProvider;

	public CachingTranslationProvider(TranslationProvider backingProvider) {
		this.backingProvider = backingProvider;
	}

	@Nullable
	@Override
	public String get(String key) {
		return cachedMessages.computeIfAbsent(key, backingProvider::get);
	}

	@Nullable
	@Override
	public List<String> getList(String key) {
		return cachedMessageLists.computeIfAbsent(key, msgKey -> {
			List<String> messageList = backingProvider.getList(msgKey);
			if (messageList == null || messageList.isEmpty()) {
				return null;
			}
			return messageList;
		});
	}
}
