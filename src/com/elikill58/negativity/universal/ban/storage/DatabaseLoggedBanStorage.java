package com.elikill58.negativity.universal.ban.storage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.elikill58.negativity.universal.Database;
import com.elikill58.negativity.universal.NegativityAccount;
import com.elikill58.negativity.universal.NegativityPlayer;
import com.elikill58.negativity.universal.UniversalUtils;
import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.ban.BanType;
import com.elikill58.negativity.universal.ban.BaseBan;
import com.elikill58.negativity.universal.ban.LoggedBan;

public class DatabaseLoggedBanStorage implements LoggedBanStorage {

	@Override
	public List<LoggedBan> load(UUID playerId) {
		List<LoggedBan> loadedBans = new ArrayList<>();
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
				String reason = rs.getString(ada.getStringInConfig("ban.db.column.reason"));
				int expirationTime = rs.getInt(ada.getStringInConfig("ban.db.column.time"));
				boolean isDefinitive = rs.getBoolean(ada.getStringInConfig("ban.db.column.def"));
				String cheatName = hasCheatDetect ? rs.getString(ada.getStringInConfig("ban.db.column.cheat_detect")) : "Unknow";
				String bannedBy = hasBy ? rs.getString(ada.getStringInConfig("ban.db.column.by")) : "console";
				loadedBans.add(new LoggedBan(playerId, reason, bannedBy, isDefinitive, BanType.UNKNOW, expirationTime, cheatName, false));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return loadedBans;
	}

	@Override
	public void save(LoggedBan ban) {
		try {
			Adapter ada = Adapter.getAdapter();
			NegativityAccount account = ada.getNegativityAccount(ban.getPlayerId());
			String values = ada.getStringInConfig("ban.db.column.uuid") + ","
					+ ada.getStringInConfig("ban.db.column.time") + ","
					+ ada.getStringInConfig("ban.db.column.def") + ","
					+ ada.getStringInConfig("ban.db.column.reason") + ","
					+ ada.getStringInConfig("ban.db.column.cheat_detect") + ","
					+ ada.getStringInConfig("ban.db.column.by");
			String parentheses = "";
			List<String> content = new ArrayList<>();
			HashMap<String, String> hash = ada.getKeysListInConfig("ban.db.column.other");
			for (String keys : hash.keySet()) {
				values += "," + keys;
				parentheses += ",?";
				content.add(fillPlaceholders(account, ban, hash.get(keys)));
			}
			PreparedStatement stm = Database.getConnection().prepareStatement(
					"INSERT INTO " + Database.table_ban + "(" + values + ") VALUES (?,?,?,?,?,?" + parentheses + ")");
			stm.setString(1, ban.getPlayerId().toString());
			stm.setInt(2, (int) (ban.getExpirationTime()));
			stm.setBoolean(3, ban.isDefinitive());
			stm.setString(4, ban.getReason());
			stm.setString(5, ban.getCheatName());
			stm.setString(6, ban.getBannedBy());
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

	private static String fillPlaceholders(NegativityAccount nAccount, BaseBan ban, String s) {
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
