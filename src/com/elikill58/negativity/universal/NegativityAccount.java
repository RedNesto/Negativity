package com.elikill58.negativity.universal;

import java.util.UUID;

import com.elikill58.negativity.universal.utils.NonnullByDefault;

/**
 * Contains player-related data that can be accessed when the player is offline.
 */
@NonnullByDefault
public class NegativityAccount {

	private UUID playerId;
	private String lang;

	public NegativityAccount(UUID playerId) {
		this(playerId, TranslatedMessages.DEFAULT_LANG);
	}

	public NegativityAccount(UUID playerId, String lang) {
		this.playerId = playerId;
		this.lang = lang;
	}

	public String getUUID() {
		return this.playerId.toString();
	}

	public String getLang() {
		return lang;
	}

	public UUID getPlayerId() {
		return playerId;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}
}
