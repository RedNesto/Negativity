package com.elikill58.negativity.universal.adapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.DefaultConfigValue;
import com.elikill58.negativity.universal.NegativityAccount;
import com.elikill58.negativity.universal.NegativityPlayer;
import com.elikill58.negativity.universal.ReportType;
import com.elikill58.negativity.universal.TranslatedMessages;
import com.elikill58.negativity.velocity.NegativityCommandSource;
import com.elikill58.negativity.velocity.NegativityTypeTokens;
import com.elikill58.negativity.velocity.VelocityNegativity;
import com.elikill58.negativity.velocity.VelocityNegativityPlayer;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

public class VelocityAdapter extends Adapter {

	private final VelocityNegativity plugin;

	private final Map<String, ConfigurationNode> languages = new HashMap<>();
	private final LoadingCache<UUID, NegativityAccount> accountCache = CacheBuilder.newBuilder()
			.expireAfterAccess(10, TimeUnit.MINUTES)
			.build(new NegativityAccountLoader());

	public VelocityAdapter(VelocityNegativity plugin) {
		this.plugin = plugin;
	}

	@Override
	public String getName() {
		return "velocity";
	}

	@Override
	public Object getConfig() {
		return plugin.getConfigNode();
	}

	@Override
	public File getDataFolder() {
		return plugin.getDataDir().toFile();
	}

	@Override
	public String getStringInConfig(String dir) {
		Object[] path = dir.split("\\.");
		String value;
		try {
			value = plugin.getConfigNode().getNode(path).getString();
			if (value != null) {
				return value;
			}
		} catch (Exception ignored) {
		}
		return DefaultConfigValue.getDefaultValueString(dir);
	}

	@Override
	public boolean getBooleanInConfig(String dir) {
		Object[] path = dir.split("\\.");
		try {
			return plugin.getConfigNode().getNode(path).getBoolean();
		} catch (Exception ignored) {
		}
		return DefaultConfigValue.getDefaultValueBoolean(dir);
	}

	@Override
	public int getIntegerInConfig(String dir) {
		Object[] path = dir.split("\\.");
		try {
			return plugin.getConfigNode().getNode(path).getInt();
		} catch (Exception ignored) {
		}
		return DefaultConfigValue.getDefaultValueInt(dir);
	}

	@Override
	public double getDoubleInConfig(String dir) {
		Object[] path = dir.split("\\.");
		try {
			return plugin.getConfigNode().getNode(path).getDouble();
		} catch (Exception ignored) {
		}
		return DefaultConfigValue.getDefaultValueDouble(dir);
	}

	@Override
	public List<String> getStringListInConfig(String dir) {
		Object[] path = dir.split("\\.");
		try {
			return plugin.getConfigNode().getNode(path).getList(NegativityTypeTokens.STRING);
		} catch (Exception ignored) {
		}
		return Collections.emptyList();
	}

	@Override
	public HashMap<String, String> getKeysListInConfig(String dir) {
		Object[] path = dir.split("\\.");
		final HashMap<String, String> hash = new HashMap<>();
		try {
			plugin.getConfigNode().getNode(path).getChildrenMap().forEach((obj, cn) -> {
				hash.put(obj.toString(), cn.getString());
			});
		} catch (Exception ignored) {
		}
		return hash;
	}

	@Override
	public String getStringInOtherConfig(Path relativeFile, String key, String defaultValue) {
		Path filePath = plugin.getDataDir().resolve(relativeFile);
		if (Files.notExists(filePath))
			return defaultValue;

		try {
			ConfigurationNode node = loadHoconFile(filePath);
			String[] parts = key.split("\\.");
			for (String s : parts)
				node = node.getNode(s);

			return node.getString(defaultValue);
		} catch (IOException ignored) {
		}

		return defaultValue;
	}

	@Override
	public File copy(String lang, File f) {
		return copy(lang, f.toPath()).toFile();
	}

