package com.elikill58.negativity.universal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.utils.UniversalUtils;

public class Stats {

	private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

	public static final String SITE = "https://eliapp.fr/", SITE_UPDATE = "https://api.eliapp.fr/";
	static final String SITE_FILE = SITE_UPDATE + "negativity.php";
	public static boolean STATS_IN_MAINTENANCE = false;
	private static final AtomicBoolean LOADING_STATS = new AtomicBoolean(false);
	private static boolean statsLoaded = false;

    public static void updateStats(StatsType type, String... value) {
    	String post = "&";
    	switch (type) {
		case BAN:
			post = "&value=" + value[0];
			break;
		case CHEAT:
			post = "&hack=" + value[0] + "&reliability=" + value[1] + "&comment=" + value[2];
			break;
		case ONLINE:
			post = "&value=" + value[0];
			break;
		case PORT:
			post = "&value=" + value[0];
			break;
		}
    	sendUpdateStats(type, "platform=" + Adapter.getAdapter().getName() + "&type=" + type.getKey() + post);
    }
    
	private static void sendUpdateStats(StatsType type, String post) {
		if (STATS_IN_MAINTENANCE || !Adapter.getAdapter().canSendStats()) {
			return;
		}

		Runnable task = () -> {
			try {
				URLConnection conn = (HttpsURLConnection) new URL(SITE_FILE).openConnection();
				UniversalUtils.doTrustToCertificates();
				conn.setDoOutput(true);
				OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
				writer.write(post);//"platform=" + Adapter.getAdapter().getName() + "&type=" + type.getKey() + "&value=" + value + more);
				writer.flush();
				writer.close();
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String respons = "", end = "";
				while ((respons = br.readLine()) != null)
					end += respons;
				if (!end.equalsIgnoreCase("")) {
					Adapter.getAdapter().log(
							"Error while updating stats. Please, report this to Elikill58 (Mail: arpetzouille@gmail.com | Discord: @Elikill58#0743 | Twitter: @Elikill58 / @elinegativity");
					Adapter.getAdapter().log(end);
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		try {
			if (!statsLoaded) {
				loadStats().thenAcceptAsync(statsServerOnline -> {
					if (statsServerOnline) {
						task.run();
					}
				}, THREAD_POOL);
			} else {
				THREAD_POOL.submit(task);
			}
		} catch (RejectedExecutionException e) {
			Adapter.getAdapter().error("Could not update stats: " + e.getMessage());
		}
	}

	/**
	 * Connects to the stats server for the first time, checking whether it is available.
	 * <p>
	 * The returned future's completion value is {@code true} if
	 * stats collection is available server-side, {@code false} otherwise.
	 */
	public static CompletableFuture<Boolean> loadStats() {
		if (LOADING_STATS.get()) {
			return CompletableFuture.completedFuture(!STATS_IN_MAINTENANCE);
		}

		Supplier<Boolean> task = () -> {
			try {
				StringBuilder result = new StringBuilder();
				URL url = new URL(SITE_UPDATE + "status.php?plateforme=negativity");
				UniversalUtils.doTrustToCertificates();
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line;
				while ((line = rd.readLine()) != null)
					result.append(line);
				rd.close();
				STATS_IN_MAINTENANCE = result.toString().equalsIgnoreCase("on") ? false : true;
				if (STATS_IN_MAINTENANCE)
					Adapter.getAdapter().log("Website is in maintenance mode.");
			} catch (SSLHandshakeException e) {
				STATS_IN_MAINTENANCE = true;
				Adapter.getAdapter().warn("Error while loading Stats for Negativity.");
			} catch (Exception e) {
				e.printStackTrace();
			}
			LOADING_STATS.set(false);
			statsLoaded = true;
			return !STATS_IN_MAINTENANCE;
		};
		CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(task, THREAD_POOL);
		LOADING_STATS.set(true);
		return future;
	}
	
	public static enum StatsType {
		ONLINE("online"), PORT("port"), CHEAT("cheat"), BAN("ban");

		private String key;

		private StatsType(String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}
	}
}
