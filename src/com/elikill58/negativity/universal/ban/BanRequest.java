package com.elikill58.negativity.universal.ban;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardOpenOption;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.elikill58.negativity.universal.Database;
import com.elikill58.negativity.universal.NegativityAccount;
import com.elikill58.negativity.universal.NegativityPlayer;
import com.elikill58.negativity.universal.UniversalUtils;
import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.permissions.Perm;

public class BanRequest {

	private UUID uuid;
	private String reason, by;
	private boolean def, isUnban;
	private BanType banType;
	private long expirationTime;
	private String ac;
	private File f = null;

	public BanRequest(UUID playerId, String banReason, long expirationTime, boolean def, BanType banType, String ac,
	                  String by, boolean isUnban) {
		this.uuid = playerId;
		this.reason = banReason;
		this.def = def;
		this.banType = banType;
		this.expirationTime = expirationTime;
		this.ac = ac;
		this.by = by;
		this.isUnban = isUnban;
	}

	public static BanRequest fromString(UUID playerId, String line) {
		String reason = "";
		String by = "Negativity";
		boolean def = false;
		boolean isUnban = false;
		BanType banType = BanType.UNKNOW;
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

		return new BanRequest(playerId, reason, expirationTime, def, banType, ac, by, isUnban);
	}

	public UUID getUUID() {
		return uuid;
	}

	public String getReason() {
		return reason;
	}

	public boolean isDef() {
		return def;
	}

	public String getBy() {
		return by;
	}

	public String getCheatName() {
		return ac;
	}

	public BanType getBanType() {
		return banType;
	}

	public long getExpirationTime() {
		return expirationTime;
	}

	public boolean isUnban() {
		return isUnban;
	}

	public void execute() {
		Adapter ada = Adapter.getAdapter();
		NegativityPlayer nPlayer = ada.getNegativityPlayer(this.uuid);
		if (nPlayer != null && Perm.hasPerm(nPlayer, "notBanned"))
			return;
		if (Ban.banActiveIsFile) {
			try {
				f = new File(Ban.banDir, uuid + ".txt");
				if (!f.exists())
					f.createNewFile();
				Files.write(f.toPath(),
						(expirationTime + ":reason=" + reason.replaceAll(":", "") + ":def=" + def + ":bantype="
								+ banType.name() + ":ac=" + ac + ":by=" + by + ":unban=false\n").getBytes(),
						StandardOpenOption.APPEND);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!Ban.banActiveIsFile) {
			try {
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
					content.add(getWithReplaceOlder(hash.get(keys)));
				}
				PreparedStatement stm = Database.getConnection().prepareStatement(
						"INSERT INTO " + Database.table_ban + "(" + values + ") VALUES (?,?,?,?,?,?" + parentheses + ")");
				stm.setString(1, uuid.toString());
				stm.setInt(2, (int) (expirationTime));
				stm.setBoolean(3, def);
				stm.setString(4, reason);
				stm.setString(5, ac);
				stm.setString(6, by);
				int i = 7;
				for (String cc : content) {
					String s = getWithReplaceOlder(cc);
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

		if (nPlayer != null) {
			nPlayer.banEffect();
			nPlayer.kickPlayer(reason, new Timestamp(expirationTime).toString().split("\\.", 2)[0], by, def);
		}
	}

	public void unban() {
		if(this.isUnban)
			return;
		try {
			this.isUnban = true;
			Adapter ada = Adapter.getAdapter();
			ada.getNegativityAccount(this.uuid).removeBanRequest(this);
			if (ada.getBooleanInConfig("ban.destroy_when_unban")) {
				if (Ban.banActiveIsFile) {
					Files.write(f.toPath(), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
					f.delete();
					f.deleteOnExit();
				}
				if (!Ban.banActiveIsFile) {
					PreparedStatement stm = Database.getConnection()
							.prepareStatement("DELETE FROM " + Database.table_ban + " WHERE uuid = ?");
					stm.setString(1, uuid.toString());
					stm.execute();
				}
			} else {
				if (Ban.banActiveIsFile) {
					List<String> lines = Files.readAllLines(f.toPath()), futurLines = new ArrayList<>();
					for (String l : lines) {
						if(l.contains("unban=false"))
							futurLines.add(l.replaceAll("unban=false", "unban=true")); // unbanning
						else if(!l.contains("unban=true"))
							futurLines.add(l + ":unban=true"); // unbanning with older version
						else futurLines.add(l); // already unban
					}
					BufferedWriter bw = new BufferedWriter(new PrintWriter(f.getAbsolutePath()));
					for (String l : futurLines) {
						bw.write(l);
						bw.newLine();
					}
					bw.close();
				}
				if (!Ban.banActiveIsFile) {
					String uc = ada.getStringInConfig("ban.db.column.uuid");
					PreparedStatement stm = Database.getConnection().prepareStatement("UPDATE " + Database.table_ban
							+ " SET " + ada.getStringInConfig("ban.db.column.time") + " = ? WHERE " + uc + " = ?");
					stm.setInt(1, 0);
					stm.setString(2, uuid.toString());
					stm.execute();
					PreparedStatement stm2 = Database.getConnection().prepareStatement("UPDATE " + Database.table_ban
							+ " SET " + ada.getStringInConfig("ban.db.column.def") + " = ? WHERE " + uc + " = ?");
					stm2.setBoolean(1, false);
					stm2.setString(2, uuid.toString());
					stm2.execute();
				}
			}
		} catch (NoSuchFileException e) {
			// already deleted
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getWithReplaceOlder(String s) {
		String life = "?";
		String name = "???";
		String level = "?";
		String gamemode = "?";
		String walkSpeed = "?";
		NegativityPlayer nPlayer = Adapter.getAdapter().getNegativityPlayer(this.uuid);
		if (nPlayer != null) {
			life = String.valueOf(nPlayer.getLife());
			name = nPlayer.getName();
			level = String.valueOf(nPlayer.getLevel());
			gamemode = nPlayer.getGameMode();
			walkSpeed = String.valueOf(nPlayer.getWalkSpeed());
		}

		return s.replaceAll("%uuid%", uuid.toString()).replaceAll("%name%", "").replaceAll("%reason%", reason)
				.replaceAll("%life%", life).replaceAll("%name%", name).replaceAll("%level%", level)
				.replaceAll("%gm%", gamemode).replaceAll("%walk_speed%", walkSpeed);
	}

	public static enum BanType {
		PLUGIN, MOD, CONSOLE, UNKNOW;
	}
}
