package com.elikill58.negativity.universal;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.elikill58.negativity.universal.ban.Ban;
import com.elikill58.negativity.universal.ban.BanManager;
import com.elikill58.negativity.universal.ban.BanRequest;
import com.elikill58.negativity.universal.utils.NonnullByDefault;

/**
 * Contains player-related data that can be accessed when the player is offline.
 */
@NonnullByDefault
public class NegativityAccount {

	private UUID playerId;
	private String lang;
	private boolean gettedBan;
	private List<BanRequest> banRequest;

	public NegativityAccount(UUID playerId) {
		this(playerId, TranslatedMessages.DEFAULT_LANG, false, new ArrayList<>());
	}

	public NegativityAccount(UUID playerId, String lang, boolean gettedBan, List<BanRequest> banRequest) {
		this.playerId = playerId;
		this.lang = lang;
		this.gettedBan = gettedBan;
		this.banRequest = banRequest;
	}

	public String getUUID() {
		return this.playerId.toString();
	}

	public String getLang() {
		return lang;
	}

	public UUID getPlayerId() {
		return playerId;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public boolean hasGettedBan() {
		return gettedBan;
	}

	public void setGettedBan(boolean b) {
		this.gettedBan = b;
	}

	public List<BanRequest> getBanRequest() {
		if (!hasGettedBan())
			loadBanRequest();
		List<BanRequest> br = new ArrayList<>();
		br.addAll(banRequest);
		return br;
	}

	public void loadBanRequest() {
		loadBanRequest(false);
	}

	public void loadBanRequest(boolean forceReload) {
		if(!Ban.banActive)
			return;

		if (!forceReload && gettedBan)
			return;

		gettedBan = true;
		banRequest = BanManager.loadBans(getPlayerId());
	}

	public String getBanReason() {
		if (banRequest.size() == 0)
			return "not banned";
		if (banRequest.size() == 1)
			return banRequest.get(0).getReason();
		String reason = "";
		List<String> cheatAlready = new ArrayList<>();
		for (BanRequest br : banRequest)
			if (!cheatAlready.contains(br.getCheatName()) && !br.getCheatName().equalsIgnoreCase("unknow")) {
				reason += (reason.equalsIgnoreCase("") ? "Cheat (" : ", ") + br.getCheatName();
				cheatAlready.add(br.getCheatName());
			}
		return reason + ")";
	}

	public String getBanTime() {
		if (banRequest.size() == 0)
			return "not banned";
		long l = 0;
		for (BanRequest br : banRequest) {
			if (br.isDef())
				return "always";
			else if ((br.getExpirationTime()) > l)
				l = br.getExpirationTime();
		}
		Timestamp time = new Timestamp(l);
		return time.toString().split("\\.", 2)[0];
	}

	public String getBanBy() {
		String by = "";
		List<String> byAlready = new ArrayList<>();
		for (BanRequest br : banRequest)
			if (!byAlready.contains(br.getBy())) {
				by += (by.equalsIgnoreCase("") ? "" : ", ") + br.getBy();
				byAlready.add(br.getBy());
			}
		return by;
	}

	public boolean isBanDef() {
		if (banRequest.size() == 0)
			return false;
		for (BanRequest br : banRequest)
			if (br.isDef())
				return true;
		return false;
	}

	public void removeBanRequest(BanRequest br) {
		banRequest.remove(br);
	}
}
