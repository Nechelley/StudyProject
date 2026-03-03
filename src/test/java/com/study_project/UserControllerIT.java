package com.study_project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study_project.config.RestControllerTestConfig;
import com.study_project.configuration.security.SecurityConfiguration;
import com.study_project.controller.dto.*;
import com.study_project.enums.ProfileEnum;
import com.study_project.factory.UserCreationDtoFactory;
import com.study_project.factory.UserUpdateDtoFactory;
import com.study_project.model.Profile;
import com.study_project.model.User;
import com.study_project.repository.UserRepository;
import com.study_project.setup.MysqlSetup;
import com.study_project.validation.dto.GenericFieldErrorDto;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = RestControllerTestConfig.class)
class UserControllerIT implements MysqlSetup {

	private static final String ADMIN_REGISTERED_EMAIL = "adminRegisteredEmail@example.com";
	private static final String BASIC_REGISTERED_EMAIL = "registeredEmail@example.com";
	private static final String BEARER_PREFIX = "Bearer ";
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String USER_PATH = "/user";

	private static final String NAME_WITH_MINIMUM_CHARACTERS = "teste";
	private static final String NAME_WITH_MAXIMUM_CHARACTERS = "100charactersNameaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	private static final String NAME_WITH_LESS_THAN_MINIMUM_CHARACTERS = "john";
	private static final String NAME_WITH_MORE_THAN_MAXIMUM_CHARACTERS = "johnnyaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	private static final String NAME_WITH_ESPECIAL_CHARACTERS = "Sáçoul LeTest";

	private static final String EMAIL_WITH_MINIMUM_CHARACTERS = "te@test.br";
	private static final String EMAIL_WITH_MAXIMUM_CHARACTERS = "Is72charactersEmailaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@test.br";
	private static final String EMAIL_WITH_LESS_THAN_MINIMUM_CHARACTERS = "j@e.com";
	private static final String EMAIL_WITH_MORE_THAN_MAXIMUM_CHARACTERS = "johnnyaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@example.com";
	private static final String MALFORMED_EMAIL = "malformed@";

	private static final String PASSWORD_WITH_MINIMUM_CHARACTERS = "asdfghjkl0";
	private static final String PASSWORD_WITH_MAXIMUM_CHARACTERS = "72charactersPasswordaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	private static final String PASSWORD_WITH_LESS_THAN_MINIMUM_CHARACTERS = "pass";
	private static final String PASSWORD_WITH_MORE_THAN_MAXIMUM_CHARACTERS = "password123aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	private static final String PASSWORD = "1234567890";
	private static final String NEW_PASSWORD = "qwertyui10";
	private static final String WRONG_PASSWORD = "0000000000";
	private static final String PASSWORD_WITH_MINIMUM_CHARACTERS_IN_HASH = "$2a$10$bZxIK957JA31x66sCP0ive0qsKvuLjT/XEO27hPGjk.rNO8PaAVW6";//password "1234567890" in hash

	@LocalServerPort
	private Integer port;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	private String adminToken;
	private String basicToken;
	private User adminUser;
	private User basicUser;

