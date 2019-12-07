package com.elikill58.negativity.universal.adapter;

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.elikill58.negativity.spigot.SpigotNegativity;
import com.elikill58.negativity.spigot.SpigotNegativityPlayer;
import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.DefaultConfigValue;
import com.elikill58.negativity.universal.NegativityAccount;
import com.elikill58.negativity.universal.NegativityPlayer;
import com.elikill58.negativity.universal.ReportType;
import com.elikill58.negativity.universal.TranslatedMessages;
import com.elikill58.negativity.universal.utils.UniversalUtils;
import com.google.common.io.ByteStreams;

public class SpigotAdapter extends Adapter {

	private FileConfiguration config;
	private JavaPlugin pl;
	private final HashMap<String, YamlConfiguration> LANGS = new HashMap<>();
	private HashMap<UUID, NegativityAccount> account = new HashMap<>();
	/*private LoadingCache<UUID, NegativityAccount> accountCache = CacheBuilder.newBuilder()
			.expireAfterAccess(10, TimeUnit.MINUTES)
			.build(new NegativityAccountLoader());*/

	public SpigotAdapter(JavaPlugin pl, FileConfiguration config) {
		this.pl = pl;
		this.config = config;
	}

	@Override
	public String getName() {
		return "spigot";
	}

	@Override
	public Object getConfig() {
		return config;
	}

	@Override
	public File getDataFolder() {
		return pl.getDataFolder();
	}

	@Override
	public String getStringInConfig(String dir) {
		if (config.contains(dir))
			return config.getString(dir);
		return DefaultConfigValue.getDefaultValueString(dir);
	}

	@Override
	public void log(String msg) {
		pl.getLogger().info(msg);
	}

	@Override
	public void warn(String msg) {
		pl.getLogger().warning(msg);
	}

	@Override
	public void error(String msg) {
		pl.getLogger().severe(msg);
	}

	@Override
	public HashMap<String, String> getKeysListInConfig(String dir) {
		HashMap<String, String> list = new HashMap<>();
		ConfigurationSection cs = config.getConfigurationSection(dir);
		if (cs == null)
			return list;
		for (String s : cs.getKeys(false))
			list.put(s, config.getString(dir + "." + s));
		return list;
	}

	@Override
	public boolean getBooleanInConfig(String dir) {
		if (config.contains(dir))
			return config.getBoolean(dir);
		return DefaultConfigValue.getDefaultValueBoolean(dir);
	}

	@Override
	public int getIntegerInConfig(String dir) {
		if (config.contains(dir))
			return config.getInt(dir);
		return DefaultConfigValue.getDefaultValueInt(dir);
	}

	@Override
	public void set(String dir, Object value) {
		config.set(dir, value);
		SpigotNegativity.getInstance().saveConfig();
	}

	@Override
	public double getDoubleInConfig(String dir) {
		if (config.contains(dir))
			return config.getDouble(dir);
		return DefaultConfigValue.getDefaultValueDouble(dir);
	}

	@Override
	public List<String> getStringListInConfig(String dir) {
		return config.getStringList(dir);
	}

	@Override
	public String getStringInOtherConfig(Path relativeFile, String key, String defaultValue) {
		Path configFile = pl.getDataFolder().toPath().resolve(relativeFile);
		if (Files.notExists(configFile))
			return defaultValue;
		return YamlConfiguration.loadConfiguration(configFile.toFile()).getString(key, defaultValue);
	}

