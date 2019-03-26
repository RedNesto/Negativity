package com.elikill58.negativity.sponge;

import com.elikill58.negativity.sponge.utils.Utils;
import com.elikill58.negativity.universal.TranslatedMessages;
import com.elikill58.negativity.universal.adapter.Adapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class Messages {

	public static String getMessage(String dir, String... placeholders) {
		String message = "";
		try {
			message = TranslatedMessages.getStringFromLang(TranslatedMessages.getLang(), dir);
			if (message.equalsIgnoreCase(""))
				return dir;
		} catch (NullPointerException e) {
			System.out.println("Unknow ! default: " + Adapter.getAdapter().getStringInConfig("Translation.default") + " Get: " + TranslatedMessages.getLang());
		}
		for (int index = 0; index <= placeholders.length - 1; index += 2)
			message = message.replaceAll(placeholders[index], placeholders[index + 1]);
		return Utils.coloredMessage("&r" + message);
	}

	public static Text getMessage(Player p, String dir, String... placeholders) {
		return Text.of(getStringMessage(p, dir, placeholders));
	}

	public static String getStringMessage(Player p, String dir, String... placeholders) {
		String message = TranslatedMessages.getStringFromLang((p != null ? TranslatedMessages.getLang(SpongeNegativityPlayer.getNegativityPlayer(p)) : TranslatedMessages.getLang()), dir);
		for (int index = 0; index <= placeholders.length - 1; index += 2)
			message = message.replaceAll(placeholders[index], placeholders[index + 1]);
		if (message.equalsIgnoreCase("&rnull"))
			return dir;
		return Utils.coloredMessage("&r" + message);
	}

	public static void sendMessage(Player p, String dir, String... placeholders) {
		try {
			p.sendMessage(getMessage(p, dir, placeholders));
		} catch (Exception e) {
			Sponge.getServer().getBroadcastChannel().send(Text.builder("[Negativity] " + dir + " not found. (Code error: " + e.getMessage() + ")").color(TextColors.RED).build());
		}
	}

	public static void sendMessageList(Player p, String dir, String... placeholders) {
		for (String s : TranslatedMessages.getStringListFromLang(TranslatedMessages.getLang(SpongeNegativityPlayer.getNegativityPlayer(p)), dir)) {
			for (int index = 0; index <= placeholders.length - 1; index += 2)
				s = s.replaceAll(placeholders[index], placeholders[index + 1]);
			p.sendMessage(Text.of(Utils.coloredMessage(s)));
		}
	}

	public static void broadcastMessageList(String dir, String... placeholders) {
		for (Player p : Utils.getOnlinePlayers())
			sendMessageList(p, dir, placeholders);
	}
}
