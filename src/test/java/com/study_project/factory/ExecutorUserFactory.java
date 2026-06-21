package com.study_project.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study_project.controller.dto.TokenDto;
import com.study_project.controller.dto.UserLoginDto;
import com.study_project.enums.ProfileEnum;
import com.study_project.model.Profile;
import com.study_project.model.User;
import com.study_project.record.ExecutorUser;
import com.study_project.repository.UserRepository;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

@Component
public class ExecutorUserFactory {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	private static final String ADMIN_NAME = "admin admin";
	private static final String ADMIN_EMAIL = "adminEmail@example.com";
	private static final String BASIC_NAME = "basic basic";
	private static final String BASIC_EMAIL = "basicEmail@example.com";
	private static final String BASIC_SECONDARY_EMAIL = "basicSecondaryEmail@example.com";
	private static final String PASSWORD = "1234567890";
	private static final String PASSWORD_WITH_MINIMUM_CHARACTERS_IN_HASH = "$2a$10$bZxIK957JA31x66sCP0ive0qsKvuLjT/XEO27hPGjk.rNO8PaAVW6";//password "1234567890" in hash


	public ExecutorUser generateAdmin() throws JsonProcessingException {
		User user = createUserForTesting(ADMIN_NAME, ADMIN_EMAIL, ProfileEnum.ADMIN);
		String token = getToken(ADMIN_EMAIL, PASSWORD);
		return new ExecutorUser(user, token);
	}

	public ExecutorUser generateBasic() throws JsonProcessingException {
		User user = createUserForTesting(BASIC_NAME, BASIC_EMAIL, ProfileEnum.BASIC);
		String token = getToken(BASIC_EMAIL, PASSWORD);
		return new ExecutorUser(user, token);
	}

	public ExecutorUser generateBasicSecondary() throws JsonProcessingException {
		User user = createUserForTesting(BASIC_NAME, BASIC_SECONDARY_EMAIL, ProfileEnum.BASIC);
		String token = getToken(BASIC_SECONDARY_EMAIL, PASSWORD);
		return new ExecutorUser(user, token);
	}

	public String getStaticPassword() {
		return PASSWORD;
	}

	private String getToken(String email, String password) throws JsonProcessingException {
		UserLoginDto userLoginDto = new UserLoginDto();
		userLoginDto.setEmail(email);
		userLoginDto.setPassword(password);

		Response response = given()
				.log().all()
				.contentType(JSON)
				.body(objectMapper.writeValueAsString(userLoginDto))
				.post("/authentication")
				.then()
				.log().all()
				.extract()
				.response();

		TokenDto tokenDto = response.as(TokenDto.class);
		return tokenDto.token();
	}

	private User createUserForTesting(String name, String email, ProfileEnum profileEnum) {
		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setPassword(PASSWORD_WITH_MINIMUM_CHARACTERS_IN_HASH);
		user.setPasswordChangedAt(LocalDateTime.now().minusDays(1));

		Profile profile = new Profile(profileEnum.getId(), profileEnum.getName());
		user.setProfiles(List.of(profile));

		return userRepository.save(user);
	}
}
