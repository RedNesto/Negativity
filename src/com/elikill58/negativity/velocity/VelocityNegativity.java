package com.elikill58.negativity.velocity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;

import com.elikill58.negativity.universal.Database;
import com.elikill58.negativity.universal.Stats;
import com.elikill58.negativity.universal.Stats.StatsType;
import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.adapter.VelocityAdapter;
import com.elikill58.negativity.universal.utils.UniversalUtils;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

@Plugin(id = "negativity", name = "VelocityNegativity", version = "1.4.1", authors = "Elikill58")
public class VelocityNegativity {

	public static final LegacyChannelIdentifier NEGATIVITY_CHANNEL_ID = new LegacyChannelIdentifier(UniversalUtils.CHANNEL_NEGATIVITY);
	public static final LegacyChannelIdentifier NEGATIVITY_MOD_CHANNEL_ID = new LegacyChannelIdentifier(UniversalUtils.CHANNEL_NEGATIVITY_MOD);

	private static VelocityNegativity instance;

	private final Logger logger;

	private final Path dataDir;
	private final Path configFile;
	private ConfigurationNode configNode;

	private final ProxyServer proxyServer;

	@Inject
	private VelocityNegativity(Logger logger, @DataDirectory Path dataDir, ProxyServer proxyServer) {
		instance = this;

		this.logger = logger;
		this.dataDir = dataDir;
		this.configFile = dataDir.resolve("config.yml");
		this.proxyServer = proxyServer;
		proxyServer.getChannelRegistrar().register(NEGATIVITY_CHANNEL_ID);
		proxyServer.getChannelRegistrar().register(NEGATIVITY_MOD_CHANNEL_ID);

		loadConfiguration();

		Adapter.setAdapter(new VelocityAdapter(this));
		UniversalUtils.init();
		// TODO support stats for velocity
		//Stats.loadStats();
		//Stats.updateStats(StatsType.ONLINE, "1");
	}

	@Subscribe
	public void onInit(ProxyInitializeEvent event) {
		proxyServer.getEventManager().register(this, new VelocityListener(this));
	}

	@Subscribe
	public void onStop(ProxyShutdownEvent event) {
		Database.close();
		Stats.updateStats(StatsType.ONLINE, "0");
	}

	public void loadConfiguration() {
		try {
			if (Files.notExists(configFile)) {
				Files.createDirectories(dataDir);
				try (InputStream inputStream = getClass().getResourceAsStream("/velocity/" + configFile.getFileName())) {
					Files.copy(inputStream, configFile);
				}
			}

			YAMLConfigurationLoader loader = YAMLConfigurationLoader.builder().setPath(configFile).build();
			configNode = loader.load();
		} catch (IOException e) {
			logger.error("Could not load configuration", e);
		}
	}

	public Logger getLogger() {
		return logger;
	}

	public Path getDataDir() {
		return dataDir;
	}

	public ConfigurationNode getConfigNode() {
		return configNode;
	}

	public ProxyServer getProxyServer() {
		return proxyServer;
	}

	public static VelocityNegativity getInstance() {
		return instance;
	}
}
