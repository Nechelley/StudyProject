package com.study_project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study_project.config.RestControllerTestConfig;
import com.study_project.controller.dto.*;
import com.study_project.enums.AttributeEnum;
import com.study_project.factory.PlayerCharacterCreationDtoFactory;
import com.study_project.factory.PlayerCharacterUpdateDtoFactory;
import com.study_project.model.PlayerCharacter;
import com.study_project.model.User;
import com.study_project.record.ExecutorUser;
import com.study_project.repository.PlayerCharacterRepository;
import com.study_project.repository.UserRepository;
import com.study_project.setup.MysqlSetup;
import com.study_project.validation.dto.GenericFieldErrorDto;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
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
public class PlayerCharacterControllerIT extends AbstractIT implements MysqlSetup {

	private static final short MAX_INITIAL_ATTRIBUTE = 15;
	private static final String BEARER_PREFIX = "Bearer ";
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String CHARACTER_PATH = "/player-character";
	private static final short INITIAL_LEVEL = 1;

	private static final String NAME_WITH_MINIMUM_CHARACTERS = "teste";
	private static final String NAME_WITH_MAXIMUM_CHARACTERS = "100charactersNameaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	private static final String NAME_WITH_LESS_THAN_MINIMUM_CHARACTERS = "john";
	private static final String NAME_WITH_MORE_THAN_MAXIMUM_CHARACTERS = "johnnyaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	private static final String NAME_WITH_ESPECIAL_CHARACTERS = "Sáçoul LeTest";
	public static final short LESS_THAN_MINIMUM_ATTRIBUTE = 9;
	public static final short MORE_THAN_MAXIMUM_ATTRIBUTE = 501;
	public static final short MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE = 16;

	@LocalServerPort
	private Integer port;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PlayerCharacterRepository playerCharacterRepository;

	@BeforeEach
	void beforeSetup() {
		RestAssured.baseURI = String.format("http://localhost:%s", port);

		cleanup();
	}

	@AfterEach
	void afterSetup() {
		cleanup();
	}

	private void cleanup() {
		playerCharacterRepository.deleteAll();
		userRepository.deleteAll();
	}

	protected void comparePlayerCharacterWithDatabasePlayerCharacter(PlayerCharacterResponseDto playerCharacterResponseDto, User user) {
		Optional<PlayerCharacter> playerCharacterInDatabase = playerCharacterRepository.findById(playerCharacterResponseDto.getId());
		assertThat(playerCharacterInDatabase)
				.isPresent();
		assertThat(playerCharacterInDatabase.get().getId())
				.isEqualTo(playerCharacterResponseDto.getId());
		assertThat(playerCharacterInDatabase.get().getName())
				.isEqualTo(playerCharacterResponseDto.getName());
		assertThat(playerCharacterInDatabase.get().getBaseUnitAttributes().getLevel())
				.isEqualTo(playerCharacterResponseDto.getLevel());
		assertThat(playerCharacterInDatabase.get().getBaseUnitAttributes().getStrength())
				.isEqualTo(playerCharacterResponseDto.getStrength());
		assertThat(playerCharacterInDatabase.get().getBaseUnitAttributes().getDexterity())
				.isEqualTo(playerCharacterResponseDto.getDexterity());
		assertThat(playerCharacterInDatabase.get().getBaseUnitAttributes().getIntelligence())
				.isEqualTo(playerCharacterResponseDto.getIntelligence());
		assertThat(playerCharacterInDatabase.get().getBaseUnitAttributes().getConstitution())
				.isEqualTo(playerCharacterResponseDto.getConstitution());
		assertThat(playerCharacterInDatabase.get().getBaseUnitAttributes().getWillpower())
				.isEqualTo(playerCharacterResponseDto.getWillpower());
		assertThat(playerCharacterInDatabase.get().getBaseUnitAttributes().getPerception())
				.isEqualTo(playerCharacterResponseDto.getPerception());
		assertThat(playerCharacterInDatabase.get().getBaseUnitAttributes().getLuck())
				.isEqualTo(playerCharacterResponseDto.getLuck());
		assertThat(playerCharacterInDatabase.get().getUser().getId())
				.isEqualTo(user.getId());
	}

	@DisplayName("When creating a initial player character")
	@Nested
	class Create {

		protected Response doCreatePlayerCharacterRequest(PlayerCharacterCreationDto playerCharacterCreationDto, String executorToken) throws JsonProcessingException {
			if (executorToken != null) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(executorToken))
						.body(objectMapper.writeValueAsString(playerCharacterCreationDto))
						.post(CHARACTER_PATH)
						.then()
						.log().all()
						.extract()
						.response();
			}

