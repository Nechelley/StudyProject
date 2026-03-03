package com.study_project.configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class LocaleConfig {

	@PostConstruct
	void init() {
		Locale.setDefault(Locale.ENGLISH);
	}
}
