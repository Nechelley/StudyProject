package com.study_project.configuration.flyway;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayDatabaseConfiguration {

	private final String URL;
	private final String USERNAME;
	private final String PASSWORD;
	private final String LOCATIONS;

	public FlywayDatabaseConfiguration(DatasourceProperties datasourceProperties, FlywayProperties flywayProperties) {
		URL = datasourceProperties.url();
		USERNAME = datasourceProperties.username();
		PASSWORD = datasourceProperties.password();
		LOCATIONS = flywayProperties.locations();
	}

	@Bean(initMethod = "migrate")
	public Flyway flyway() {
		return Flyway.configure()
				.dataSource(URL, USERNAME, PASSWORD)
				.locations(LOCATIONS)
				.baselineOnMigrate(true)
				.load();
	}

}
