package com.elikill58.negativity.universal.ban.processor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.elikill58.negativity.universal.ban.ActiveBan;
import com.elikill58.negativity.universal.ban.BanManager;
import com.elikill58.negativity.universal.ban.LoggedBan;

/**
 * Decides what to do with ban and unban requests as well as active and logged bans queries.
 * <p>
 * {@link BanManager} basically delegates actions to a BanProcessor.
 */
public interface BanProcessor {

	/**
	 * Executes the given ban.
	 * <p>
	 * The ban may not be executed for any processor-specific reason.
	 *
	 * @return the ban that has been executed, or {@code null} if the ban has not been executed.
	 */
	CompletableFuture<@Nullable ActiveBan> executeBan(ActiveBan ban);

	/**
	 * Revokes the active ban of the player identified by the given UUID.
	 * <p>
	 * The revocation may fail for any processor-specific reason.
	 * <p>
	 * If the revocation was successful a LoggedBan must always be returned, even if it will not persist in any way.
	 *
	 * @param playerId the UUID of the player to unban
	 *
	 * @return the logged revoked ban or {@code null} if the revocation failed.
	 */
	CompletableFuture<@Nullable LoggedBan> revokeBan(UUID playerId);

	default CompletableFuture<Boolean> isBanned(UUID playerId) {
		return getActiveBan(playerId).thenApply(Objects::nonNull);
	}

	CompletableFuture<@Nullable ActiveBan> getActiveBan(UUID playerId);

	CompletableFuture<List<LoggedBan>> getLoggedBans(UUID playerId);
}
