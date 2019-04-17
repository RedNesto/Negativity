package com.elikill58.negativity.universal.ban.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.elikill58.negativity.universal.UniversalUtils;
import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.ban.Ban;
import com.elikill58.negativity.universal.ban.BanRequest;

public class FileBanStorage implements BanStorage {

	@Override
	public List<BanRequest> load(UUID playerId) {
		List<BanRequest> loadedBans = new ArrayList<>();

		File banFile = new File(Ban.banDir.getAbsolutePath(), playerId + ".txt");
		if (!banFile.exists())
			return loadedBans;

		try {
			for (String line : Files.readAllLines(banFile.toPath(), UniversalUtils.getOs().getCharset()))
				loadedBans.add(fromString(playerId, line));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return loadedBans;
	}

	@Override
	public void save(BanRequest ban) {
		try {
			File f = new File(Ban.banDir, ban.getUUID() + ".txt");
			if (!f.exists())
				f.createNewFile();
			Files.write(f.toPath(),
					(ban.getExpirationTime() + ":reason=" + ban.getReason().replaceAll(":", "") + ":def=" + ban.isDef() + ":bantype="
							+ ban.getBanType().name() + ":ac=" + ban.getCheatName() + ":by=" + ban.getBy() + ":unban=" + ban.isUnban() + "\n").getBytes(),
					StandardOpenOption.APPEND);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static BanRequest fromString(UUID playerId, String line) {
		String reason = "";
		String by = "Negativity";
		boolean def = false;
		boolean isUnban = false;
		BanRequest.BanType banType = BanRequest.BanType.UNKNOW;
		long expirationTime;
		String ac = "unknown";
		String[] content = line.split(":");
		expirationTime = Long.valueOf(content[0]);
		for (String s : content) {
			String[] part = s.split("=", 2);
			if (part.length != 2)
				continue;
			String type = part[0], value = part[1];
			switch (type) {
				case "bantype":
					banType = BanRequest.BanType.valueOf(value.toUpperCase());
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

		return new BanRequest(playerId, reason, expirationTime, def, banType, ac, by, isUnban);
	}
}