	public Path copy(String lang, Path filePath) {
		String fileName = "en_US.yml";
		String lowercaseLang = lang.toLowerCase();
		if (lowercaseLang.contains("fr") || lowercaseLang.contains("be"))
			fileName = "fr_FR.yml";
		else if (lowercaseLang.contains("pt") || lowercaseLang.contains("br"))
			fileName = "pt_BR.yml";
		else if (lowercaseLang.contains("no"))
			fileName = "no_NO.yml";
		else if (lowercaseLang.contains("ru"))
			fileName = "ru_RU.yml";
		else if (lowercaseLang.contains("zh") || lowercaseLang.contains("cn"))
			fileName = "zh_CN.yml";
		else if (lowercaseLang.contains("de"))
			fileName = "de_DE.yml";
		else if (lowercaseLang.contains("nl"))
			fileName = "nl_NL.yml";
		else if (lowercaseLang.contains("sv"))
			fileName = "sv_SV.yml";

		if (Files.notExists(filePath)) {
			try (InputStream inputStream = getClass().getResourceAsStream("/velocity/" + fileName)) {
				if (inputStream == null) {
					plugin.getLogger().error("Could not find built-in file /velocity/{}.", fileName);
					return filePath;
				}

				Path parentDir = filePath.normalize().getParent();
				if (parentDir != null) {
					Files.createDirectories(parentDir);
				}

				Files.copy(inputStream, filePath);
			} catch (IOException e) {
				plugin.getLogger().error("Failed to copy default language file {}.", fileName, e);
			}
		}

		return filePath;
	}

	private ConfigurationNode loadHoconFile(Path filePath) throws IOException {
		return HoconConfigurationLoader.builder().setPath(filePath).build().load();
	}

	@Override
	public void loadLang() {
		Path messagesDir = plugin.getDataDir().resolve("messages");
		try {
			for (String lang : TranslatedMessages.LANGS) {
				ConfigurationNode langNode = YAMLConfigurationLoader.builder()
						.setPath(copy(lang, messagesDir.resolve(lang + ".yml")))
						.build().load();
				languages.put(lang, langNode);
			}
		} catch (IOException e) {
			plugin.getLogger().error("Failed to copy default language files", e);
		}
	}

	@Override
	public void log(String msg) {
		plugin.getLogger().info(msg);
	}

	@Override
	public void warn(String msg) {
		plugin.getLogger().warn(msg);
	}

	@Override
	public void error(String msg) {
		plugin.getLogger().error(msg);
	}

	@Override
	public void set(String dir, Object value) {
		Object[] path = dir.split("\\.");
		plugin.getConfigNode().getNode(path).setValue(value);
	}

	@Override
	public String getStringFromLang(String lang, String key) {
		Object[] path = key.split("\\.");
		return languages.get(lang).getNode(path).getString(key);
	}

	@Override
	public List<String> getStringListFromLang(String lang, String key) {
		Object[] path = key.split("\\.");
		try {
			return languages.get(lang).getNode(path).getList(NegativityTypeTokens.STRING);
		} catch (ObjectMappingException e) {
			return Collections.emptyList();
		}
	}

	@Override
	public List<Cheat> getAbstractCheats() {
		return Collections.emptyList();
	}

	@Override
	public void reload() {
	}

	@Override
	public Object getItem(String itemName) {
		return null;
	}

	@Override
	public String getVersion() {
		return plugin.getProxyServer().getVersion().getVersion();
	}

	@Override
	public void reloadConfig() {
	}

	@Override
	public void alertMod(ReportType type, Object p, Cheat c, int reliability, String proof, String hover_proof) {
	}

	@Nonnull
	@Override
	public NegativityAccount getNegativityAccount(UUID playerId) {
		try {
			return accountCache.get(playerId);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	@Override
	public NegativityPlayer getNegativityPlayer(UUID playerId) {
		return VelocityNegativityPlayer.getNegativityPlayer(playerId);
	}

	@Override
	public void invalidateAccount(UUID playerId) {
		accountCache.invalidate(playerId);
	}

	@Override
	public void runConsoleCommand(String cmd) {
		plugin.getProxyServer().getCommandManager().execute(NegativityCommandSource.INSTANCE, cmd);
	}

	private static class NegativityAccountLoader extends CacheLoader<UUID, NegativityAccount> {
		@Override
		public NegativityAccount load(UUID playerId) {
			NegativityAccount account = new NegativityAccount(playerId, TranslatedMessages.getLang(playerId), false, new ArrayList<>());
			account.loadBanRequest();
			return account;
		}
	}
}
