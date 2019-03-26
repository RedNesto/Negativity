package com.elikill58.negativity.universal;

import com.elikill58.negativity.universal.ban.Ban;
import com.elikill58.negativity.universal.permissions.Perm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class UniversalUtils {

	public static boolean isMe(String uuid) {
		return uuid.equals("195dbcbc-9f2e-389e-82c4-3d017795ca65") || uuid.equals("3437a701-efaf-49d5-95d4-a8814e67760d");
	}

	public static boolean isMe(UUID uuid) {
		return isMe(uuid.toString());
	}

	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public static boolean isLong(String s) {
		try {
			Long.parseLong(s);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public static boolean hasInternet() {
		try {
			URL url = new URL("http://www.google.com");
			url.openConnection();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isLatestVersion(Optional<String> version) {
		if(!version.isPresent())
			return false;
		Optional<String> optVer = getLatestVersion();
		if (optVer.isPresent())
			return version.get().equalsIgnoreCase(optVer.get());
		else
			return false;
	}

	public static boolean getFromBoolean(String s) {
		return s.toLowerCase().contains("true") || s.equalsIgnoreCase("true") || s.toLowerCase().contains("vrai")
				|| s.equalsIgnoreCase("vrai");
	}

	public static boolean isBoolean(String s) {
		return s.toLowerCase().contains("true") || s.equalsIgnoreCase("true") || s.toLowerCase().contains("vrai")
				|| s.equalsIgnoreCase("vrai");
	}

	public static Optional<String> getLatestVersion() {
		try {
			URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=48399");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			/*connection.setConnectTimeout(5);
			connection.setReadTimeout(5);*/
			connection.setUseCaches(true);
			connection.setDoOutput(true);
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String content = "";
			String input;
			while ((input = br.readLine()) != null)
				content = content + input;
			br.close();
			return Optional.of(content);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public static boolean statsServerOnline() {
		try {
			URL url = new URL(Stats.SITE);
			url.openConnection();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static void init() {
		DefaultConfigValue.init();
		Database.init();
		Perm.init();
		Ban.init();
		SuspectManager.init();
		TranslatedMessages.init();
	}

	public static OS os = null;

	public static OS getOs() {
		if (os == null)
			os = OS.OTHER.getOs();
		return os;
	}

	public enum OS {
		WINDOWS(StandardCharsets.ISO_8859_1), MAC(StandardCharsets.UTF_16), LINUX(StandardCharsets.UTF_8), SOLARIS(
				StandardCharsets.UTF_8), OTHER(StandardCharsets.UTF_16);

		private Charset ch;

		OS(Charset ch) {
			this.ch = ch;
		}

		public Charset getCharset() {
			return ch;
		}

		protected OS getOs() {
			String os = System.getProperty("os.name").toLowerCase();
			if (isWindows(os))
				return WINDOWS;
			else if (isMac(os))
				return MAC;
			else if (isUnix(os))
				return LINUX;
			else if (isSolaris(os))
				return SOLARIS;
			else
				return OTHER;
		}

		private boolean isWindows(String OS) {
			return (OS.indexOf("win") >= 0);
		}

		private boolean isMac(String OS) {
			return (OS.indexOf("mac") >= 0);
		}

		private boolean isUnix(String OS) {
			return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
		}

		private boolean isSolaris(String OS) {
			return (OS.indexOf("sunos") >= 0);
		}
	}

	public static Optional<Integer> parseToInt(String s){
		try {
			int hour = 0, min = 0, month = 0, year = 0, day = 0;
			for(String spliter : Arrays.asList("m", "h", "d", "mo", "y")) {
				if(!Arrays.asList(s.split("")).contains(spliter))
					continue;
				String[] args = s.split("");
				int slot = 0;
				String caracter = args[0], i = "";
				while(isInteger(caracter) || !caracter.equalsIgnoreCase(spliter)) {
					if(isInteger(caracter))
						i += caracter;
					else if(!caracter.equalsIgnoreCase(spliter))
						i = "";
					slot++;
					caracter = args[slot];
				}
				int time = Integer.parseInt(i);
				switch(spliter) {
				case "m":
					min = time;
					break;
				case "mo":
					month = time;
					break;
				case "h":
					hour = time;
					break;
				case "y":
					year = time;
					break;
				case "d":
					day = time;
					break;
				}
			}
			return Optional.of(min * 60 + hour * 3600 + day * 3600 * 24 + month * 3600 * 24 * 30 + year * 3600 * 24 * 365);
		} catch (Exception e) {
			return Optional.empty();
		}
	}
}
