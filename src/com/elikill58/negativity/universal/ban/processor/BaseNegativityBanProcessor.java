package com.elikill58.negativity.universal.ban.processor;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.elikill58.negativity.universal.ban.ActiveBan;
import com.elikill58.negativity.universal.ban.LoggedBan;
import com.elikill58.negativity.universal.ban.storage.ActiveBanStorage;
import com.elikill58.negativity.universal.ban.storage.BanLogsStorage;

/**
 * This ban processor simply saves bans (active and logged) in a configurable storage ({@link ActiveBanStorage} and {@link BanLogsStorage} respectively).
 * <p>
 * It is important to know its sole purpose is to manage bans, and will not do anything on the game server,
 * like kicking the player when {@link BanProcessor#executeBan(ActiveBan) executing a ban}.
 * If you want direct actions on the game server use {@link NegativityBanProcessor} instead.
 */
public class BaseNegativityBanProcessor implements BanProcessor {

	protected final ActiveBanStorage activeBanStorage;
	@Nullable
	protected final BanLogsStorage banLogsStorage;

	public BaseNegativityBanProcessor(ActiveBanStorage activeBanStorage, @Nullable BanLogsStorage banLogsStorage) {
		this.activeBanStorage = activeBanStorage;
		this.banLogsStorage = banLogsStorage;
	}

	@Override
	public CompletableFuture<@Nullable ActiveBan> executeBan(ActiveBan ban) {
		return isBanned(ban.getPlayerId()).thenApplyAsync(isBanned -> {
			if (isBanned) {
				return null;
			}

			activeBanStorage.save(ban).join();
			return ban;
		});
	}

	@Override
	public CompletableFuture<@Nullable LoggedBan> revokeBan(UUID playerId) {
		return activeBanStorage.load(playerId).thenApplyAsync(activeBan -> {
			if (activeBan == null) {
				return null;
			}

			activeBanStorage.remove(playerId).join();
			LoggedBan revokedLoggedBan = LoggedBan.from(activeBan, true);

			if (banLogsStorage != null) {
				banLogsStorage.save(revokedLoggedBan).join();
			}

			return revokedLoggedBan;
		});
	}

	@Override
	public CompletableFuture<@Nullable ActiveBan> getActiveBan(UUID playerId) {
		return activeBanStorage.load(playerId).thenApplyAsync(activeBan -> {
			if (activeBan == null) {
				return null;
			}

			long now = System.currentTimeMillis();
			if (activeBan.isDefinitive() || activeBan.getExpirationTime() > now) {
				return activeBan;
			}

			activeBanStorage.remove(playerId).join();
			if (banLogsStorage != null) {
				banLogsStorage.save(LoggedBan.from(activeBan, false)).join();
			}

			return null;
		});
	}

	@Override
	public CompletableFuture<List<LoggedBan>> getLoggedBans(UUID playerId) {
		if (banLogsStorage == null) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}
		return banLogsStorage.load(playerId);
	}
}
