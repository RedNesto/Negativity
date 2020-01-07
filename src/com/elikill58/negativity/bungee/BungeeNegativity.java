package com.elikill58.negativity.bungee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;

import com.elikill58.negativity.universal.Database;
import com.elikill58.negativity.universal.Stats;
import com.elikill58.negativity.universal.Stats.StatsType;
import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.adapter.BungeeAdapter;
import com.elikill58.negativity.universal.pluginMessages.NegativityMessagesManager;
import com.elikill58.negativity.universal.utils.UniversalUtils;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeNegativity extends Plugin {

	public static Configuration CONFIG;
	private static BungeeNegativity instance;
	public static BungeeNegativity getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;
		
		new Metrics(this);
		enableConfig();
		getProxy().registerChannel(NegativityMessagesManager.CHANNEL_ID);
		getProxy().getPluginManager().registerListener(this, new NegativityListener());
		Adapter.setAdapter(new BungeeAdapter(this, CONFIG));
		UniversalUtils.init();
		Stats.loadStats();
		Stats.updateStats(StatsType.ONLINE, 1 + "");
		try {
			Stats.updateStats(StatsType.PORT, ((LinkedHashMap<?, ?>) ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder().getParentFile().getParentFile(), "config.yml"))
							.getList("listeners").get(0)).get("query_port") + "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		Database.close();
		Stats.updateStats(StatsType.ONLINE, 0 + "");
	}

	protected boolean enableConfig() {
		File folder = getDataFolder();
		folder.mkdir();
		File resourceFile = new File(folder, "config.yml");
		try {
			if (!resourceFile.exists()) {
				resourceFile.createNewFile();
				try (InputStream in = getResourceAsStream("bungee_config.yml");
						OutputStream out = new FileOutputStream(resourceFile)) {
					ByteStreams.copy(in, out);
				}
			}
			CONFIG = ConfigurationProvider.getProvider(YamlConfiguration.class).load(resourceFile);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
