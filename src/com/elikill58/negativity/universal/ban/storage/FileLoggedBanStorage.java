package com.elikill58.negativity.universal.ban.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.elikill58.negativity.universal.UniversalUtils;
import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.ban.Ban;
import com.elikill58.negativity.universal.ban.BanType;
import com.elikill58.negativity.universal.ban.LoggedBan;

public class FileLoggedBanStorage implements LoggedBanStorage {

	@Override
	public List<LoggedBan> load(UUID playerId) {
		List<LoggedBan> loadedBans = new ArrayList<>();

		File banFile = new File(Ban.banDir.getAbsolutePath(), playerId + ".txt");
		if (!banFile.exists())
			return loadedBans;

		try {
			for (String line : Files.readAllLines(banFile.toPath(), UniversalUtils.getOs().getCharset())) {
				LoggedBan ban = fromString(playerId, line);
				if (ban != null)
					loadedBans.add(ban);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return loadedBans;
	}

	@Override
	public void save(LoggedBan ban) {
		try {
			File f = new File(Ban.banDir, ban.getPlayerId() + ".txt");
			if (!f.exists())
				f.createNewFile();
			Files.write(f.toPath(),
					(ban.getExpirationTime()
							+ ":reason=" + ban.getReason().replaceAll(":", "")
							+ ":def=" + ban.isDefinitive()
							+ ":bantype=" + ban.getBanType().name()
							+ (ban.getCheatName() != null ? ":ac=" + ban.getCheatName() : "")
							+ ":by=" + ban.getBannedBy()
							+ ":unban=" + ban.isRevoked() + "\n").getBytes(),
					StandardOpenOption.APPEND);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Nullable
	private static LoggedBan fromString(UUID playerId, String line) {
		String[] content = line.split(":");
		if (content.length == 1)
			return null;

		long expirationTime;
		try {
			expirationTime = Long.valueOf(content[0]);
		} catch (NumberFormatException e) {
			// This line is invalid
			return null;
		}

		String reason = "";
		String by = "Negativity";
		boolean def = false;
		boolean isUnban = false;
		BanType banType = BanType.UNKNOW;
		String ac = null;
		for (String s : content) {
			String[] part = s.split("=", 2);
			if (part.length != 2)
				continue;
			String type = part[0], value = part[1];
			switch (type) {
				case "bantype":
					banType = BanType.valueOf(value.toUpperCase());
					break;
				case "def":
					def = Boolean.valueOf(value);
					break;
				case "reason":
					reason = value;
					break;
				case "ac":
					ac = value;
					break;
				case "by":
					by = value;
					break;
				case "unban":
					isUnban = Boolean.valueOf(value);
					break;
				default:
					Adapter.getAdapter().warn("Type " + type + " unknow. Value: " + value);
					break;
			}
		}

		return new LoggedBan(playerId, reason, by, def, banType, expirationTime, ac, isUnban);
	}
}
