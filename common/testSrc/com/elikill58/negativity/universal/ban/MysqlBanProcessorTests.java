package com.elikill58.negativity.universal.ban;

import org.junit.jupiter.api.Assumptions;

import com.elikill58.negativity.api.yaml.config.Configuration;
import com.elikill58.negativity.universal.Database;

public class MysqlBanProcessorTests extends BaseBanProcessorTests {
	
	@Override
	protected void configure(Configuration configuration) {
		String url = System.getenv("NEGATIVITY_MYSQL_URL");
		Assumptions.assumeTrue(url != null, "Missing MySQL database URL");
		String user = System.getenv("NEGATIVITY_MYSQL_USER");
		Assumptions.assumeTrue(user != null, "Missing MySQL database user");
		String password = System.getenv("NEGATIVITY_MYSQL_PASSWORD");
		Assumptions.assumeTrue(password != null, "Missing MySQL database password");
		configuration.set("Database.url", url);
		configuration.set("Database.user", user);
		configuration.set("Database.password", password);
		configuration.set("Database.type", Database.DatabaseType.MYSQL.getType());
	}
}
