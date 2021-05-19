package com.elikill58.negativity.universal.ban;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.elikill58.negativity.api.yaml.config.Configuration;
import com.elikill58.negativity.testFramework.TestAdapter;
import com.elikill58.negativity.universal.Adapter;
import com.elikill58.negativity.universal.Database;
import com.elikill58.negativity.universal.ban.processor.NegativityBanProcessor;
import com.elikill58.negativity.universal.ban.storage.DatabaseActiveBanStorage;

public abstract class BaseBanProcessorTests {
	
	@Test
	public void sameBanTwice() {
		NegativityBanProcessor processor = new NegativityBanProcessor(new DatabaseActiveBanStorage(), null);
		UUID playerId = UUID.randomUUID();
		long executionTime = System.currentTimeMillis();
		long expirationTime = executionTime + 10_000;
		Ban originalBan = new Ban(playerId, "Ban reason", "Ban source", BanType.MOD, expirationTime, "Cheat 1, Cheat 2", null, BanStatus.ACTIVE, executionTime);
		BanResult result = processor.executeBan(originalBan);
		Assertions.assertEquals(new BanResult(BanResult.BanResultType.DONE, originalBan), result);
		BanResult result2 = processor.executeBan(originalBan);
		Assertions.assertEquals(new BanResult(BanResult.BanResultType.ALREADY_BANNED, null), result2);
	}
	
	protected abstract void configure(Configuration configuration);
	
	@BeforeEach
	public void setUp() {
		TestAdapter adapter = new TestAdapter();
		Adapter.setAdapter(adapter);
		Configuration config = adapter.getConfig();
		config.set("Database.isActive", true);
		configure(config);
		adapter.reload();
		Assumptions.assumeTrue(Database.hasCustom, "Could not connect to database, skipping tests.");
	}
	
	@AfterEach
	public void tearDown() throws SQLException {
		Connection connection = Database.getConnection();
		if (connection != null) {
			// Clearing the tables after each test guarantees proper test isolation on the database end
			clearDatabaseTables(connection);
		}
		Database.close();
		Adapter.getAdapter().reloadConfig();
	}
	
	private void clearDatabaseTables(Connection connection) throws SQLException {
		try (PreparedStatement listTablesStm = connection.prepareStatement("SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'negativity_tests'")) {
			ResultSet listTablesResult = listTablesStm.executeQuery();
			connection.setAutoCommit(false);
			try (PreparedStatement disableForeignCheckStm = connection.prepareStatement("SET FOREIGN_KEY_CHECKS = 0")) {
				disableForeignCheckStm.executeUpdate();
			}
			while (listTablesResult.next()) {
				String tableName = listTablesResult.getString("TABLE_NAME");
				try (PreparedStatement dropTableStm = connection.prepareStatement("DROP TABLE " + tableName)) {
					dropTableStm.executeUpdate();
				}
			}
			try (PreparedStatement enableForeignCheckStm = connection.prepareStatement("SET FOREIGN_KEY_CHECKS = 1")) {
				enableForeignCheckStm.executeUpdate();
			}
			connection.commit();
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			if (!connection.getAutoCommit()) {
				connection.rollback();
			}
		} finally {
			connection.setAutoCommit(true);
		}
	}
}
