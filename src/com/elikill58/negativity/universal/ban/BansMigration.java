package com.elikill58.negativity.universal.ban;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.ban.storage.ActiveBanStorage;
import com.elikill58.negativity.universal.ban.storage.FileActiveBanStorage;
import com.elikill58.negativity.universal.ban.storage.FileLoggedBanStorage;
import com.elikill58.negativity.universal.ban.storage.LoggedBanStorage;

public class BansMigration {

	public static void migrateBans() {
		// We only support file storage migration
		if (!Ban.banActive && !BanManager.getBanStorageId().equals("file"))
			return;

		Path bansDir = Ban.banDir.toPath();
		if (Files.notExists(bansDir))
			return;

		boolean didMigration = false;
		boolean migrationFailed = false;
		LoggedBanStorage loggedBanStorage = new FileLoggedBanStorage(() -> Ban.banDir);
		ActiveBanStorage activeBanStorage = new FileActiveBanStorage();
		try (Stream<Path> dirStream = Files.list(bansDir)) {
			List<Path> files = dirStream.filter(Files::isRegularFile).collect(Collectors.toList());
			if (files.isEmpty()) {
				return;
			}

			Path oldBansDir = bansDir.resolveSibling("old_bans");
			Files.createDirectories(oldBansDir);

			for (Path file : files) {
				String filename = file.getFileName().toString();
				String filenameWithoutExt = filename.replace(".txt", "");
				UUID uuid;
				try {
					uuid = UUID.fromString(filenameWithoutExt);
				} catch (IllegalArgumentException e) {
					continue;
				}

				try {
					List<LoggedBan> loadedBans = loggedBanStorage.load(uuid);
					LoggedBan extractedActiveBan = getActiveBanFromLoggedBans(loadedBans);

					List<LoggedBan> loggedBansToSave = loadedBans;
					if (extractedActiveBan != null) {
						// We remove the active ban and all the following ones from the list of bans to keep in logs
						loggedBansToSave = loadedBans.subList(0, loadedBans.indexOf(extractedActiveBan));

						ActiveBan activeBan = ActiveBan.from(extractedActiveBan);
						activeBanStorage.save(activeBan);
					}

					loggedBansToSave.forEach(loggedBanStorage::save);
					Files.move(file, oldBansDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

					didMigration = true;
				} catch (Exception e) {
					Adapter.getAdapter().error("Could not migrate ban file " + filename + ". Another migration attempt for this file will be made the next startup or reload.");
					e.printStackTrace();
					migrationFailed = true;
				}
			}
		} catch (IOException e) {
			Adapter.getAdapter().error("Unable to migrate bans.");
			e.printStackTrace();
		}

		if (didMigration && !migrationFailed) {
			Adapter.getAdapter().log("Bans migration ended successfully");
		}
	}

	@Nullable
	private static LoggedBan getActiveBanFromLoggedBans(List<LoggedBan> loggedBans) {
		if (loggedBans.isEmpty())
			return null;

		final long now = System.currentTimeMillis();
		for (LoggedBan loggedBan : loggedBans) {
			if (!loggedBan.isRevoked() && (loggedBan.isDefinitive() || loggedBan.getExpirationTime() > now)) {
				return loggedBan;
			}
		}

		return null;
	}
}
