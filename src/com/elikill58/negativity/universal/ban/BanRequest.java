package com.elikill58.negativity.universal.ban;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardOpenOption;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.UUID;

import com.elikill58.negativity.universal.Database;
import com.elikill58.negativity.universal.NegativityPlayer;
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

		BanManager.saveBan(this);

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
					BanManager.saveBans(ada.getNegativityAccount(uuid).getBanRequest());
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

	public static enum BanType {
		PLUGIN, MOD, CONSOLE, UNKNOW;
	}
}
