package com.elikill58.negativity.universal.ban;

import org.junit.jupiter.api.Assumptions;

import com.elikill58.negativity.api.yaml.config.Configuration;

public class MysqlBanProcessorTests extends BaseBanProcessorTests {
	
	@Override
	protected void configure(Configuration configuration) {
		String url = System.getProperty("negativity.mysql.url");
		Assumptions.assumeTrue(url != null, "Missing MySQL database URL");
		String user = System.getProperty("negativity.mysql.user");
		Assumptions.assumeTrue(user != null, "Missing MySQL database user");
		String password = System.getProperty("negativity.mysql.password");
		Assumptions.assumeTrue(password != null, "Missing MySQL database password");
		configuration.set("Database.url", url);
		configuration.set("Database.user", user);
		configuration.set("Database.password", password);
	}
}