			return given()
					.log().all()
					.contentType(JSON)
					.body(objectMapper.writeValueAsString(playerCharacterCreationDto))
					.post(CHARACTER_PATH)
					.then()
					.log().all()
					.extract()
					.response();
		}

		@DisplayName("Must success")
		@Nested
		class Success {

			private static Stream<Arguments> providePlayerCharacterSuccessArguments() {
				PlayerCharacterCreationDto playerCharacterCreationDtoWithoutDistribution = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.build();
				PlayerCharacterCreationDto playerCharacterCreationDtoWithMinimumName = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withName(NAME_WITH_MINIMUM_CHARACTERS)
						.build();
				PlayerCharacterCreationDto playerCharacterCreationDtoWithMaximumName = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withName(NAME_WITH_MAXIMUM_CHARACTERS)
						.build();
				PlayerCharacterCreationDto playerCharacterCreationDtoWithSpecialName = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withName(NAME_WITH_ESPECIAL_CHARACTERS)
						.build();
				PlayerCharacterCreationDto playerCharacterCreationDtoWithPointsDistributed = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withStrength((short) 11)
						.withDexterity((short) 11)
						.withIntelligence((short) 11)
						.withConstitution((short) 11)
						.withWillpower((short) 11)
						.build();
				PlayerCharacterCreationDto playerCharacterCreationDtoWithMaxStrength = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withStrength(MAX_INITIAL_ATTRIBUTE)
						.build();
				PlayerCharacterCreationDto playerCharacterCreationDtoWithMaxDexterity = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withDexterity(MAX_INITIAL_ATTRIBUTE)
						.build();
				PlayerCharacterCreationDto playerCharacterCreationDtoWithMaxIntelligence = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withIntelligence(MAX_INITIAL_ATTRIBUTE)
						.build();
				PlayerCharacterCreationDto playerCharacterCreationDtoWithMaxConstitution = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withConstitution(MAX_INITIAL_ATTRIBUTE)
						.build();
				PlayerCharacterCreationDto playerCharacterCreationDtoWithMaxWillpower = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withWillpower(MAX_INITIAL_ATTRIBUTE)
						.build();
				PlayerCharacterCreationDto playerCharacterCreationDtoWithMaxPerception = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withPerception(MAX_INITIAL_ATTRIBUTE)
						.build();
				PlayerCharacterCreationDto playerCharacterCreationDtoWithMaxLuck = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withLuck(MAX_INITIAL_ATTRIBUTE)
						.build();

				return Stream.of(
						Arguments.of(playerCharacterCreationDtoWithoutDistribution),
						Arguments.of(playerCharacterCreationDtoWithMinimumName),
						Arguments.of(playerCharacterCreationDtoWithMaximumName),
						Arguments.of(playerCharacterCreationDtoWithSpecialName),
						Arguments.of(playerCharacterCreationDtoWithPointsDistributed),
						Arguments.of(playerCharacterCreationDtoWithMaxStrength),
						Arguments.of(playerCharacterCreationDtoWithMaxDexterity),
						Arguments.of(playerCharacterCreationDtoWithMaxIntelligence),
						Arguments.of(playerCharacterCreationDtoWithMaxConstitution),
						Arguments.of(playerCharacterCreationDtoWithMaxWillpower),
						Arguments.of(playerCharacterCreationDtoWithMaxPerception),
						Arguments.of(playerCharacterCreationDtoWithMaxLuck)
				);
			}

			@DisplayName("With a player character with all correct information")
			@ParameterizedTest
			@MethodSource("providePlayerCharacterSuccessArguments")
			void createPlayerCharacter_shouldReturn201_whenAllInformationIsCorrect(PlayerCharacterCreationDto playerCharacterCreationDto) throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();

				Response response = doCreatePlayerCharacterRequest(playerCharacterCreationDto, executorUser.token());

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.CREATED.value());

				PlayerCharacterResponseDto playerCharacterResponseDto = response.as(PlayerCharacterResponseDto.class);
				assertThat(playerCharacterResponseDto.getId())
						.isNotNull();
				assertThat(playerCharacterResponseDto.getName())
						.isEqualTo(playerCharacterCreationDto.getName());
				assertThat(playerCharacterResponseDto.getLevel())
						.isEqualTo(INITIAL_LEVEL);
				assertThat(playerCharacterResponseDto.getStrength())
						.isEqualTo(playerCharacterCreationDto.getStrength());
				assertThat(playerCharacterResponseDto.getDexterity())
						.isEqualTo(playerCharacterCreationDto.getDexterity());
				assertThat(playerCharacterResponseDto.getIntelligence())
						.isEqualTo(playerCharacterCreationDto.getIntelligence());
				assertThat(playerCharacterResponseDto.getConstitution())
						.isEqualTo(playerCharacterCreationDto.getConstitution());
				assertThat(playerCharacterResponseDto.getWillpower())
						.isEqualTo(playerCharacterCreationDto.getWillpower());
				assertThat(playerCharacterResponseDto.getPerception())
						.isEqualTo(playerCharacterCreationDto.getPerception());
				assertThat(playerCharacterResponseDto.getLuck())
						.isEqualTo(playerCharacterCreationDto.getLuck());

				comparePlayerCharacterWithDatabasePlayerCharacter(playerCharacterResponseDto, executorUser.user());
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			private static Stream<Arguments> providePlayerCharacterWithFailSizeNameArguments() {
				PlayerCharacterCreationDto lessThanMinimumPlayerCharacterCreationDto = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withName(NAME_WITH_LESS_THAN_MINIMUM_CHARACTERS)
						.build();
				PlayerCharacterCreationDto moreThanMaximumPlayerCharacterCreationDto = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withName(NAME_WITH_MORE_THAN_MAXIMUM_CHARACTERS)
						.build();

				return Stream.of(
						Arguments.of(lessThanMinimumPlayerCharacterCreationDto),
						Arguments.of(moreThanMaximumPlayerCharacterCreationDto)
				);
			}

			private static Stream<Arguments> providePlayerCharacterWithBlankFieldArguments() {
				PlayerCharacterCreationDto blankStrengthPlayerCharacterCreationDto = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withStrength(null)
						.build();
				PlayerCharacterCreationDto blankDexterityPlayerCharacterCreationDto = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withDexterity(null)
						.build();
				PlayerCharacterCreationDto blankIntelligencePlayerCharacterCreationDto = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withIntelligence(null)
						.build();
				PlayerCharacterCreationDto blankConstitutionPlayerCharacterCreationDto = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withConstitution(null)
						.build();
				PlayerCharacterCreationDto blankWillpowerPlayerCharacterCreationDto = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withWillpower(null)
						.build();
				PlayerCharacterCreationDto blankPerceptionPlayerCharacterCreationDto = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withPerception(null)
						.build();
				PlayerCharacterCreationDto blankLuckPlayerCharacterCreationDto = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withLuck(null)
						.build();

				return Stream.of(
						Arguments.of(blankStrengthPlayerCharacterCreationDto, "strength"),
						Arguments.of(blankDexterityPlayerCharacterCreationDto, "dexterity"),
						Arguments.of(blankIntelligencePlayerCharacterCreationDto, "intelligence"),
						Arguments.of(blankConstitutionPlayerCharacterCreationDto, "constitution"),
						Arguments.of(blankWillpowerPlayerCharacterCreationDto, "willpower"),
						Arguments.of(blankPerceptionPlayerCharacterCreationDto, "perception"),
						Arguments.of(blankLuckPlayerCharacterCreationDto, "luck")
				);
			}

			@DisplayName("With a player character with incorrect size name")
			@ParameterizedTest
			@MethodSource("providePlayerCharacterWithFailSizeNameArguments")
			void createPlayerCharacter_shouldReturn404_whenIncorrectSizeName(PlayerCharacterCreationDto playerCharacterCreationDto) throws JsonProcessingException {
				Response response = doCreatePlayerCharacterRequest(playerCharacterCreationDto, executorUserFactory.generateBasic().token());

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto[] messages = response.as(GenericFieldErrorDto[].class);
				GenericFieldErrorDto message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals("name") && m.error().equals("length must be between 5 and 100"))
								.findFirst()
								.orElse(null);

				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With a player character with less than minimum attribute value")
			@ParameterizedTest
			@EnumSource(AttributeEnum.class)
			void createPlayerCharacter_shouldReturn404_whenLessThanMinimumAttributeValue(AttributeEnum attribute) throws JsonProcessingException {
				PlayerCharacterCreationDto playerCharacterCreationDto = switch (attribute) {
					case AttributeEnum.STRENGTH -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withStrength(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.DEXTERITY -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withDexterity(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.INTELLIGENCE -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withIntelligence(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.CONSTITUTION -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withConstitution(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.WILLPOWER -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withWillpower(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.PERCEPTION -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withPerception(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.LUCK -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withLuck(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
				};

				Response response = doCreatePlayerCharacterRequest(playerCharacterCreationDto, executorUserFactory.generateBasic().token());

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto[] messages = response.as(GenericFieldErrorDto[].class);
				GenericFieldErrorDto message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals(attribute.getName().toLowerCase()) && m.error().equals("must be greater than or equal to 10"))
								.findFirst()
								.orElse(null);

				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With a player character with more than maximum attribute value")
			@ParameterizedTest
			@EnumSource(AttributeEnum.class)
			void createPlayerCharacter_shouldReturn404_whenMoreThanMaximumAttributeValue(AttributeEnum attribute) throws JsonProcessingException {
				PlayerCharacterCreationDto playerCharacterCreationDto = switch (attribute) {
					case AttributeEnum.STRENGTH -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withStrength(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.DEXTERITY -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withDexterity(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.INTELLIGENCE -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withIntelligence(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.CONSTITUTION -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withConstitution(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.WILLPOWER -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withWillpower(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.PERCEPTION -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withPerception(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.LUCK -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withLuck(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
				};

				Response response = doCreatePlayerCharacterRequest(playerCharacterCreationDto, executorUserFactory.generateBasic().token());

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto[] messages = response.as(GenericFieldErrorDto[].class);
				GenericFieldErrorDto message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals(attribute.getName().toLowerCase()) && m.error().equals("must be less than or equal to 500"))
								.findFirst()
								.orElse(null);

				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With a player character with more than maximum distributed attribute value")
			@ParameterizedTest
			@EnumSource(AttributeEnum.class)
			void createPlayerCharacter_shouldReturn404_whenMoreThanMaximumDistributedAttributeValue(AttributeEnum attribute) throws JsonProcessingException {
				PlayerCharacterCreationDto playerCharacterCreationDto = switch (attribute) {
					case AttributeEnum.STRENGTH -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withStrength(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
					case AttributeEnum.DEXTERITY -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withDexterity(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
					case AttributeEnum.INTELLIGENCE -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withIntelligence(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
					case AttributeEnum.CONSTITUTION -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withConstitution(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
					case AttributeEnum.WILLPOWER -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withWillpower(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
					case AttributeEnum.PERCEPTION -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withPerception(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
					case AttributeEnum.LUCK -> PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
							.withLuck(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
				};

				Response response = doCreatePlayerCharacterRequest(playerCharacterCreationDto, executorUserFactory.generateBasic().token());

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto message = response.as(GenericFieldErrorDto.class);
				assertThat(message.error())
						.isEqualTo("Player character attributes not valid");

				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With a player character with blank attribute")
			@ParameterizedTest
			@MethodSource("providePlayerCharacterWithBlankFieldArguments")
			void createPlayerCharacter_shouldReturn404_whenBlankAttributeValue(PlayerCharacterCreationDto playerCharacterCreationDto, String field) throws JsonProcessingException {
				Response response = doCreatePlayerCharacterRequest(playerCharacterCreationDto, executorUserFactory.generateBasic().token());

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto[] messages = response.as(GenericFieldErrorDto[].class);
				GenericFieldErrorDto message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals(field) && m.error().equals("must not be null"))
								.findFirst()
								.orElse(null);

				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With a player character with blank name")
			@Test
			void createPlayerCharacter_shouldReturn404_whenBlankNameValue() throws JsonProcessingException {
				PlayerCharacterCreationDto blankNamePlayerCharacterCreationDto = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.withName("")
						.build();

				Response response = doCreatePlayerCharacterRequest(blankNamePlayerCharacterCreationDto, executorUserFactory.generateBasic().token());

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
			@Test
			void createPlayerCharacter_shouldReturn403_whenNotAuthenticated() throws JsonProcessingException {
				PlayerCharacterCreationDto playerCharacterCreationDto = PlayerCharacterCreationDtoFactory.aPlayerCharacterCreationDto()
						.build();

				Response response = doCreatePlayerCharacterRequest(playerCharacterCreationDto, null);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

		}

	}

	@DisplayName("When updating a player character")
	@Nested
	class Update {

		protected Response doUpdatePlayerCharacterRequest(PlayerCharacterUpdateDto playerCharacterUpdateDto, String executorToken, PlayerCharacter playerCharacterToBeUpdated) throws JsonProcessingException {
			String path = CHARACTER_PATH.concat("/").concat(String.valueOf(playerCharacterToBeUpdated.getId()));

			if (executorToken != null) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(executorToken))
						.body(objectMapper.writeValueAsString(playerCharacterUpdateDto))
						.put(path)
						.then()
						.log().all()
						.extract()
						.response();
			}
			return given()
					.log().all()
					.contentType(JSON)
					.body(objectMapper.writeValueAsString(playerCharacterUpdateDto))
					.put(path)
					.then()
					.log().all()
					.extract()
					.response();
		}

		@DisplayName("Must success")
		@Nested
		class Success {

			private static Stream<Arguments> providePlayerCharacterSuccessArguments() {
				PlayerCharacterUpdateDto simplePlayerCharacterUpdateDto = PlayerCharacterUpdateDtoFactory.aPlayerCharacterUpdateDto()
						.build();
				PlayerCharacterUpdateDto minimumPlayerCharacterUpdateDto = PlayerCharacterUpdateDtoFactory.aPlayerCharacterUpdateDto()
						.withName(NAME_WITH_MINIMUM_CHARACTERS)
						.build();
				PlayerCharacterUpdateDto maximumPlayerCharacterUpdateDto = PlayerCharacterUpdateDtoFactory.aPlayerCharacterUpdateDto()
						.withName(NAME_WITH_MAXIMUM_CHARACTERS)
						.build();
				PlayerCharacterUpdateDto specialNamePlayerCharacterUpdateDto = PlayerCharacterUpdateDtoFactory.aPlayerCharacterUpdateDto()
						.withName(NAME_WITH_ESPECIAL_CHARACTERS)
						.build();


				return Stream.of(
						Arguments.of(simplePlayerCharacterUpdateDto),
						Arguments.of(minimumPlayerCharacterUpdateDto),
						Arguments.of(maximumPlayerCharacterUpdateDto),
						Arguments.of(specialNamePlayerCharacterUpdateDto)
				);
			}

			@DisplayName("With a player character with all correct information")
			@ParameterizedTest
			@MethodSource("providePlayerCharacterSuccessArguments")
			void updatePlayerCharacter_shouldReturn200_whenAllInformationIsCorrect(PlayerCharacterUpdateDto playerCharacterUpdateDto) throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				PlayerCharacter playerCharacter = playerCharacterFactory.generateWithHighStrength(executorUser.user());

				Response response = doUpdatePlayerCharacterRequest(playerCharacterUpdateDto, executorUser.token(), playerCharacter);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.OK.value());

				PlayerCharacterResponseDto playerCharacterResponseDto = response.as(PlayerCharacterResponseDto.class);
				assertThat(playerCharacterResponseDto.getId())
						.isEqualTo(playerCharacter.getId());
				assertThat(playerCharacterResponseDto.getName())
						.isEqualTo(playerCharacterUpdateDto.getName());

				comparePlayerCharacterWithDatabasePlayerCharacter(playerCharacterResponseDto, executorUser.user());
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			private static Stream<Arguments> providePlayerCharacterWithFailSizeNameArguments() {
				PlayerCharacterUpdateDto lessThanMinimumPlayerCharacterUpdateDto = PlayerCharacterUpdateDtoFactory.aPlayerCharacterUpdateDto()
						.withName(NAME_WITH_LESS_THAN_MINIMUM_CHARACTERS)
						.build();
				PlayerCharacterUpdateDto moreThanMaximumCharacterCreationDto = PlayerCharacterUpdateDtoFactory.aPlayerCharacterUpdateDto()
						.withName(NAME_WITH_MORE_THAN_MAXIMUM_CHARACTERS)
						.build();

				return Stream.of(
						Arguments.of(lessThanMinimumPlayerCharacterUpdateDto),
						Arguments.of(moreThanMaximumCharacterCreationDto)
				);
			}

			@DisplayName("With a player character with incorrect size name information")
			@ParameterizedTest
			@MethodSource("providePlayerCharacterWithFailSizeNameArguments")
			void updatePlayerCharacter_shouldReturn404_whenIncorrectSizeName(PlayerCharacterUpdateDto playerCharacterUpdateDto) throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				PlayerCharacter playerCharacter = playerCharacterFactory.generateWithHighStrength(executorUser.user());

				Response response = doUpdatePlayerCharacterRequest(playerCharacterUpdateDto, executorUser.token(), playerCharacter);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto[] messages = response.as(GenericFieldErrorDto[].class);
				GenericFieldErrorDto message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals("name") && m.error().equals("length must be between 5 and 100"))
								.findFirst()
								.orElse(null);

				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With a player character without name")
			@Test
			void updatePlayerCharacter_shouldReturn404_whenWithoutName() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				PlayerCharacter playerCharacter = playerCharacterFactory.generateWithHighStrength(executorUser.user());

				PlayerCharacterUpdateDto playerCharacterUpdateDto = PlayerCharacterUpdateDtoFactory.aPlayerCharacterUpdateDto()
						.withName("")
						.build();

				Response response = doUpdatePlayerCharacterRequest(playerCharacterUpdateDto, executorUser.token(), playerCharacter);

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

			@DisplayName("With a player character not authenticated")
			@Test
			void updatePlayerCharacter_shouldReturn403_whenNotAuthenticated() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				PlayerCharacter playerCharacter = playerCharacterFactory.generateWithHighStrength(executorUser.user());

				PlayerCharacterUpdateDto playerCharacterUpdateDto = PlayerCharacterUpdateDtoFactory.aPlayerCharacterUpdateDto()
						.build();

				Response response = doUpdatePlayerCharacterRequest(playerCharacterUpdateDto, null, playerCharacter);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

			@DisplayName("With a player character not authorized")
			@Test
			void updatePlayerCharacter_shouldReturn401_whenNotAuthorized() throws JsonProcessingException {
				ExecutorUser ownerUser = executorUserFactory.generateBasic();
				ExecutorUser executorUser = executorUserFactory.generateBasicSecondary();
				PlayerCharacter playerCharacter = playerCharacterFactory.generateWithHighStrength(ownerUser.user());

				PlayerCharacterUpdateDto playerCharacterUpdateDto = PlayerCharacterUpdateDtoFactory.aPlayerCharacterUpdateDto()
						.build();

				Response response = doUpdatePlayerCharacterRequest(playerCharacterUpdateDto, executorUser.token(), playerCharacter);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.UNAUTHORIZED.value());
			}

			@DisplayName("With a update of a non existent player character")
			@Test
			void updatePlayerCharacter_shouldReturn404_whenUpdatingNonExistentCharacter() throws JsonProcessingException {
				PlayerCharacterUpdateDto playerCharacterUpdateDto = PlayerCharacterUpdateDtoFactory.aPlayerCharacterUpdateDto()
						.build();

				Long playerCharacterIdNonExistent = 0L;
				PlayerCharacter playerCharacterToBeUpdated = new PlayerCharacter();
				playerCharacterToBeUpdated.setId(playerCharacterIdNonExistent);

				Response response = doUpdatePlayerCharacterRequest(playerCharacterUpdateDto, executorUserFactory.generateBasic().token(), playerCharacterToBeUpdated);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.NOT_FOUND.value());
			}

		}

	}

	@DisplayName("When getting all player characters from a user")
	@Nested
	class getAll {

		protected Response doGetAllPlayerCharacterFromUserRequest(String executorToken) {
			if (executorToken != null) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(executorToken))
						.get(CHARACTER_PATH)
						.then()
						.log().all()
						.extract()
						.response();
			}
			return given()
					.log().all()
					.contentType(JSON)
					.get(CHARACTER_PATH)
					.then()
					.log().all()
					.extract()
					.response();
		}

		@DisplayName("Must success")
		@Nested
		class Success {

			@DisplayName("With a user with none player character")
			@Test
			void getAllPlayerCharacter_shouldReturn200_whenUsingUserWithNonePlayerCharacter() throws JsonProcessingException {
				//creating playerCharacters in db for the search
				ExecutorUser executorUser = executorUserFactory.generateBasic();

				Response response = doGetAllPlayerCharacterFromUserRequest(executorUser.token());

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
						.isEqualTo(0);

				long totalElements = response.jsonPath().getLong("totalElements");
				assertThat(totalElements)
						.isEqualTo(0);

				boolean first = response.jsonPath().getBoolean("first");
				assertThat(first)
						.isEqualTo(true);

				boolean last = response.jsonPath().getBoolean("last");
				assertThat(last)
						.isEqualTo(true);

				List<PlayerCharacterResponseDto> playerCharacters = response.jsonPath().getList("content", PlayerCharacterResponseDto.class);

				assertThat(playerCharacters.size())
						.isEqualTo(0);
			}

			@DisplayName("With a user with one player character")
			@Test
			void getAllPlayerCharacter_shouldReturn200_whenUsingUserWithOnePlayerCharacter() throws JsonProcessingException {
				//creating playerCharacters in db for the search
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				PlayerCharacter playerCharacter = playerCharacterFactory.generateWithHighStrength(executorUser.user());

				Response response = doGetAllPlayerCharacterFromUserRequest(executorUser.token());

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
						.isEqualTo(1);

				boolean first = response.jsonPath().getBoolean("first");
				assertThat(first)
						.isEqualTo(true);

				boolean last = response.jsonPath().getBoolean("last");
				assertThat(last)
						.isEqualTo(true);

				List<PlayerCharacterResponseDto> playerCharacters = response.jsonPath().getList("content", PlayerCharacterResponseDto.class);

				PlayerCharacterResponseDto playerCharacterResponse = playerCharacters.stream()
						.filter(u -> u.getId().equals(playerCharacter.getId()))
						.findFirst()
						.orElse(null);
				assertThat(playerCharacterResponse)
						.isNotNull();
				comparePlayerCharacterWithDatabasePlayerCharacter(playerCharacterResponse, executorUser.user());
			}

			@DisplayName("With a user with more than one player character")
			@Test
			void getAllPlayerCharacter_shouldReturn200_whenUsingUserWithMoreThanOnePlayerCharacter() throws JsonProcessingException {
				//creating playerCharactersResponseDto in db for the search
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				PlayerCharacter playerCharacter = playerCharacterFactory.generateWithHighStrength(executorUser.user());
				PlayerCharacter secondaryPlayerCharacter = playerCharacterFactory.generateWithHighDexterity(executorUser.user());

				Response response = doGetAllPlayerCharacterFromUserRequest(executorUser.token());

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

				List<PlayerCharacterResponseDto> playerCharactersResponseDto = response.jsonPath().getList("content", PlayerCharacterResponseDto.class);

				PlayerCharacterResponseDto playerCharacterResponse = playerCharactersResponseDto.stream()
						.filter(u -> u.getId().equals(playerCharacter.getId()))
						.findFirst()
						.orElse(null);
				assertThat(playerCharacterResponse)
						.isNotNull();
				comparePlayerCharacterWithDatabasePlayerCharacter(playerCharacterResponse, executorUser.user());

				playerCharacterResponse = playerCharactersResponseDto.stream()
						.filter(u -> u.getId().equals(secondaryPlayerCharacter.getId()))
						.findFirst()
						.orElse(null);
				assertThat(playerCharacterResponse)
						.isNotNull();
				comparePlayerCharacterWithDatabasePlayerCharacter(playerCharacterResponse, executorUser.user());
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			@DisplayName("With a player character not authenticated")
			@Test
			void getAllPlayerCharacter_shouldReturn403_whenNotAuthenticated() {
				Response response = doGetAllPlayerCharacterFromUserRequest(null);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

		}

	}

	@DisplayName("When getting a player character")
	@Nested
	class Get {

		protected Response doGetPlayerCharacterRequest(String executorToken, PlayerCharacter playerCharacterSearched) {
			String path = CHARACTER_PATH.concat("/").concat(String.valueOf(playerCharacterSearched.getId()));

			if (executorToken != null) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(executorToken))
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

			@DisplayName("With a user getting a player character he own")
			@Test
			void getPlayerCharacter_shouldReturn200_whenUserGettingOwnPlayerCharacter() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				PlayerCharacter playerCharacter = playerCharacterFactory.generateWithHighStrength(executorUser.user());

				Response response = doGetPlayerCharacterRequest(executorUser.token(), playerCharacter);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.OK.value());

				PlayerCharacterResponseDto playerCharacterResponseDto = response.as(PlayerCharacterResponseDto.class);
				assertThat(playerCharacterResponseDto.getId())
						.isEqualTo(playerCharacter.getId());
				assertThat(playerCharacterResponseDto.getName())
						.isEqualTo(playerCharacter.getName());
				assertThat(playerCharacterResponseDto.getLevel())
						.isEqualTo(playerCharacter.getBaseUnitAttributes().getLevel());
				assertThat(playerCharacterResponseDto.getStrength())
						.isEqualTo(playerCharacter.getBaseUnitAttributes().getStrength());
				assertThat(playerCharacterResponseDto.getDexterity())
						.isEqualTo(playerCharacter.getBaseUnitAttributes().getDexterity());
				assertThat(playerCharacterResponseDto.getIntelligence())
						.isEqualTo(playerCharacter.getBaseUnitAttributes().getIntelligence());
				assertThat(playerCharacterResponseDto.getConstitution())
						.isEqualTo(playerCharacter.getBaseUnitAttributes().getConstitution());
				assertThat(playerCharacterResponseDto.getWillpower())
						.isEqualTo(playerCharacter.getBaseUnitAttributes().getWillpower());
				assertThat(playerCharacterResponseDto.getPerception())
						.isEqualTo(playerCharacter.getBaseUnitAttributes().getPerception());
				assertThat(playerCharacterResponseDto.getLuck())
						.isEqualTo(playerCharacter.getBaseUnitAttributes().getLuck());

				comparePlayerCharacterWithDatabasePlayerCharacter(playerCharacterResponseDto, executorUser.user());
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			@DisplayName("With a user not authenticated")
			@Test
			void getPlayerCharacter_shouldReturn403_whenNotAuthenticated() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				PlayerCharacter playerCharacter = playerCharacterFactory.generateWithHighStrength(executorUser.user());

				Response response = doGetPlayerCharacterRequest(null, playerCharacter);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

			@DisplayName("With a user not authorized")
			@Test
			void getPlayerCharacter_shouldReturn401_whenNotAuthorized() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();

				ExecutorUser secondaryExecutorUser = executorUserFactory.generateBasicSecondary();
				PlayerCharacter secondaryPlayerCharacter = playerCharacterFactory.generateWithHighStrength(secondaryExecutorUser.user());

				Response response = doGetPlayerCharacterRequest(executorUser.token(), secondaryPlayerCharacter);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.UNAUTHORIZED.value());
			}

			@DisplayName("With a search of a non existent player character")
			@Test
			void getPlayerCharacter_shouldReturn401_whenSearchingNonExistentPlayerCharacter() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();

				Long playerCharacterIdNonExistent = 0L;
				PlayerCharacter playerCharacterToBeSearched = new PlayerCharacter();
				playerCharacterToBeSearched.setId(playerCharacterIdNonExistent);

				Response response = doGetPlayerCharacterRequest(executorUser.token(), playerCharacterToBeSearched);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.NOT_FOUND.value());
			}

		}

	}

	@DisplayName("When deleting a player character")
	@Nested
	class Delete {

		protected Response doDeletePlayerCharacterRequest(String executorToken, PlayerCharacter playerCharacterDeleted) {
			String path = CHARACTER_PATH.concat("/").concat(String.valueOf(playerCharacterDeleted.getId()));

			if (executorToken != null) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(executorToken))
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

			@DisplayName("With a user deleting a player character he own")
			@Test
			void deletePlayerCharacter_shouldReturn200_whenUserDeletingOwnPlayerCharacter() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				PlayerCharacter playerCharacter = playerCharacterFactory.generateWithHighStrength(executorUser.user());

				Response response = doDeletePlayerCharacterRequest(executorUser.token(), playerCharacter);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.NO_CONTENT.value());

				Optional<PlayerCharacter> playerCharacterInDatabase = playerCharacterRepository.findById(playerCharacter.getId());
				assertThat(playerCharacterInDatabase)
						.isNotPresent();
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			@DisplayName("With a player character not authenticated")
			@Test
			void deletePlayerCharacter_shouldReturn403_whenNotAuthenticated() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				PlayerCharacter playerCharacter = playerCharacterFactory.generateWithHighStrength(executorUser.user());

				Response response = doDeletePlayerCharacterRequest(null, playerCharacter);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

			@DisplayName("With a player character not authorized")
			@Test
			void deletePlayerCharacter_shouldReturn401_whenNotAuthorized() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();

				ExecutorUser secondaryExecutorUser = executorUserFactory.generateBasicSecondary();
				PlayerCharacter secondaryPlayerCharacter = playerCharacterFactory.generateWithHighStrength(secondaryExecutorUser.user());

				Response response = doDeletePlayerCharacterRequest(executorUser.token(), secondaryPlayerCharacter);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.UNAUTHORIZED.value());
			}

			@DisplayName("With a delete of a non existent player character")
			@Test
			void getPlayerCharacter_shouldReturn404_whenDeletingNonExistentPlayerCharacter() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();

				Long playerCharacterIdNonExistent = 0L;
				PlayerCharacter playerCharacterToBeDeleted = new PlayerCharacter();
				playerCharacterToBeDeleted.setId(playerCharacterIdNonExistent);

				Response response = doDeletePlayerCharacterRequest(executorUser.token(), playerCharacterToBeDeleted);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.NOT_FOUND.value());
			}

		}

	}

}