	@Override
	public File copy(String lang, File f) {
		if (f.exists())
			return f;
		String fileName = "en_US.yml";
		if (lang.toLowerCase().contains("fr") || lang.toLowerCase().contains("be"))
			fileName = "fr_FR.yml";
		else if (lang.toLowerCase().contains("pt") || lang.toLowerCase().contains("br"))
			fileName = "pt_BR.yml";
		else if (lang.toLowerCase().contains("no"))
			fileName = "no_NO.yml";
		else if (lang.toLowerCase().contains("ru"))
			fileName = "ru_RU.yml";
		else if (lang.toLowerCase().contains("zh") || lang.toLowerCase().contains("cn"))
			fileName = "zh_CN.yml";
		else if (lang.toLowerCase().contains("de"))
			fileName = "de_DE.yml";
		else if (lang.toLowerCase().contains("nl"))
			fileName = "nl_NL.yml";
		else if (lang.toLowerCase().contains("sv"))
			fileName = "sv_SV.yml";
		try (InputStream in = pl.getResource(fileName); OutputStream out = new FileOutputStream(f)) {
			ByteStreams.copy(in, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return f;
	}

	@Override
	public void loadLang() {
		File langDir = new File(pl.getDataFolder().getAbsolutePath() + File.separator + "lang" + File.separator);
		if (!langDir.exists())
			langDir.mkdirs();

		if (!TranslatedMessages.activeTranslation) {
			String defaultLang = TranslatedMessages.DEFAULT_LANG;
			LANGS.put(defaultLang, YamlConfiguration.loadConfiguration(copy(defaultLang, new File(langDir.getAbsolutePath() + "/" + defaultLang + ".yml"))));
			return;
		}

		try {
			for (String l : TranslatedMessages.LANGS)
				LANGS.put(l, YamlConfiguration
						.loadConfiguration(copy(l, new File(langDir.getAbsolutePath() + "/" + l + ".yml"))));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getStringFromLang(String lang, String key) {
		return LANGS.get(lang).getString(key);
	}

	@Override
	public List<String> getStringListFromLang(String lang, String key) {
		return LANGS.get(lang).getStringList(key);
	}

	@Override
	public void reload() {
		SpigotNegativity sn = SpigotNegativity.getInstance();
		sn.reloadConfig();
		UniversalUtils.init();
		Cheat.loadCheat();
		loadLang();
		SpigotNegativity.isOnBungeecord = getBooleanInConfig("hasBungeecord");
		SpigotNegativity.log = getBooleanInConfig("log_alerts");
		SpigotNegativity.log_console = getBooleanInConfig("log_alerts_in_console");
		SpigotNegativity.hasBypass = getBooleanInConfig("Permissions.bypass.active");
		//Bukkit.getScheduler().cancelAllTasks();
        /*Bukkit.getPluginManager().disablePlugin(sn);
        Bukkit.getPluginManager().enablePlugin(sn);*/
	}

	@Override
	public Object getItem(String itemName) {
		for(Material m : Material.values())
			if(m.name().equalsIgnoreCase(itemName))
				return m;
		return null;
	}

	@Override
	public String getVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
	}

	@Override
	public void reloadConfig() {
		SpigotNegativity.getInstance().reloadConfig();
	}

	@Nullable
	@Override
	public NegativityPlayer getNegativityPlayer(UUID playerId) {
		Player player = Bukkit.getPlayer(playerId);
		return player != null ? SpigotNegativityPlayer.getNegativityPlayer(player) : null;
	}

	@Override
	public void alertMod(ReportType type, Object p, Cheat c, int reliability, String proof, String hover_proof) {
		SpigotNegativity.alertMod(type, (Player) p, c, reliability, proof, hover_proof, "");
	}

	@Override
	public void runConsoleCommand(String cmd) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
	}

	@Override
	public boolean canSendStats() {
		return true;
	}

	@Nonnull
	@Override
	public NegativityAccount getNegativityAccount(UUID playerId) {
		NegativityAccount existingAccount = account.get(playerId);
		if (existingAccount != null) {
			return existingAccount;
		}

		NegativityAccount na = new NegativityAccount(playerId, TranslatedMessages.getLang(playerId), false, new ArrayList<>());
		account.put(playerId, na);
		return na;
	}

	@Override
	public void invalidateAccount(UUID playerId) {
		account.remove(playerId);
	}
}
