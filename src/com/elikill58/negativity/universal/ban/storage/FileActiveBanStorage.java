package com.elikill58.negativity.universal.ban.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.ban.ActiveBan;
import com.elikill58.negativity.universal.ban.BanType;

public class FileActiveBanStorage implements ActiveBanStorage {

	private final Path banDir;
	// Using a single thread ensures this storage only access the ban file once at a time
	private final Executor fileAccessExecutor = Executors.newSingleThreadExecutor();

	public FileActiveBanStorage(Path banDir) {
		this.banDir = banDir;
	}

	@Override
	public CompletableFuture<@Nullable ActiveBan> load(UUID playerId) {
		return CompletableFuture.supplyAsync(() -> {
			Path banFile = banDir.resolve("active.txt");
			if (Files.notExists(banFile))
				return null;

			try (BufferedReader reader = Files.newBufferedReader(banFile)) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith(playerId.toString())) {
						return fromString(line);
					}
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

			return null;
		}, fileAccessExecutor);
	}

	@Override
	public CompletableFuture<Void> save(ActiveBan ban) {
		return CompletableFuture.runAsync(() -> {
			try {
				replaceBan(ban.getPlayerId(), ban);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}, fileAccessExecutor);
	}

	@Override
	public CompletableFuture<Void> remove(UUID playerId) {
		return CompletableFuture.runAsync(() -> {
			try {
				replaceBan(playerId, null);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}, fileAccessExecutor);
	}

	/**
	 * Replaces the active ban for the given UUID with the second parameter one.
	 * <p>
	 * This method exists to avoid implementing the (almost) same logic twice in {@link #load(UUID)} and {@link #save(ActiveBan)}.
	 */
	private void replaceBan(UUID playerId, @Nullable ActiveBan ban) throws IOException {
		Path banFile = banDir.resolve("active.txt");
		// If ban is null we only have to remove the ban, but if the storage file does not exist there is nothing to remove,
		// thus we don't need to do anything.
		if (ban == null && Files.notExists(banFile))
			return;

		if (Files.notExists(banFile)) {
			Path parentDir = banFile.getParent();
			if (parentDir != null) {
				Files.createDirectories(parentDir);
			}

			Files.createFile(banFile);
		}

		List<String> lines = Files.readAllLines(banFile);
		try (BufferedWriter writer = Files.newBufferedWriter(banFile)) {
			String playerIdString = playerId.toString();

			for (String line : lines) {
				if (!line.startsWith(playerIdString)) {
					writer.write(line);
					writer.newLine();
				}
			}

			if (ban != null) {
				writer.write(toString(ban));
				writer.newLine();
			}
		}
	}

	private static String toString(ActiveBan ban) {
		return ban.getPlayerId()
				+ ":expiration=" + ban.getExpirationTime()
				+ ":reason=" + ban.getReason().replaceAll(":", "")
				+ ":bantype=" + ban.getBanType().name()
				+ (ban.getCheatName() != null ? ":ac=" + ban.getCheatName() : "")
				+ ":by=" + ban.getBannedBy();
	}

	@Nullable
	private static ActiveBan fromString(String line) {
		String[] content = line.split(":");
		if (content.length == 1)
			return null;

		UUID playerId;
		try {
			playerId = UUID.fromString(content[0]);
		} catch (IllegalArgumentException e) {
			// This line is invalid
			return null;
		}

		long expirationTime = 0;
		String reason = "";
		String by = "Negativity";
		boolean def = false;
		BanType banType = BanType.UNKNOW;
		String ac = null;
		for (String s : content) {
			String[] part = s.split("=", 2);
			if (part.length != 2)
				continue;
			String type = part[0], value = part[1];
			switch (type) {
				case "expiration":
					expirationTime = Long.parseLong(value);
					break;
				case "bantype":
					banType = BanType.valueOf(value.toUpperCase());
					break;
				case "def":
					// Here for compatibility with files generated from an older version
					// of the plugin, where the expiration value may not be negative
					def = Boolean.parseBoolean(value);
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
				default:
					Adapter.getAdapter().warn("Type " + type + " unknow. Value: " + value);
					break;
			}
		}

		if (def) {
			expirationTime = -1;
		}

		return new ActiveBan(playerId, reason, by, banType, expirationTime, ac);
	}
}
