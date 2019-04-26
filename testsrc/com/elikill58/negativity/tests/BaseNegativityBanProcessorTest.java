package com.elikill58.negativity.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.elikill58.negativity.universal.ban.ActiveBan;
import com.elikill58.negativity.universal.ban.BanType;
import com.elikill58.negativity.universal.ban.LoggedBan;
import com.elikill58.negativity.universal.ban.processor.BaseNegativityBanProcessor;
import com.elikill58.negativity.universal.ban.storage.FileActiveBanStorage;
import com.elikill58.negativity.universal.ban.storage.FileLoggedBanStorage;

class BaseNegativityBanProcessorTest {

	BaseNegativityBanProcessor banProcessor = new BaseNegativityBanProcessor();

	@BeforeAll
	static void setUpAll() {
		FileActiveBanStorage.banDir = new File("bans");
		FileLoggedBanStorage.banLogsDir = new File(FileActiveBanStorage.banDir, "logs");

		BaseNegativityBanProcessor.setBanStorageId("file");
		BaseNegativityBanProcessor.setLogBans(true);
	}

	@AfterEach
	void tearDown() throws IOException {
		FileUtils.deleteDirectory(FileActiveBanStorage.banDir);
		FileUtils.deleteDirectory(FileLoggedBanStorage.banLogsDir);
	}

	@Test
	void banRevocation() {
		UUID playerId = UUID.randomUUID();

		long expirationTime = System.currentTimeMillis() + 60 * 10000;
		ActiveBan executedBan = banProcessor.banPlayer(playerId, "test", "JUnit", false, BanType.CONSOLE, expirationTime, "none");
		assertNotNull(executedBan, "The ban should be executed");

		ActiveBan activeBan = banProcessor.getActiveBan(playerId);
		assertNotNull(activeBan, "There should be an active ban");

		assertEquals(executedBan, activeBan, "The active ban is not the executed one");

		LoggedBan revokedBan = banProcessor.revokeBan(playerId);
		assertNotNull(revokedBan, "Ban must have been revoked");

		List<LoggedBan> loggedBans = banProcessor.getLoggedBans(playerId);
		assertEquals(1, loggedBans.size(), "There should be exactly one logged ban");

		assertEquals(revokedBan, loggedBans.get(0), "The logged ban must be equal to the revoked one");
	}

	@Test
	void banExpiration() throws InterruptedException {
		UUID playerId = UUID.randomUUID();

		int banDuration = 10;
		long expirationTime = System.currentTimeMillis() + banDuration;
		banProcessor.banPlayer(playerId, "test", "JUnit", false, BanType.CONSOLE, expirationTime, "none");

		Thread.sleep(banDuration);

		ActiveBan activeBan = banProcessor.getActiveBan(playerId);
		assertNull(activeBan, "There should be no active ban");

		List<LoggedBan> loggedBans = banProcessor.getLoggedBans(playerId);
		assertEquals(1, loggedBans.size(), "There should be exactly one logged ban");

		assertFalse(loggedBans.get(0).isRevoked(), "Logged ban should not be marked as revoked");
	}

	@Test
	void multipleLoggedBans() {
		UUID playerId = UUID.randomUUID();

		final int iterations = 5;
		for (int i = 0; i < iterations; i++) {
			banProcessor.banPlayer(playerId, "test", "JUnit", true, BanType.CONSOLE, 0, "none");
			banProcessor.revokeBan(playerId);
		}

		List<LoggedBan> loggedBans = banProcessor.getLoggedBans(playerId);
		assertEquals(iterations, loggedBans.size(), "There should be exactly " + iterations + " logged bans");

		for (int i = 0; i < iterations; i++) {
			assertTrue(loggedBans.get(i).isRevoked(), "Ban should not be revoked");
		}
	}

	@Test
	void failedRevocation() {
		UUID playerId = UUID.randomUUID();

		LoggedBan revokedBan = banProcessor.revokeBan(playerId);
		assertNull(revokedBan, "No ban should be revoked");
	}

	@Test
	void banOverwrite() {
		UUID playerId = UUID.randomUUID();

		ActiveBan firstExecutedBan = banProcessor.banPlayer(playerId, "first ban", "JUnit", true, BanType.CONSOLE, 0, "none");
		assertNotNull(firstExecutedBan, "First ban should be executed");

		ActiveBan secondExecutedBan = banProcessor.banPlayer(playerId, "second ban", "JUnit", true, BanType.CONSOLE, 0, "none");
		assertNull(secondExecutedBan, "Second ban should not be executed");

		ActiveBan activeBan = banProcessor.getActiveBan(playerId);
		assertEquals(firstExecutedBan, activeBan, "The active ban should be the first executed ban");
	}
}
