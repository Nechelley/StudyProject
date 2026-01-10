package com.study_project.configuration.flyway;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.flyway")
public record FlywayProperties(
		String locations
) {}
