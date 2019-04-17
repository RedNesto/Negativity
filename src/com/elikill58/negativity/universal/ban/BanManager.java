package com.elikill58.negativity.universal.ban;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.elikill58.negativity.universal.ban.storage.BanStorage;
import com.elikill58.negativity.universal.ban.storage.DatabaseBanStorage;
import com.elikill58.negativity.universal.ban.storage.FileBanStorage;

public class BanManager {

	private static final BanStorage FILE_STORAGE = new FileBanStorage();
	private static final BanStorage DB_STORAGE = new DatabaseBanStorage();

	public static List<BanRequest> loadBans(UUID playerId) {
		if (Ban.banFileActive) {
			return FILE_STORAGE.load(playerId);
		}

		if (Ban.banDbActive) {
			return DB_STORAGE.load(playerId);
		}

		return new ArrayList<>();
	}

	public static void saveBans(Collection<BanRequest> bans) {
		Set<UUID> tracker = new HashSet<>();
		for (BanRequest ban : bans) {
			if (tracker.add(ban.getUUID())) {
				// Delete existing file to ensure we have no duplicated entries
				File bansFile = new File(Ban.banDir, ban.getUUID() + ".txt");
				bansFile.delete();
			}

			saveBan(ban);
		}
	}

	public static void saveBan(BanRequest ban) {
		if (Ban.banFileActive) {
			FILE_STORAGE.save(ban);
		}

		if (Ban.banDbActive) {
			DB_STORAGE.save(ban);
		}
	}
}
