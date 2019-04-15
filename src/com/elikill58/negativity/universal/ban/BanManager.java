package com.elikill58.negativity.universal.ban;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.elikill58.negativity.universal.Database;
import com.elikill58.negativity.universal.NegativityAccount;
import com.elikill58.negativity.universal.NegativityPlayer;
import com.elikill58.negativity.universal.UniversalUtils;
import com.elikill58.negativity.universal.adapter.Adapter;

public class BanManager {

	public static List<BanRequest> loadBans(UUID playerId) {
		List<BanRequest> loadedBans = new ArrayList<>();
		if (Ban.banFileActive) {
			File banFile = new File(Ban.banDir.getAbsolutePath(), playerId + ".txt");
			if (!banFile.exists())
				return loadedBans;

			try {
				for (String line : Files.readAllLines(banFile.toPath(), UniversalUtils.getOs().getCharset()))
					loadedBans.add(BanRequest.fromString(playerId, line));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (Ban.banDbActive) {
			try {
				Adapter ada = Adapter.getAdapter();
				PreparedStatement stm = Database.getConnection()
						.prepareStatement("SELECT * FROM " + Database.table_ban + " WHERE uuid = ?");
				stm.setString(1, playerId.toString());
				ResultSet rs = stm.executeQuery();
				while (rs.next()) {
					boolean hasCheatDetect = false, hasBy = false;
					try {
						rs.findColumn(ada.getStringInConfig("ban.db.column.cheat_detect"));
						hasCheatDetect = true;
					} catch (SQLException sqlexce) {
					}
					try {
						rs.findColumn(ada.getStringInConfig("ban.db.column.by"));
						hasBy = true;
					} catch (SQLException sqlexce) {
					}
					loadedBans.add(new BanRequest(playerId, rs.getString(ada.getStringInConfig("ban.db.column.reason")),
							rs.getInt(ada.getStringInConfig("ban.db.column.time")),
							rs.getBoolean(ada.getStringInConfig("ban.db.column.def")), BanRequest.BanType.UNKNOW,
							hasCheatDetect ? rs.getString(ada.getStringInConfig("ban.db.column.cheat_detect")) : "Unknow",
							hasBy ? rs.getString(ada.getStringInConfig("ban.db.column.by")) : "console", false));
				}
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return loadedBans;
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

		if (Ban.banDbActive) {
			try {

				Adapter ada = Adapter.getAdapter();
				NegativityAccount account = ada.getNegativityAccount(ban.getUUID());
				String values = ada.getStringInConfig("ban.db.column.uuid") + ","
						+ ada.getStringInConfig("ban.db.column.time") + "," + ada.getStringInConfig("ban.db.column.def")
						+ "," + ada.getStringInConfig("ban.db.column.reason") + ","
						+ ada.getStringInConfig("ban.db.column.cheat_detect") + ","
						+ ada.getStringInConfig("ban.db.column.by"), parentheses = "";
				List<String> content = new ArrayList<>();
				HashMap<String, String> hash = ada.getKeysListInConfig("ban.db.column.other");
				for (String keys : hash.keySet()) {
					values += "," + keys;
					parentheses += ",?";
					content.add(fillPlaceholders(account, ban, hash.get(keys)));
				}
				PreparedStatement stm = Database.getConnection().prepareStatement(
						"INSERT INTO " + Database.table_ban + "(" + values + ") VALUES (?,?,?,?,?,?" + parentheses + ")");
				stm.setString(1, ban.getUUID().toString());
				stm.setInt(2, (int) (ban.getExpirationTime()));
				stm.setBoolean(3, ban.isDef());
				stm.setString(4, ban.getReason());
				stm.setString(5, ban.getCheatName());
				stm.setString(6, ban.getBy());
				int i = 5;
				for (String cc : content) {
					String s = fillPlaceholders(account, ban, cc);
					if (UniversalUtils.isInteger(s))
						stm.setInt(i++, Integer.parseInt(s));
					else
						stm.setString(i++, s);
				}
				stm.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static String fillPlaceholders(NegativityAccount nAccount, BanRequest ban, String s) {
		String life = "?";
		String name = "???";
		String level = "?";
		String gamemode = "?";
		String walkSpeed = "?";
		if (nAccount instanceof NegativityPlayer) {
			NegativityPlayer np = (NegativityPlayer) nAccount;
			life = String.valueOf(np.getLife());
			name = np.getName();
			level = String.valueOf(np.getLevel());
			gamemode = np.getGameMode();
			walkSpeed = String.valueOf(np.getWalkSpeed());
		}

		return s.replaceAll("%uuid%", nAccount.getUUID())
				.replaceAll("%name%", "")
				.replaceAll("%reason%", ban.getReason())
				.replaceAll("%life%", life)
				.replaceAll("%name%", name)
				.replaceAll("%level%", level)
				.replaceAll("%gm%", gamemode)
				.replaceAll("%walk_speed%", walkSpeed);
	}
}
