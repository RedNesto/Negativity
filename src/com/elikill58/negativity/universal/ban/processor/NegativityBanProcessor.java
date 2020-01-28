package com.elikill58.negativity.universal.ban.processor;

import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.elikill58.negativity.universal.NegativityPlayer;
import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.ban.ActiveBan;
import com.elikill58.negativity.universal.ban.storage.ActiveBanStorage;
import com.elikill58.negativity.universal.ban.storage.BanLogsStorage;
import com.elikill58.negativity.universal.permissions.Perm;

/**
 * This class, additionally to what {@link BaseNegativityBanProcessor} can do, kicks online players that have been banned.
 */
public class NegativityBanProcessor extends BaseNegativityBanProcessor {

	public NegativityBanProcessor(ActiveBanStorage activeBanStorage, @Nullable BanLogsStorage banLogsStorage) {
		super(activeBanStorage, banLogsStorage);
	}

	@Override
	public CompletableFuture<@Nullable ActiveBan> executeBan(ActiveBan ban) {
		NegativityPlayer nPlayer = Adapter.getAdapter().getNegativityPlayer(ban.getPlayerId());
		if (nPlayer != null && Perm.hasPerm(nPlayer, "notBanned"))
			return CompletableFuture.completedFuture(null);

		return super.executeBan(ban).whenComplete((executedBan, t) -> {
			if (executedBan != null && nPlayer != null) {
				nPlayer.banEffect();
				String formattedExpTime = new Timestamp(executedBan.getExpirationTime()).toString().split("\\.", 2)[0];
				nPlayer.kickPlayer(executedBan.getReason(), formattedExpTime, executedBan.getBannedBy(), executedBan.isDefinitive());
			}
		});
	}
}
