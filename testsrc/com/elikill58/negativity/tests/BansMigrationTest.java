package com.elikill58.negativity.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.ban.ActiveBan;
import com.elikill58.negativity.universal.ban.BanType;
import com.elikill58.negativity.universal.ban.BansMigration;
import com.elikill58.negativity.universal.ban.LoggedBan;
import com.elikill58.negativity.universal.ban.processor.BaseNegativityBanProcessor;
import com.elikill58.negativity.universal.ban.storage.FileActiveBanStorage;
import com.elikill58.negativity.universal.ban.storage.FileLoggedBanStorage;

class BansMigrationTest {

	static final UUID TEST_UUID_0 = UUID.fromString("c489bd97-6d0a-4183-b711-5ae56421ac62");
	static final UUID TEST_UUID_1 = UUID.fromString("c489bd97-6d0a-4183-b711-5ae56421ac63");

	@BeforeAll
	static void setUpAll() {
		if (Adapter.getAdapter() == null)
			Adapter.setAdapter(new DummyTestAdapter());

		FileActiveBanStorage.banDir = new File("bans");
		FileLoggedBanStorage.banLogsDir = new File(FileActiveBanStorage.banDir, "logs");

		BaseNegativityBanProcessor.setBanStorageId("file");
		BaseNegativityBanProcessor.setLogBans(true);
	}

	@BeforeEach
	void setUp() throws IOException {
		FileUtils.copyToFile(getClass().getResourceAsStream("/resources/banMigration/ban/" + TEST_UUID_0 + ".txt"), new File(FileActiveBanStorage.banDir, TEST_UUID_0 + ".txt"));
		FileUtils.copyToFile(getClass().getResourceAsStream("/resources/banMigration/ban/" + TEST_UUID_1 + ".txt"), new File(FileActiveBanStorage.banDir, TEST_UUID_1 + ".txt"));
	}

	@AfterEach
	void tearDown() throws IOException {
		FileUtils.deleteDirectory(FileActiveBanStorage.banDir);
		FileUtils.deleteDirectory(FileLoggedBanStorage.banLogsDir);
		FileUtils.deleteDirectory(new File("old_bans"));
	}

	@Test
	void migrateBans() {
		BansMigration.migrateBans();

		FileActiveBanStorage activeBanStorage = new FileActiveBanStorage();
		FileLoggedBanStorage loggedBanStorage = new FileLoggedBanStorage();

		{
			// TEST_UUID_0 assertions
			ActiveBan expectedActiveBan = new ActiveBan(TEST_UUID_0, "Cheat (SomeCheat)", "Negativity", true, BanType.PLUGIN, 2, "SomeCheat");
			ActiveBan activeBan = activeBanStorage.load(TEST_UUID_0);
			assertEquals(expectedActiveBan, activeBan, "Migrated active ban of TEST_UUID_0 is not the expected one");

			List<LoggedBan> expectedLoggedBans = Arrays.asList(
					new LoggedBan(TEST_UUID_0, "Cheat (SomeCheat)", "Negativity", false, BanType.PLUGIN, 0, "SomeCheat", false),
					new LoggedBan(TEST_UUID_0, "Cheat (SomeCheat)", "Negativity", true, BanType.PLUGIN, 1, "SomeCheat", true));
			List<LoggedBan> loggedBans = loggedBanStorage.load(TEST_UUID_0);
			assertIterableEquals(expectedLoggedBans, loggedBans, "Logged bans of TEST_UUID_0 are not expected");
		}

		{
			// TEST_UUID_1 assertions
			ActiveBan expectedActiveBan = new ActiveBan(TEST_UUID_1, "Cheat (SomeCheat)", "Negativity", true, BanType.PLUGIN, 1, "SomeCheat");
			ActiveBan activeBan = activeBanStorage.load(TEST_UUID_1);
			assertEquals(expectedActiveBan, activeBan, "Migrated active ban of TEST_UUID_1 is not the expected one");

			List<LoggedBan> expectedLoggedBans = Collections.singletonList(
					new LoggedBan(TEST_UUID_1, "Cheat (SomeCheat)", "Negativity", false, BanType.PLUGIN, 0, "SomeCheat", true));
			List<LoggedBan> loggedBans = loggedBanStorage.load(TEST_UUID_1);
			assertIterableEquals(expectedLoggedBans, loggedBans, "Logged bans of TEST_UUID_1 are not expected");
		}
	}
}
