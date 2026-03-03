package com.study_project.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@TestConfiguration
@EnableWebMvc
@ComponentScan(basePackages = "com.study_project.validation")
@Import(ObjectMapper.class)
public class RestControllerTestConfig {
}