	@BeforeEach
	void setup() throws JsonProcessingException {
		RestAssured.baseURI = String.format("http://localhost:%s", port);

		//cleanup
		userRepository.deleteAll();

		// seed existing user
		adminUser = createUserForTesting("big johnny", ADMIN_REGISTERED_EMAIL, ProfileEnum.ADMIN);
		basicUser = createUserForTesting("johnny", BASIC_REGISTERED_EMAIL, ProfileEnum.BASIC);

		// get authorization
		adminToken = getToken(ADMIN_REGISTERED_EMAIL, PASSWORD);
		basicToken = getToken(BASIC_REGISTERED_EMAIL, PASSWORD);
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

	protected void compareUserWithDatabaseUser(UserResponseDto userResponseDto, boolean checkEmail) {
		Optional<User> userInDatabase = userRepository.findById(userResponseDto.getId());
		assertThat(userInDatabase)
				.isPresent();
		assertThat(userInDatabase.get().getId())
				.isEqualTo(userResponseDto.getId());
		assertThat(userInDatabase.get().getName())
				.isEqualTo(userResponseDto.getName());
		if (checkEmail) {
			assertThat(userInDatabase.get().getEmail())
					.isEqualTo(userResponseDto.getEmail());
		}
	}

	@DisplayName("When creating a user")
	@Nested
	class Create {

		protected Response doCreateUserRequest(UserCreationDto userCreationDto, ProfileEnum executorProfile, Boolean useAdminPath) throws JsonProcessingException {
			String path = USER_PATH.concat(useAdminPath ? "/admin" : "");
			if (ProfileEnum.ADMIN.equals(executorProfile)) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(adminToken))
						.body(objectMapper.writeValueAsString(userCreationDto))
						.post(path)
						.then()
						.log().all()
						.extract()
						.response();
			}
			if (ProfileEnum.BASIC.equals(executorProfile)) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(basicToken))
						.body(objectMapper.writeValueAsString(userCreationDto))
						.post(path)
						.then()
						.log().all()
						.extract()
						.response();
			}
			return given()
					.log().all()
					.contentType(JSON)
					.body(objectMapper.writeValueAsString(userCreationDto))
					.post(path)
					.then()
					.log().all()
					.extract()
					.response();
		}

		@DisplayName("Must success")
		@Nested
		class Success {

			private static Stream<Arguments> provideUserSuccessArguments() {
				UserCreationDto simpleUserCreationDto = UserCreationDtoFactory.aUserCreationDto()
						.build();
				UserCreationDto minimumUserCreationDto = UserCreationDtoFactory.aUserCreationDto()
						.withName(NAME_WITH_MINIMUM_CHARACTERS)
						.withEmail(EMAIL_WITH_MINIMUM_CHARACTERS)
						.withPassword(PASSWORD_WITH_MINIMUM_CHARACTERS)
						.build();
				UserCreationDto maximumUserCreationDto = UserCreationDtoFactory.aUserCreationDto()
						.withName(NAME_WITH_MAXIMUM_CHARACTERS)
						.withEmail(EMAIL_WITH_MAXIMUM_CHARACTERS)
						.withPassword(PASSWORD_WITH_MAXIMUM_CHARACTERS)
						.build();
				UserCreationDto specialNameUserCreationDto = UserCreationDtoFactory.aUserCreationDto()
						.withName(NAME_WITH_ESPECIAL_CHARACTERS)
						.build();

				return Stream.of(
						Arguments.of(simpleUserCreationDto, ProfileEnum.ADMIN, true),
						Arguments.of(simpleUserCreationDto, ProfileEnum.ADMIN, false),
						Arguments.of(simpleUserCreationDto, ProfileEnum.BASIC, false),
						Arguments.of(simpleUserCreationDto, null, false),

						Arguments.of(minimumUserCreationDto, ProfileEnum.ADMIN, true),
						Arguments.of(minimumUserCreationDto, ProfileEnum.ADMIN, false),
						Arguments.of(minimumUserCreationDto, ProfileEnum.BASIC, false),
						Arguments.of(minimumUserCreationDto, null, false),

						Arguments.of(maximumUserCreationDto, ProfileEnum.ADMIN, true),
						Arguments.of(maximumUserCreationDto, ProfileEnum.ADMIN, false),
						Arguments.of(maximumUserCreationDto, ProfileEnum.BASIC, false),
						Arguments.of(maximumUserCreationDto, null, false),

						Arguments.of(specialNameUserCreationDto, ProfileEnum.ADMIN, true),
						Arguments.of(specialNameUserCreationDto, ProfileEnum.ADMIN, false),
						Arguments.of(specialNameUserCreationDto, ProfileEnum.BASIC, false),
						Arguments.of(specialNameUserCreationDto, null, false)
				);
			}

			@DisplayName("With a user with all correct information")
			@ParameterizedTest
			@MethodSource("provideUserSuccessArguments")
			void createUser_shouldReturn201_whenAllInformationIsCorrect(UserCreationDto userCreationDto, ProfileEnum executorProfile, Boolean useAdminPath) throws JsonProcessingException {
				Response response = doCreateUserRequest(userCreationDto, executorProfile, useAdminPath);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.CREATED.value());

				UserResponseDto userResponseDto = response.as(UserResponseDto.class);
				assertThat(userResponseDto.getId())
						.isNotNull();
				assertThat(userResponseDto.getName())
						.isEqualTo(userCreationDto.getName());
				assertThat(userResponseDto.getEmail())
						.isEqualTo(userCreationDto.getEmail());

				compareUserWithDatabaseUser(userResponseDto, true);
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			private static Stream<Arguments> provideUserWithFailSizeNameArguments() {
				UserCreationDto lessThanMinimumUserCreationDto = UserCreationDtoFactory.aUserCreationDto()
						.withName(NAME_WITH_LESS_THAN_MINIMUM_CHARACTERS)
						.build();
				UserCreationDto moreThanMaximumUserCreationDto = UserCreationDtoFactory.aUserCreationDto()
						.withName(NAME_WITH_MORE_THAN_MAXIMUM_CHARACTERS)
						.build();

				return Stream.of(
						Arguments.of(lessThanMinimumUserCreationDto, ProfileEnum.ADMIN, true),
						Arguments.of(lessThanMinimumUserCreationDto, ProfileEnum.ADMIN, false),
						Arguments.of(lessThanMinimumUserCreationDto, ProfileEnum.BASIC, false),
						Arguments.of(lessThanMinimumUserCreationDto, null, false),

						Arguments.of(moreThanMaximumUserCreationDto, ProfileEnum.ADMIN, true),
						Arguments.of(moreThanMaximumUserCreationDto, ProfileEnum.ADMIN, false),
						Arguments.of(moreThanMaximumUserCreationDto, ProfileEnum.BASIC, false),
						Arguments.of(moreThanMaximumUserCreationDto, null, false)
				);
			}

			private static Stream<Arguments> provideUserWithFailSizeEmailArguments() {
				UserCreationDto lessThanMinimumUserCreationDto = UserCreationDtoFactory.aUserCreationDto()
						.withEmail(EMAIL_WITH_LESS_THAN_MINIMUM_CHARACTERS)
						.build();
				UserCreationDto moreThanMaximumUserCreationDto = UserCreationDtoFactory.aUserCreationDto()
						.withEmail(EMAIL_WITH_MORE_THAN_MAXIMUM_CHARACTERS)
						.build();

				return Stream.of(
						Arguments.of(lessThanMinimumUserCreationDto, ProfileEnum.ADMIN, true),
						Arguments.of(lessThanMinimumUserCreationDto, ProfileEnum.ADMIN, false),
						Arguments.of(lessThanMinimumUserCreationDto, ProfileEnum.BASIC, false),
						Arguments.of(lessThanMinimumUserCreationDto, null, false),

						Arguments.of(moreThanMaximumUserCreationDto, ProfileEnum.ADMIN, true),
						Arguments.of(moreThanMaximumUserCreationDto, ProfileEnum.ADMIN, false),
						Arguments.of(moreThanMaximumUserCreationDto, ProfileEnum.BASIC, false),
						Arguments.of(moreThanMaximumUserCreationDto, null, false)
				);
			}

			private static Stream<Arguments> provideUserWithFailSizePasswordArguments() {
				UserCreationDto lessThanMinimumUserCreationDto = UserCreationDtoFactory.aUserCreationDto()
						.withPassword(PASSWORD_WITH_LESS_THAN_MINIMUM_CHARACTERS)
						.build();
				UserCreationDto moreThanMaximumUserCreationDto = UserCreationDtoFactory.aUserCreationDto()
						.withPassword(PASSWORD_WITH_MORE_THAN_MAXIMUM_CHARACTERS)
						.build();

				return Stream.of(
						Arguments.of(lessThanMinimumUserCreationDto, ProfileEnum.ADMIN, true),
						Arguments.of(lessThanMinimumUserCreationDto, ProfileEnum.ADMIN, false),
						Arguments.of(lessThanMinimumUserCreationDto, ProfileEnum.BASIC, false),
						Arguments.of(lessThanMinimumUserCreationDto, null, false),

						Arguments.of(moreThanMaximumUserCreationDto, ProfileEnum.ADMIN, true),
						Arguments.of(moreThanMaximumUserCreationDto, ProfileEnum.ADMIN, false),
						Arguments.of(moreThanMaximumUserCreationDto, ProfileEnum.BASIC, false),
						Arguments.of(moreThanMaximumUserCreationDto, null, false)
				);
			}

			private static Stream<Arguments> provideUserWithBlankFieldArguments() {
				UserCreationDto blankNameUserCreationDto = UserCreationDtoFactory.aUserCreationDto()
						.withName("")
						.build();
				UserCreationDto blankEmailUserCreationDto = UserCreationDtoFactory.aUserCreationDto()
						.withEmail("")
						.build();
				UserCreationDto blankPasswordUserCreationDto = UserCreationDtoFactory.aUserCreationDto()
						.withPassword("")
						.build();

				return Stream.of(
						Arguments.of(blankNameUserCreationDto, ProfileEnum.ADMIN, true, "name"),
						Arguments.of(blankNameUserCreationDto, ProfileEnum.ADMIN, false, "name"),
						Arguments.of(blankNameUserCreationDto, ProfileEnum.BASIC, false, "name"),
						Arguments.of(blankNameUserCreationDto, null, false, "name"),

						Arguments.of(blankEmailUserCreationDto, ProfileEnum.ADMIN, true, "email"),
						Arguments.of(blankEmailUserCreationDto, ProfileEnum.ADMIN, false, "email"),
						Arguments.of(blankEmailUserCreationDto, ProfileEnum.BASIC, false, "email"),
						Arguments.of(blankEmailUserCreationDto, null, false, "email"),

						Arguments.of(blankPasswordUserCreationDto, ProfileEnum.ADMIN, true, "password"),
						Arguments.of(blankPasswordUserCreationDto, ProfileEnum.ADMIN, false, "password"),
						Arguments.of(blankPasswordUserCreationDto, ProfileEnum.BASIC, false, "password"),
						Arguments.of(blankPasswordUserCreationDto, null, false, "password")
				);
			}

			private static Stream<Arguments> provideCorrectRequestArguments() {
				return Stream.of(
						Arguments.of(ProfileEnum.ADMIN, true),
						Arguments.of(ProfileEnum.ADMIN, false),
						Arguments.of(ProfileEnum.BASIC, false),
						Arguments.of(null, false)
				);
			}

			@DisplayName("With a user with incorrect size name information")
			@ParameterizedTest
			@MethodSource("provideUserWithFailSizeNameArguments")
			void createUser_shouldReturn404_whenIncorrectSizeName(UserCreationDto userCreationDto, ProfileEnum executorProfile, Boolean useAdminPath) throws JsonProcessingException {
				Response response = doCreateUserRequest(userCreationDto, executorProfile, useAdminPath);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto[] messages = response.as(GenericFieldErrorDto[].class);
				GenericFieldErrorDto message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals("name") && m.error().equals("size must be between 5 and 100"))
								.findFirst()
								.orElse(null);

				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With a user with incorrect size email information")
			@ParameterizedTest
			@MethodSource("provideUserWithFailSizeEmailArguments")
			void createUser_shouldReturn404_whenIncorrectSizeEmail(UserCreationDto userCreationDto, ProfileEnum executorProfile, Boolean useAdminPath) throws JsonProcessingException {
				Response response = doCreateUserRequest(userCreationDto, executorProfile, useAdminPath);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto[] messages = response.as(GenericFieldErrorDto[].class);
				GenericFieldErrorDto message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals("email") && m.error().equals("size must be between 10 and 72"))
								.findFirst()
								.orElse(null);

				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With a user with incorrect size password information")
			@ParameterizedTest
			@MethodSource("provideUserWithFailSizePasswordArguments")
			void createUser_shouldReturn404_whenIncorrectSizePassword(UserCreationDto userCreationDto, ProfileEnum executorProfile, Boolean useAdminPath) throws JsonProcessingException {
				Response response = doCreateUserRequest(userCreationDto, executorProfile, useAdminPath);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto[] messages = response.as(GenericFieldErrorDto[].class);
				GenericFieldErrorDto message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals("password") && m.error().equals("size must be between 10 and 72"))
								.findFirst()
								.orElse(null);

				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With a user without name")
			@ParameterizedTest
			@MethodSource("provideUserWithBlankFieldArguments")
			void createUser_shouldReturn404_whenBlankField(UserCreationDto userCreationDto, ProfileEnum executorProfile, Boolean useAdminPath, String blankField) throws JsonProcessingException {
				Response response = doCreateUserRequest(userCreationDto, executorProfile, useAdminPath);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto[] messages = response.as(GenericFieldErrorDto[].class);
				GenericFieldErrorDto message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals(blankField) && m.error().equals("must not be blank"))
								.findFirst()
								.orElse(null);

				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With a user with email already registered")
			@ParameterizedTest
			@MethodSource("provideCorrectRequestArguments")
			void createUser_shouldReturn404_whenEmailAlreadyRegistered(ProfileEnum executorProfile, Boolean useAdminPath) throws JsonProcessingException {
				UserCreationDto userCreationDto = UserCreationDtoFactory.aUserCreationDto()
						.withEmail(BASIC_REGISTERED_EMAIL)
						.build();

				Response response = doCreateUserRequest(userCreationDto, executorProfile, useAdminPath);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto message = response.as(GenericFieldErrorDto.class);
				assertThat(message.field())
						.isEqualTo("email");
				assertThat(message.error())
						.isEqualTo("email already registered");
			}

			@DisplayName("With a user without well-formed email address")
			@ParameterizedTest
			@MethodSource("provideCorrectRequestArguments")
			void createUser_shouldReturn404_whenBadFormedEmail(ProfileEnum executorProfile, Boolean useAdminPath) throws JsonProcessingException {
				UserCreationDto userCreationDto = UserCreationDtoFactory.aUserCreationDto()
						.withEmail(MALFORMED_EMAIL)
						.build();

				Response response = doCreateUserRequest(userCreationDto, executorProfile, useAdminPath);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto[] messages = response.as(GenericFieldErrorDto[].class);
				GenericFieldErrorDto message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals("email") && m.error().equals("must be a well-formed email address"))
								.findFirst()
								.orElse(null);

				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With a user without any valid field")
			@ParameterizedTest
			@MethodSource("provideCorrectRequestArguments")
			void createUser_shouldReturn404_whenWithoutAnyValidField(ProfileEnum executorProfile, Boolean useAdminPath) throws JsonProcessingException {
				UserCreationDto userCreationDto = UserCreationDtoFactory.aUserCreationDto()
						.withEmail(MALFORMED_EMAIL)
						.withName(NAME_WITH_MORE_THAN_MAXIMUM_CHARACTERS)
						.withPassword("")
						.build();

				Response response = doCreateUserRequest(userCreationDto, executorProfile, useAdminPath);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto[] messages = response.as(GenericFieldErrorDto[].class);
				GenericFieldErrorDto message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals("password") && m.error().equals("must not be blank"))
								.findFirst()
								.orElse(null);
				assertThat(message)
						.isNotNull();

				message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals("password") && m.error().equals("size must be between 10 and 72"))
								.findFirst()
								.orElse(null);
				assertThat(message)
						.isNotNull();

				message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals("email") && m.error().equals("must be a well-formed email address"))
								.findFirst()
								.orElse(null);
				assertThat(message)
						.isNotNull();

				message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals("name") && m.error().equals("size must be between 5 and 100"))
								.findFirst()
								.orElse(null);
				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With a user not authenticated as ADMIN")
			@Test
			void createUser_shouldReturn403_whenNotAuthenticated() throws JsonProcessingException {
				UserCreationDto userCreationDto = UserCreationDtoFactory.aUserCreationDto()
						.build();

				Response response = doCreateUserRequest(userCreationDto, null, true);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

			@DisplayName("With a user not authorized")
			@Test
			void createUser_shouldReturn403_whenNotAuthorized() throws JsonProcessingException {
				UserCreationDto userCreationDto = UserCreationDtoFactory.aUserCreationDto()
						.build();

				Response response = doCreateUserRequest(userCreationDto, ProfileEnum.BASIC, true);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

		}

	}

	@DisplayName("When updating a user")
	@Nested
	class Update {

		protected Response doUpdateUserRequest(UserUpdateDto userUpdateDto, ProfileEnum executorProfile, User userToBeUpdated) throws JsonProcessingException {
			String path = USER_PATH.concat("/").concat(String.valueOf(userToBeUpdated.getId()));
			if (ProfileEnum.ADMIN.equals(executorProfile)) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(adminToken))
						.body(objectMapper.writeValueAsString(userUpdateDto))
						.put(path)
						.then()
						.log().all()
						.extract()
						.response();
			}
			if (ProfileEnum.BASIC.equals(executorProfile)) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(basicToken))
						.body(objectMapper.writeValueAsString(userUpdateDto))
						.put(path)
						.then()
						.log().all()
						.extract()
						.response();
			}
			return given()
					.log().all()
					.contentType(JSON)
					.body(objectMapper.writeValueAsString(userUpdateDto))
					.put(path)
					.then()
					.log().all()
					.extract()
					.response();
		}

		@DisplayName("Must success")
		@Nested
		class Success {

			private static Stream<Arguments> provideUserSuccessArguments() {
				UserUpdateDto simpleUserUpdateDto = UserUpdateDtoFactory.aUserUpdateDto()
						.build();
				UserUpdateDto minimumUserUpdateDto = UserUpdateDtoFactory.aUserUpdateDto()
						.withName(NAME_WITH_MINIMUM_CHARACTERS)
						.build();
				UserUpdateDto maximumUserUpdateDto = UserUpdateDtoFactory.aUserUpdateDto()
						.withName(NAME_WITH_MAXIMUM_CHARACTERS)
						.build();
				UserUpdateDto specialNameUserUpdateDto = UserUpdateDtoFactory.aUserUpdateDto()
						.withName(NAME_WITH_ESPECIAL_CHARACTERS)
						.build();


				return Stream.of(
						Arguments.of(simpleUserUpdateDto, ProfileEnum.ADMIN),
						Arguments.of(simpleUserUpdateDto, ProfileEnum.BASIC),

						Arguments.of(minimumUserUpdateDto, ProfileEnum.ADMIN),
						Arguments.of(minimumUserUpdateDto, ProfileEnum.BASIC),

						Arguments.of(maximumUserUpdateDto, ProfileEnum.ADMIN),
						Arguments.of(maximumUserUpdateDto, ProfileEnum.BASIC),

						Arguments.of(specialNameUserUpdateDto, ProfileEnum.ADMIN),
						Arguments.of(specialNameUserUpdateDto, ProfileEnum.BASIC)
				);
			}

			@DisplayName("With a user with all correct information")
			@ParameterizedTest
			@MethodSource("provideUserSuccessArguments")
			void updateUser_shouldReturn200_whenAllInformationIsCorrect(UserUpdateDto userUpdateDto, ProfileEnum executorProfile) throws JsonProcessingException {
				User userToBeUpdated = executorProfile.equals(ProfileEnum.ADMIN) ? adminUser : basicUser;

				Response response = doUpdateUserRequest(userUpdateDto, executorProfile, userToBeUpdated);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.OK.value());

				UserResponseDto userResponseDto = response.as(UserResponseDto.class);
				assertThat(userResponseDto.getId())
						.isEqualTo(userToBeUpdated.getId());
				assertThat(userResponseDto.getName())
						.isEqualTo(userUpdateDto.getName());

				compareUserWithDatabaseUser(userResponseDto, false);
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			private static Stream<Arguments> provideUserWithFailSizeNameArguments() {
				UserUpdateDto lessThanMinimumUserUpdateDto = UserUpdateDtoFactory.aUserUpdateDto()
						.withName(NAME_WITH_LESS_THAN_MINIMUM_CHARACTERS)
						.build();
				UserUpdateDto moreThanMaximumUserCreationDto = UserUpdateDtoFactory.aUserUpdateDto()
						.withName(NAME_WITH_MORE_THAN_MAXIMUM_CHARACTERS)
						.build();

				return Stream.of(
						Arguments.of(lessThanMinimumUserUpdateDto, ProfileEnum.ADMIN),
						Arguments.of(lessThanMinimumUserUpdateDto, ProfileEnum.BASIC),

						Arguments.of(moreThanMaximumUserCreationDto, ProfileEnum.ADMIN),
						Arguments.of(moreThanMaximumUserCreationDto, ProfileEnum.BASIC)
				);
			}

			@DisplayName("With a user with incorrect size name information")
			@ParameterizedTest
			@MethodSource("provideUserWithFailSizeNameArguments")
			void updateUser_shouldReturn404_whenIncorrectSizeName(UserUpdateDto userUpdateDto, ProfileEnum executorProfile) throws JsonProcessingException {
				User userToBeUpdated = executorProfile.equals(ProfileEnum.ADMIN) ? adminUser : basicUser;

				Response response = doUpdateUserRequest(userUpdateDto, executorProfile, userToBeUpdated);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto[] messages = response.as(GenericFieldErrorDto[].class);
				GenericFieldErrorDto message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals("name") && m.error().equals("size must be between 5 and 100"))
								.findFirst()
								.orElse(null);

				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With a user without name")
			@ParameterizedTest
			@EnumSource(ProfileEnum.class)
			void updateUser_shouldReturn404_whenWithoutName(ProfileEnum executorProfile) throws JsonProcessingException {
				UserUpdateDto userUpdateDto = UserUpdateDtoFactory.aUserUpdateDto()
						.withName("")
						.build();

				User userToBeUpdated = executorProfile.equals(ProfileEnum.ADMIN) ? adminUser : basicUser;

				Response response = doUpdateUserRequest(userUpdateDto, executorProfile, userToBeUpdated);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto[] messages = response.as(GenericFieldErrorDto[].class);
				GenericFieldErrorDto message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals("name") && m.error().equals("must not be blank"))
								.findFirst()
								.orElse(null);

				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With a user not authenticated")
			@ParameterizedTest
			@EnumSource(ProfileEnum.class)
			void updateUser_shouldReturn403_whenNotAuthenticated(ProfileEnum targetProfile) throws JsonProcessingException {
				UserUpdateDto userUpdateDto = UserUpdateDtoFactory.aUserUpdateDto()
						.build();

				User userToBeUpdated = targetProfile.equals(ProfileEnum.ADMIN) ? adminUser : basicUser;

				Response response = doUpdateUserRequest(userUpdateDto, null, userToBeUpdated);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

			@DisplayName("With a user not authorized")
			@ParameterizedTest
			@EnumSource(ProfileEnum.class)
			void updateUser_shouldReturn401_whenNotAuthorized(ProfileEnum executorProfile) throws JsonProcessingException {
				UserUpdateDto userUpdateDto = UserUpdateDtoFactory.aUserUpdateDto()
						.build();

				User userToBeUpdated = executorProfile.equals(ProfileEnum.ADMIN) ? basicUser : adminUser;

				Response response = doUpdateUserRequest(userUpdateDto, executorProfile, userToBeUpdated);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.UNAUTHORIZED.value());
			}

			@DisplayName("With a update of a non existent user")
			@ParameterizedTest
			@EnumSource(ProfileEnum.class)
			void updateUser_shouldReturn403_whenUpdatingNonExistentUser(ProfileEnum executorProfile) throws JsonProcessingException {
				UserUpdateDto userUpdateDto = UserUpdateDtoFactory.aUserUpdateDto()
						.build();

				Long userIdNonExistent = 0L;
				User userToBeUpdated = new User();
				userToBeUpdated.setId(userIdNonExistent);

				Response response = doUpdateUserRequest(userUpdateDto, executorProfile, userToBeUpdated);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.UNAUTHORIZED.value());//Gives unauthorized because the check of permission occurs before the search of the non existent user
			}

		}

	}

	@DisplayName("When getting all users")
	@Nested
	class getAll {

		protected Response doGetAllUserRequest(ProfileEnum executorProfile) {
			if (ProfileEnum.ADMIN.equals(executorProfile)) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(adminToken))
						.get(USER_PATH)
						.then()
						.log().all()
						.extract()
						.response();
			}
			if (ProfileEnum.BASIC.equals(executorProfile)) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(basicToken))
						.get(USER_PATH)
						.then()
						.log().all()
						.extract()
						.response();
			}
			return given()
					.log().all()
					.contentType(JSON)
					.get(USER_PATH)
					.then()
					.log().all()
					.extract()
					.response();
		}

		@DisplayName("Must success")
		@Nested
		class Success {

			@DisplayName("With a admin user")
			@Test
			void getAllUser_shouldReturn200_whenUsingAdminUser() {
				Response response = doGetAllUserRequest(ProfileEnum.ADMIN);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.OK.value());

				int pageNumber = response.jsonPath().getInt("number");
				assertThat(pageNumber)
						.isEqualTo(0);

				int pageSize = response.jsonPath().getInt("size");
				assertThat(pageSize)
						.isEqualTo(10);

				int totalPages = response.jsonPath().getInt("totalPages");
				assertThat(totalPages)
						.isEqualTo(1);

				long totalElements = response.jsonPath().getLong("totalElements");
				assertThat(totalElements)
						.isEqualTo(2);

				boolean first = response.jsonPath().getBoolean("first");
				assertThat(first)
						.isEqualTo(true);

				boolean last = response.jsonPath().getBoolean("last");
				assertThat(last)
						.isEqualTo(true);

				List<UserResponseDto> users = response.jsonPath().getList("content", UserResponseDto.class);

				UserResponseDto user = users.stream()
						.filter(u -> u.getId().equals(adminUser.getId()))
						.findFirst()
						.orElse(null);
				assertThat(user)
						.isNotNull();
				compareUserWithDatabaseUser(user, false);

				user = users.stream()
						.filter(u -> u.getId().equals(basicUser.getId()))
						.findFirst()
						.orElse(null);
				assertThat(user)
						.isNotNull();
				compareUserWithDatabaseUser(user, false);
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			@DisplayName("With a user not authenticated")
			@Test
			void getAllUser_shouldReturn403_whenNotAuthenticated() {
				Response response = doGetAllUserRequest(null);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

			@DisplayName("With a user not authorized")
			@Test
			void getAllUser_shouldReturn403_whenNotAuthorized() {
				Response response = doGetAllUserRequest(ProfileEnum.BASIC);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

		}

	}

	@DisplayName("When getting a user")
	@Nested
	class Get {

		protected Response doGetUserRequest(ProfileEnum executorProfile, User userSearched) {
			String path = USER_PATH.concat("/").concat(String.valueOf(userSearched.getId()));
			if (ProfileEnum.ADMIN.equals(executorProfile)) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(adminToken))
						.get(path)
						.then()
						.log().all()
						.extract()
						.response();
			}
			if (ProfileEnum.BASIC.equals(executorProfile)) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(basicToken))
						.get(path)
						.then()
						.log().all()
						.extract()
						.response();
			}
			return given()
					.log().all()
					.contentType(JSON)
					.get(path)
					.then()
					.log().all()
					.extract()
					.response();
		}

		@DisplayName("Must success")
		@Nested
		class Success {

			protected void compareUserWithDatabaseUser(UserResponseDto userResponseDto) {
				Optional<User> userInDatabase = userRepository.findById(userResponseDto.getId());
				assertThat(userInDatabase)
						.isPresent();
				assertThat(userInDatabase.get().getId())
						.isEqualTo(userResponseDto.getId());
				assertThat(userInDatabase.get().getName())
						.isEqualTo(userResponseDto.getName());
				assertThat(userInDatabase.get().getEmail())
						.isEqualTo(userResponseDto.getEmail());
			}

			@DisplayName("With a user getting his own information")
			@ParameterizedTest
			@EnumSource(ProfileEnum.class)
			void getUser_shouldReturn200_whenUserGettingOwnInformation(ProfileEnum executorProfile) {
				User user = executorProfile.equals(ProfileEnum.ADMIN) ? adminUser : basicUser;

				Response response = doGetUserRequest(executorProfile, user);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.OK.value());

				UserResponseDto userResponseDto = response.as(UserResponseDto.class);
				assertThat(userResponseDto.getId())
						.isEqualTo(user.getId());
				assertThat(userResponseDto.getName())
						.isEqualTo(user.getName());
				assertThat(userResponseDto.getEmail())
						.isEqualTo(user.getEmail());

				compareUserWithDatabaseUser(userResponseDto);
			}

			@DisplayName("With a admin user getting information of another user")
			@Test
			void getUser_shouldReturn200_whenAdminUserGettingInformationOfAnotherUser() {
				Response response = doGetUserRequest(ProfileEnum.ADMIN, basicUser);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.OK.value());

				UserResponseDto userResponseDto = response.as(UserResponseDto.class);
				assertThat(userResponseDto.getId())
						.isEqualTo(basicUser.getId());
				assertThat(userResponseDto.getName())
						.isEqualTo(basicUser.getName());
				assertThat(userResponseDto.getEmail())
						.isEqualTo(basicUser.getEmail());

				compareUserWithDatabaseUser(userResponseDto);
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			@DisplayName("With a user not authenticated")
			@Test
			void getUser_shouldReturn403_whenNotAuthenticated() {
				Response response = doGetUserRequest(null, basicUser);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

			@DisplayName("With a user not authorized")
			@Test
			void getUser_shouldReturn401_whenNotAuthorized() {
				Response response = doGetUserRequest(ProfileEnum.BASIC, adminUser);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.UNAUTHORIZED.value());
			}

			@DisplayName("With a search of a non existent user")
			@Test
			void getUser_shouldReturn401_whenAdminSearchingNonExistentUser() {
				Long userIdNonExistent = 0L;
				User userToBeSearched = new User();
				userToBeSearched.setId(userIdNonExistent);

				Response response = doGetUserRequest(ProfileEnum.ADMIN, userToBeSearched);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.NOT_FOUND.value());
			}

			@DisplayName("With a search of a non existent user")
			@Test
			void getUser_shouldReturn401_whenBasicSearchingNonExistentUser() {
				Long userIdNonExistent = 0L;
				User userToBeSearched = new User();
				userToBeSearched.setId(userIdNonExistent);

				Response response = doGetUserRequest(ProfileEnum.BASIC, userToBeSearched);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.UNAUTHORIZED.value());//Gives unauthorized because the check of permission occurs before the search of the non existent user
			}

		}

	}

	@DisplayName("When deleting a user")
	@Nested
	class Delete {

		protected Response doDeleteUserRequest(ProfileEnum executorProfile, User userDeleted) {
			String path = USER_PATH.concat("/").concat(String.valueOf(userDeleted.getId()));
			if (ProfileEnum.ADMIN.equals(executorProfile)) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(adminToken))
						.delete(path)
						.then()
						.log().all()
						.extract()
						.response();
			}
			if (ProfileEnum.BASIC.equals(executorProfile)) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(basicToken))
						.delete(path)
						.then()
						.log().all()
						.extract()
						.response();
			}
			return given()
					.log().all()
					.contentType(JSON)
					.delete(path)
					.then()
					.log().all()
					.extract()
					.response();
		}

		@DisplayName("Must success")
		@Nested
		class Success {

			@DisplayName("With a user deleting his own information")
			@ParameterizedTest
			@EnumSource(ProfileEnum.class)
			void deleteUser_shouldReturn200_whenUserDeletingOwnInformation(ProfileEnum executorProfile) {
				User user = executorProfile.equals(ProfileEnum.ADMIN) ? adminUser : basicUser;

				Response response = doDeleteUserRequest(executorProfile, user);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.NO_CONTENT.value());

				Optional<User> userInDatabase = userRepository.findById(user.getId());
				assertThat(userInDatabase)
						.isNotPresent();
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			@DisplayName("With a user not authenticated")
			@ParameterizedTest
			@EnumSource(ProfileEnum.class)
			void deleteUser_shouldReturn403_whenNotAuthenticated(ProfileEnum targetProfile) {
				User user = targetProfile.equals(ProfileEnum.ADMIN) ? adminUser : basicUser;

				Response response = doDeleteUserRequest(null, user);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

			@DisplayName("With a user not authorized")
			@ParameterizedTest
			@EnumSource(ProfileEnum.class)
			void deleteUser_shouldReturn401_whenNotAuthorized(ProfileEnum executorProfile) {
				User user = executorProfile.equals(ProfileEnum.ADMIN) ? basicUser : adminUser;

				Response response = doDeleteUserRequest(executorProfile, user);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.UNAUTHORIZED.value());
			}

			@DisplayName("With a delete of a non existent user")
			@ParameterizedTest
			@EnumSource(ProfileEnum.class)
			void getUser_shouldReturn401_whenDeletingNonExistentUser(ProfileEnum executorProfile) {
				Long userIdNonExistent = 0L;
				User userToBeDeleted = new User();
				userToBeDeleted.setId(userIdNonExistent);

				Response response = doDeleteUserRequest(executorProfile, userToBeDeleted);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.UNAUTHORIZED.value());//Gives unauthorized because the check of permission occurs before the search of the non existent user
			}

		}

	}

	@DisplayName("When changing password")
	@Nested
	class ChangePassword {

		protected Response doChangePasswordRequest(ProfileEnum executorProfile, User userPasswordChanged, ChangePasswordDto changePasswordDto) throws JsonProcessingException {
			String path = USER_PATH.concat("/").concat(String.valueOf(userPasswordChanged.getId()).concat("/change-password"));
			if (ProfileEnum.ADMIN.equals(executorProfile)) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(adminToken))
						.body(objectMapper.writeValueAsString(changePasswordDto))
						.put(path)
						.then()
						.log().all()
						.extract()
						.response();
			}
			if (ProfileEnum.BASIC.equals(executorProfile)) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(basicToken))
						.body(objectMapper.writeValueAsString(changePasswordDto))
						.put(path)
						.then()
						.log().all()
						.extract()
						.response();
			}
			return given()
					.log().all()
					.contentType(JSON)
					.body(objectMapper.writeValueAsString(changePasswordDto))
					.put(path)
					.then()
					.log().all()
					.extract()
					.response();
		}

		@DisplayName("Must success")
		@Nested
		class Success {

			private static Stream<Arguments> provideCorrectPasswordArguments() {
				return Stream.of(
						Arguments.of(ProfileEnum.ADMIN, PASSWORD_WITH_MINIMUM_CHARACTERS),
						Arguments.of(ProfileEnum.BASIC, PASSWORD_WITH_MINIMUM_CHARACTERS),
						Arguments.of(ProfileEnum.ADMIN, PASSWORD_WITH_MAXIMUM_CHARACTERS),
						Arguments.of(ProfileEnum.BASIC, PASSWORD_WITH_MAXIMUM_CHARACTERS)
				);
			}

			@DisplayName("With a user changing his own password")
			@ParameterizedTest
			@MethodSource("provideCorrectPasswordArguments")
			void changePassword_shouldReturn200_whenUserChangingHisOwnInformation(ProfileEnum executorProfile, String newPassword) throws JsonProcessingException {
				User user = executorProfile.equals(ProfileEnum.ADMIN) ? adminUser : basicUser;

				ChangePasswordDto changePasswordDto = new ChangePasswordDto(PASSWORD, newPassword);

				Response response = doChangePasswordRequest(executorProfile, user, changePasswordDto);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.OK.value());

				GenericFieldResponseDto message = response.as(GenericFieldResponseDto.class);
				assertThat(message)
						.isNotNull();
				assertThat(message.getField())
						.isEqualTo("message");
				assertThat(message.getMessage())
						.isEqualTo("Password changed successfully");

				Optional<User> userInDatabase = userRepository.findById(user.getId());
				assertThat(userInDatabase)
						.isPresent();
				assertThat(SecurityConfiguration.getEncrypter().matches(newPassword, userInDatabase.get().getPassword()));
				assertThat(userInDatabase.get().getPasswordChangedAt())
						.isAfter(user.getPasswordChangedAt());
				assertThat(userInDatabase.get().getTokenVersion())
						.isGreaterThan(user.getTokenVersion());
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			private static Stream<Arguments> provideFailSizePasswordArguments() {
				return Stream.of(
						Arguments.of(ProfileEnum.ADMIN, PASSWORD_WITH_LESS_THAN_MINIMUM_CHARACTERS),
						Arguments.of(ProfileEnum.BASIC, PASSWORD_WITH_LESS_THAN_MINIMUM_CHARACTERS),

						Arguments.of(ProfileEnum.ADMIN, PASSWORD_WITH_MORE_THAN_MAXIMUM_CHARACTERS),
						Arguments.of(ProfileEnum.BASIC, PASSWORD_WITH_MORE_THAN_MAXIMUM_CHARACTERS)
				);
			}

			@DisplayName("With a user not authenticated")
			@ParameterizedTest
			@EnumSource(ProfileEnum.class)
			void changePassword_shouldReturn403_whenNotAuthenticated(ProfileEnum targetProfile) throws JsonProcessingException {
				User user = targetProfile.equals(ProfileEnum.ADMIN) ? adminUser : basicUser;

				ChangePasswordDto changePasswordDto = new ChangePasswordDto(PASSWORD, NEW_PASSWORD);

				Response response = doChangePasswordRequest(null, user, changePasswordDto);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

			@DisplayName("With a user not authorized")
			@ParameterizedTest
			@EnumSource(ProfileEnum.class)
			void changePassword_shouldReturn401_whenNotAuthorized(ProfileEnum executorProfile) throws JsonProcessingException {
				User user = executorProfile.equals(ProfileEnum.ADMIN) ? basicUser : adminUser;

				ChangePasswordDto changePasswordDto = new ChangePasswordDto(PASSWORD, NEW_PASSWORD);

				Response response = doChangePasswordRequest(executorProfile, user, changePasswordDto);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.UNAUTHORIZED.value());
			}

			@DisplayName("With a non existent user changing password")
			@ParameterizedTest
			@EnumSource(ProfileEnum.class)
			void changePassword_shouldReturn401_whenChangingNonExistentUser(ProfileEnum executorProfile) throws JsonProcessingException {
				Long userIdNonExistent = 0L;
				User userToBeChanged = new User();
				userToBeChanged.setId(userIdNonExistent);

				ChangePasswordDto changePasswordDto = new ChangePasswordDto(PASSWORD, NEW_PASSWORD);

				Response response = doChangePasswordRequest(executorProfile, userToBeChanged, changePasswordDto);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.UNAUTHORIZED.value());//Gives unauthorized because the check of permission occurs before the search of the non existent user
			}

			@DisplayName("With a incorrect current password")
			@ParameterizedTest
			@EnumSource(ProfileEnum.class)
			void changePassword_shouldReturn404_whenCurrentPasswordIncorrect(ProfileEnum executorProfile) throws JsonProcessingException {
				User user = executorProfile.equals(ProfileEnum.ADMIN) ? adminUser : basicUser;

				ChangePasswordDto changePasswordDto = new ChangePasswordDto(WRONG_PASSWORD, NEW_PASSWORD);

				Response response = doChangePasswordRequest(executorProfile, user, changePasswordDto);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto message = response.as(GenericFieldErrorDto.class);
				assertThat(message)
						.isNotNull();
				assertThat(message.field())
						.isNull();
				assertThat(message.error())
						.isEqualTo("Email or password is incorrect");
			}

			@DisplayName("With a incorrect current password")
			@ParameterizedTest
			@MethodSource("provideFailSizePasswordArguments")
			void changePassword_shouldReturn404_whenPasswordSizeIsIncorrect(ProfileEnum executorProfile, String newPassword) throws JsonProcessingException {
				User user = executorProfile.equals(ProfileEnum.ADMIN) ? adminUser : basicUser;

				ChangePasswordDto changePasswordDto = new ChangePasswordDto(PASSWORD, newPassword);

				Response response = doChangePasswordRequest(executorProfile, user, changePasswordDto);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto[] messages = response.as(GenericFieldErrorDto[].class);
				GenericFieldErrorDto message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals("newPassword") && m.error().equals("size must be between 10 and 72"))
								.findFirst()
								.orElse(null);

				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With a user without name")
			@ParameterizedTest
			@EnumSource(ProfileEnum.class)
			void changePassword_shouldReturn404_whenBlankField(ProfileEnum executorProfile) throws JsonProcessingException {
				User user = executorProfile.equals(ProfileEnum.ADMIN) ? adminUser : basicUser;

				ChangePasswordDto changePasswordDto = new ChangePasswordDto(PASSWORD, "");

				Response response = doChangePasswordRequest(executorProfile, user, changePasswordDto);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto[] messages = response.as(GenericFieldErrorDto[].class);
				GenericFieldErrorDto message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals("newPassword") && m.error().equals("must not be blank"))
								.findFirst()
								.orElse(null);

				assertThat(message)
						.isNotNull();
			}

		}

	}
}