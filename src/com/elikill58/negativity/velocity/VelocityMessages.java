package com.elikill58.negativity.velocity;

import com.elikill58.negativity.universal.TranslatedMessages;
import com.velocitypowered.api.proxy.Player;

import net.kyori.text.TextComponent;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;

public class VelocityMessages {

	public static TextComponent getMessage(String language, String key, String... placeholders) {
		String message = null;
		try {
			message = TranslatedMessages.getStringFromLang(language, key);
		} catch (Exception e) {
			// This is a workaround to avoid issues with the current TranslatedMessages methods
			// Fixing these would require a rewrite of the translations system
			// For now we handle the exception in following null-check
		}

		if (message == null) {
			System.out.println("Could not find the message " + key + " for language " + language);
			return TextComponent.of(key);
		}

		for (int index = 0; index <= placeholders.length - 1; index += 2) {
			message = message.replaceAll(placeholders[index], placeholders[index + 1]);
		}

		return LegacyComponentSerializer.legacy().deserialize(message, '&');
	}

	public static TextComponent getMessage(String key, String... placeholders) {
		return getMessage(TranslatedMessages.getDefaultLang(), key, placeholders);
	}

	public static TextComponent getMessage(Player player, String key, String... placeholders) {
		return getMessage(TranslatedMessages.getLang(player.getUniqueId()), key, placeholders);
	}

	public static void sendMessage(Player player, String key, String... placeholders) {
		player.sendMessage(getMessage(player, key, placeholders));
	}
}
