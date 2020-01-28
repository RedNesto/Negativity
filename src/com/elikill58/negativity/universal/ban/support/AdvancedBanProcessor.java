package com.elikill58.negativity.universal.ban.support;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.elikill58.negativity.universal.NegativityPlayer;
import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.ban.ActiveBan;
import com.elikill58.negativity.universal.ban.BanType;
import com.elikill58.negativity.universal.ban.LoggedBan;
import com.elikill58.negativity.universal.ban.processor.BanProcessor;

import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;

public class AdvancedBanProcessor implements BanProcessor {

	@Override
	public CompletableFuture<@Nullable ActiveBan> executeBan(ActiveBan ban) {
		NegativityPlayer player = Adapter.getAdapter().getNegativityPlayer(ban.getPlayerId());
		if (player == null) {
			return CompletableFuture.completedFuture(null);
		}

		String playerName = player.getName();
		return CompletableFuture.supplyAsync(() -> {
			long endTime = ban.isDefinitive() ? 0 : ban.getExpirationTime();
			PunishmentType type = ban.isDefinitive() ? PunishmentType.BAN : PunishmentType.TEMP_BAN;
			Punishment punishment = new Punishment(playerName, ban.getPlayerId().toString(), ban.getReason(), ban.getBannedBy(), type, System.currentTimeMillis(), endTime, "", -1);
			punishment.create();

			return ban;
		});
	}

	@Override
	public CompletableFuture<@Nullable LoggedBan> revokeBan(UUID playerId) {
		return CompletableFuture.supplyAsync(() -> {
			Punishment punishment = PunishmentManager.get().getBan(playerId.toString());
			if (punishment == null) {
				return null;
			}

			punishment.delete();
			return loggedBanFrom(playerId, punishment, true);
		});
	}

	@Override
	public CompletableFuture<Boolean> isBanned(UUID playerId) {
		return CompletableFuture.supplyAsync(() -> PunishmentManager.get().isBanned(playerId.toString()));
	}

	@Override
	public CompletableFuture<@Nullable ActiveBan> getActiveBan(UUID playerId) {
		return CompletableFuture.supplyAsync(() -> {
			Punishment punishment = PunishmentManager.get().getBan(playerId.toString());
			if (punishment == null) {
				return null;
			}

			return new ActiveBan(playerId,
					punishment.getReason(),
					punishment.getOperator(),
					BanType.UNKNOW,
					punishment.getEnd(),
					punishment.getReason());
		});
	}

	@Override
	public CompletableFuture<List<LoggedBan>> getLoggedBans(UUID playerId) {
		return CompletableFuture.supplyAsync(() -> {
			List<Punishment> punishments = PunishmentManager.get().getPunishments(playerId.toString(), PunishmentType.BAN, false);
			List<LoggedBan> loggedBans = new ArrayList<>();
			punishments.forEach(punishment -> loggedBans.add(loggedBanFrom(playerId, punishment, false)));
			return loggedBans;
		});
	}

	private LoggedBan loggedBanFrom(UUID playerId, Punishment punishment, boolean revoked) {
		return new LoggedBan(playerId,
				punishment.getReason(),
				punishment.getOperator(),
				BanType.UNKNOW,
				punishment.getEnd(),
				punishment.getReason(),
				revoked);
	}
}
