package com.elikill58.negativity.universal.ban.storage;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.elikill58.negativity.universal.ban.ActiveBan;

/**
 * A class responsible for loading and saving active bans.
 * <p>
 * Implementations must not retain any state since they can be replaced at any time,
 * caching is fine as long as it does not require saving cached values implicitly.
 */
public interface ActiveBanStorage {

	/**
	 * Loads the active ban of the player identified by the given UUID.
	 *
	 * @param playerId the UUID of a player.
	 *
	 * @return the active ban of the player, or {@code null} if the player is not banned
	 */
	CompletableFuture<@Nullable ActiveBan> load(UUID playerId);

	/**
	 * Saves the given active ban.
	 *
	 * @param ban the active ban to save.
	 * @return
	 */
	CompletableFuture<Void> save(ActiveBan ban);

	/**
	 * Removes the ban associated to the player identified by the given UUID.
	 *
	 * @param playerId the UUID of the player
	 * @return
	 */
	CompletableFuture<Void> remove(UUID playerId);
}
