package com.study_project.configuration.flyway;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.datasource")
public record DatasourceProperties(
		String url,
		String username,
		String password
) {}
