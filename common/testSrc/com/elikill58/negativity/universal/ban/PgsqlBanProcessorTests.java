package com.elikill58.negativity.universal.ban;

import org.junit.jupiter.api.Assumptions;

import com.elikill58.negativity.api.yaml.config.Configuration;
import com.elikill58.negativity.universal.Database;

public class PgsqlBanProcessorTests extends BaseBanProcessorTests {
	
	@Override
	protected void configure(Configuration configuration) {
		String url = System.getenv("NEGATIVITY_PGSQL_URL");
		Assumptions.assumeTrue(url != null, "Missing PostgreSQL database URL");
		String user = System.getenv("NEGATIVITY_PGSQL_USER");
		Assumptions.assumeTrue(user != null, "Missing PostgreSQL database user");
		String password = System.getenv("NEGATIVITY_PGSQL_PASSWORD");
		Assumptions.assumeTrue(password != null, "Missing PostgreSQL database password");
		configuration.set("Database.url", url);
		configuration.set("Database.user", user);
		configuration.set("Database.password", password);
		configuration.set("Database.type", Database.DatabaseType.POSTGRESQL.getType());
	}
}
