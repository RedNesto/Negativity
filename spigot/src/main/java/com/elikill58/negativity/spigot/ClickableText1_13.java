package com.elikill58.negativity.spigot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.elikill58.negativity.spigot.ClickableText.Action;
import com.elikill58.negativity.spigot.ClickableText.MessageComponent;
import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.DefaultConfigValue;
import com.elikill58.negativity.universal.NegativityAccount;
import com.elikill58.negativity.universal.NegativityPlayer;
import com.elikill58.negativity.universal.ReportType;
import com.elikill58.negativity.universal.TranslatedMessages;
import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.translation.CachingTranslationProvider;
import com.elikill58.negativity.universal.translation.TranslationProvider;
import com.elikill58.negativity.universal.translation.TranslationProviderFactory;
import com.elikill58.negativity.universal.utils.UniversalUtils;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ClickableText1_13 {

	public static void send(Player p, MessageComponent mc) {
		TextComponent text = new TextComponent(mc.text);
		if (mc.a == Action.SHOW_TEXT)
			text.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
					new ComponentBuilder(mc.data).create()));
		else if (mc.a == Action.SUGGEST_COMMAND)
			text.setClickEvent(
					new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND, mc.data));
		else if (mc.a == Action.OPEN_URL)
			text.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL, mc.data));
		else if (mc.a == Action.RUN_COMMAND)
			text.setClickEvent(
					new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, mc.data));

		if (mc.a2 == Action.OPEN_URL)
			text.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL, mc.data2));
		else if (mc.a2 == Action.RUN_COMMAND)
			text.setClickEvent(
					new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, mc.data2));
		p.spigot().sendMessage(text);
	}

}
