package com.study_project.setup;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

public interface MysqlSetup {
	MySQLContainer mysql = new MySQLContainer("mysql:9.5");

	@DynamicPropertySource
	static void localstackDynamicPropertySource(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysql::getJdbcUrl);
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);
	}

	@BeforeAll
	static void mysqlBeforeAll() {
		mysql.start();
	}

	@AfterAll
	static void mysqlAfterAll() {
		mysql.stop();
	}
}
