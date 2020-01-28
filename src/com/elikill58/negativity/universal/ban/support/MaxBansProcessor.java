package com.elikill58.negativity.universal.ban.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.HistoryRecord;
import org.maxgamer.maxbans.banmanager.Temporary;

import com.elikill58.negativity.universal.NegativityPlayer;
import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.ban.ActiveBan;
import com.elikill58.negativity.universal.ban.BanType;
import com.elikill58.negativity.universal.ban.LoggedBan;
import com.elikill58.negativity.universal.ban.processor.BanProcessor;

public class MaxBansProcessor implements BanProcessor {

	@Override
	public CompletableFuture<@Nullable ActiveBan> executeBan(ActiveBan ban) {
		NegativityPlayer player = Adapter.getAdapter().getNegativityPlayer(ban.getPlayerId());
		if (player == null) {
			return CompletableFuture.completedFuture(null);
		}

		return CompletableFuture.supplyAsync(() -> {
			if (ban.isDefinitive()) {
				MaxBans.instance.getBanManager().ban(player.getName(), ban.getReason(), ban.getBannedBy());
			} else {
				MaxBans.instance.getBanManager().tempban(player.getName(), ban.getReason(), ban.getBannedBy(), ban.getExpirationTime());
			}
			return ban;
		});
	}

	@Override
	public CompletableFuture<@Nullable LoggedBan> revokeBan(UUID playerId) {
		NegativityPlayer player = Adapter.getAdapter().getNegativityPlayer(playerId);
		if (player == null) {
			return CompletableFuture.completedFuture(null);
		}

		return CompletableFuture.supplyAsync(() -> {
			Ban revokedBan = MaxBans.instance.getBanManager().getBan(player.getName());
			if (revokedBan == null) {
				return null;
			}

			MaxBans.instance.getBanManager().unban(player.getName());

			
			long expirationTime = -1;
			if (revokedBan instanceof Temporary) {
				expirationTime = ((Temporary) revokedBan).getExpires();
			}
			return new LoggedBan(playerId, revokedBan.getReason(), revokedBan.getBanner(),  BanType.UNKNOW, expirationTime, revokedBan.getReason(), true);
		});
	}

	@Override
	public CompletableFuture<@Nullable ActiveBan> getActiveBan(UUID playerId) {
		NegativityPlayer player = Adapter.getAdapter().getNegativityPlayer(playerId);
		if (player == null) {
			return CompletableFuture.completedFuture(null);
		}

		return CompletableFuture.supplyAsync(() -> {
			Ban ban = MaxBans.instance.getBanManager().getBan(player.getName());

			
			long expirationTime = -1;
			if (ban instanceof Temporary) {
				expirationTime = ((Temporary) ban).getExpires();
			}

			return new ActiveBan(playerId, ban.getReason(), ban.getBanner(), BanType.UNKNOW, expirationTime, ban.getReason());
		});
	}

	@Override
	public CompletableFuture<List<LoggedBan>> getLoggedBans(final UUID playerId) {
		final NegativityPlayer player = Adapter.getAdapter().getNegativityPlayer(playerId);
		if (player == null) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}

		return CompletableFuture.supplyAsync(() -> {
			List<LoggedBan> loggedBans = new ArrayList<>();
			for (HistoryRecord record : MaxBans.instance.getBanManager().getHistory(player.getName())) {
				loggedBans.add(new LoggedBan(playerId, record.getMessage(), record.getBanner(), BanType.UNKNOW, 0, record.getMessage(), false));
			}
			return loggedBans;
		});
	}
}
