package com.study_project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study_project.config.RestControllerTestConfig;
import com.study_project.controller.dto.EnemyCreationDto;
import com.study_project.controller.dto.EnemyResponseDto;
import com.study_project.controller.dto.EnemyUpdateDto;
import com.study_project.enums.AttributeEnum;
import com.study_project.factory.EnemyCreationDtoFactory;
import com.study_project.factory.EnemyUpdateDtoFactory;
import com.study_project.model.Enemy;
import com.study_project.record.ExecutorUser;
import com.study_project.repository.EnemyRepository;
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
public class EnemyControllerIT extends AbstractIT implements MysqlSetup {

	private static final short MAX_INITIAL_ATTRIBUTE = 15;
	private static final short MAX_ATTRIBUTE_AT_LEVEL_TWO = 20;
	private static final String BEARER_PREFIX = "Bearer ";
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String ENEMY_PATH = "/enemy";
	private static final short INITIAL_LEVEL = 1;
	private static final short LEVEL_TWO = 2;

	private static final String NAME_WITH_MINIMUM_CHARACTERS = "teste";
	private static final String NAME_WITH_MAXIMUM_CHARACTERS = "100charactersNameaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	private static final String NAME_WITH_LESS_THAN_MINIMUM_CHARACTERS = "john";
	private static final String NAME_WITH_MORE_THAN_MAXIMUM_CHARACTERS = "johnnyaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	private static final String NAME_WITH_ESPECIAL_CHARACTERS = "Sáçoul LeTest";
	private static final short LESS_THAN_MINIMUM_ATTRIBUTE = 9;
	private static final short MORE_THAN_MAXIMUM_ATTRIBUTE = 501;
	private static final short MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE = 16;
	private static final short LESS_THAN_MINIMUM_LEVEL = -1;
	private static final short MORE_THAN_MAXIMUM_LEVEL = 100;

	@LocalServerPort
	private Integer port;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EnemyRepository enemyRepository;

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
		enemyRepository.deleteAll();
		userRepository.deleteAll();
	}

	protected void compareEnemyWithDatabaseEnemy(EnemyResponseDto enemyResponseDto) {
		Optional<Enemy> enemyInDatabase = enemyRepository.findById(enemyResponseDto.getId());
		assertThat(enemyInDatabase)
				.isPresent();
		assertThat(enemyInDatabase.get().getId())
				.isEqualTo(enemyResponseDto.getId());
		assertThat(enemyInDatabase.get().getName())
				.isEqualTo(enemyResponseDto.getName());
		assertThat(enemyInDatabase.get().getBaseUnitAttributes().getLevel())
				.isEqualTo(enemyResponseDto.getLevel());
		assertThat(enemyInDatabase.get().getBaseUnitAttributes().getStrength())
				.isEqualTo(enemyResponseDto.getStrength());
		assertThat(enemyInDatabase.get().getBaseUnitAttributes().getDexterity())
				.isEqualTo(enemyResponseDto.getDexterity());
		assertThat(enemyInDatabase.get().getBaseUnitAttributes().getIntelligence())
				.isEqualTo(enemyResponseDto.getIntelligence());
		assertThat(enemyInDatabase.get().getBaseUnitAttributes().getConstitution())
				.isEqualTo(enemyResponseDto.getConstitution());
		assertThat(enemyInDatabase.get().getBaseUnitAttributes().getWillpower())
				.isEqualTo(enemyResponseDto.getWillpower());
		assertThat(enemyInDatabase.get().getBaseUnitAttributes().getPerception())
				.isEqualTo(enemyResponseDto.getPerception());
		assertThat(enemyInDatabase.get().getBaseUnitAttributes().getLuck())
				.isEqualTo(enemyResponseDto.getLuck());
	}

	@DisplayName("When creating an enemy")
	@Nested
	class Create {

		protected Response doCreateEnemyRequest(EnemyCreationDto enemyCreationDto, String executorToken) throws JsonProcessingException {
			if (executorToken != null) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(executorToken))
						.body(objectMapper.writeValueAsString(enemyCreationDto))
						.post(ENEMY_PATH)
						.then()
						.log().all()
						.extract()
						.response();
			}

			return given()
					.log().all()
					.contentType(JSON)
					.body(objectMapper.writeValueAsString(enemyCreationDto))
					.post(ENEMY_PATH)
					.then()
					.log().all()
					.extract()
					.response();
		}

		@DisplayName("Must success")
		@Nested
		class Success {

			private static Stream<Arguments> provideEnemySuccessArguments() {
				EnemyCreationDto enemyCreationDtoWithoutDistribution = EnemyCreationDtoFactory.aEnemyCreationDto()
						.build();
				EnemyCreationDto enemyCreationDtoWithMinimumName = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withName(NAME_WITH_MINIMUM_CHARACTERS)
						.build();
				EnemyCreationDto enemyCreationDtoWithMaximumName = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withName(NAME_WITH_MAXIMUM_CHARACTERS)
						.build();
				EnemyCreationDto enemyCreationDtoWithSpecialName = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withName(NAME_WITH_ESPECIAL_CHARACTERS)
						.build();
				EnemyCreationDto enemyCreationDtoWithPointsDistributed = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withStrength((short) 11)
						.withDexterity((short) 11)
						.withIntelligence((short) 11)
						.withConstitution((short) 11)
						.withWillpower((short) 11)
						.build();
				EnemyCreationDto enemyCreationDtoWithMaxStrength = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withStrength(MAX_INITIAL_ATTRIBUTE)
						.build();
				EnemyCreationDto enemyCreationDtoWithMaxDexterity = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withDexterity(MAX_INITIAL_ATTRIBUTE)
						.build();
				EnemyCreationDto enemyCreationDtoWithMaxIntelligence = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withIntelligence(MAX_INITIAL_ATTRIBUTE)
						.build();
				EnemyCreationDto enemyCreationDtoWithMaxConstitution = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withConstitution(MAX_INITIAL_ATTRIBUTE)
						.build();
				EnemyCreationDto enemyCreationDtoWithMaxWillpower = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withWillpower(MAX_INITIAL_ATTRIBUTE)
						.build();
				EnemyCreationDto enemyCreationDtoWithMaxPerception = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withPerception(MAX_INITIAL_ATTRIBUTE)
						.build();
				EnemyCreationDto enemyCreationDtoWithMaxLuck = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withLuck(MAX_INITIAL_ATTRIBUTE)
						.build();
				EnemyCreationDto enemyCreationDtoWithNotInitialLevel = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withLevel(LEVEL_TWO)
						.withLuck(MAX_ATTRIBUTE_AT_LEVEL_TWO)
						.build();

				return Stream.of(
						Arguments.of(enemyCreationDtoWithoutDistribution),
						Arguments.of(enemyCreationDtoWithMinimumName),
						Arguments.of(enemyCreationDtoWithMaximumName),
						Arguments.of(enemyCreationDtoWithSpecialName),
						Arguments.of(enemyCreationDtoWithPointsDistributed),
						Arguments.of(enemyCreationDtoWithMaxStrength),
						Arguments.of(enemyCreationDtoWithMaxDexterity),
						Arguments.of(enemyCreationDtoWithMaxIntelligence),
						Arguments.of(enemyCreationDtoWithMaxConstitution),
						Arguments.of(enemyCreationDtoWithMaxWillpower),
						Arguments.of(enemyCreationDtoWithMaxPerception),
						Arguments.of(enemyCreationDtoWithMaxLuck),
						Arguments.of(enemyCreationDtoWithNotInitialLevel)
				);
			}

			@DisplayName("With an enemy with all correct information")
			@ParameterizedTest
			@MethodSource("provideEnemySuccessArguments")
			void createEnemy_shouldReturn201_whenAllInformationIsCorrect(EnemyCreationDto enemyCreationDto) throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateAdmin();

				Response response = doCreateEnemyRequest(enemyCreationDto, executorUser.token());

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.CREATED.value());

				EnemyResponseDto enemyResponseDto = response.as(EnemyResponseDto.class);
				assertThat(enemyResponseDto.getId())
						.isNotNull();
				assertThat(enemyResponseDto.getName())
						.isEqualTo(enemyCreationDto.getName());
				assertThat(enemyResponseDto.getLevel())
						.isEqualTo(enemyCreationDto.getLevel());
				assertThat(enemyResponseDto.getStrength())
						.isEqualTo(enemyCreationDto.getStrength());
				assertThat(enemyResponseDto.getDexterity())
						.isEqualTo(enemyCreationDto.getDexterity());
				assertThat(enemyResponseDto.getIntelligence())
						.isEqualTo(enemyCreationDto.getIntelligence());
				assertThat(enemyResponseDto.getConstitution())
						.isEqualTo(enemyCreationDto.getConstitution());
				assertThat(enemyResponseDto.getWillpower())
						.isEqualTo(enemyCreationDto.getWillpower());
				assertThat(enemyResponseDto.getPerception())
						.isEqualTo(enemyCreationDto.getPerception());
				assertThat(enemyResponseDto.getLuck())
						.isEqualTo(enemyCreationDto.getLuck());

				compareEnemyWithDatabaseEnemy(enemyResponseDto);
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			private static Stream<Arguments> provideEnemyWithFailSizeNameArguments() {
				EnemyCreationDto lessThanMinimumEnemyCreationDto = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withName(NAME_WITH_LESS_THAN_MINIMUM_CHARACTERS)
						.build();
				EnemyCreationDto moreThanMaximumEnemyCreationDto = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withName(NAME_WITH_MORE_THAN_MAXIMUM_CHARACTERS)
						.build();

				return Stream.of(
						Arguments.of(lessThanMinimumEnemyCreationDto),
						Arguments.of(moreThanMaximumEnemyCreationDto)
				);
			}

			private static Stream<Arguments> provideEnemyWithBlankFieldArguments() {
				EnemyCreationDto blankStrengthEnemyCreationDto = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withStrength(null)
						.build();
				EnemyCreationDto blankDexterityEnemyCreationDto = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withDexterity(null)
						.build();
				EnemyCreationDto blankIntelligenceEnemyCreationDto = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withIntelligence(null)
						.build();
				EnemyCreationDto blankConstitutionEnemyCreationDto = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withConstitution(null)
						.build();
				EnemyCreationDto blankWillpowerEnemyCreationDto = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withWillpower(null)
						.build();
				EnemyCreationDto blankPerceptionEnemyCreationDto = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withPerception(null)
						.build();
				EnemyCreationDto blankLuckEnemyCreationDto = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withLuck(null)
						.build();
				EnemyCreationDto blankLevelEnemyCreationDto = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withLevel(null)
						.build();

				return Stream.of(
						Arguments.of(blankStrengthEnemyCreationDto, "strength"),
						Arguments.of(blankDexterityEnemyCreationDto, "dexterity"),
						Arguments.of(blankIntelligenceEnemyCreationDto, "intelligence"),
						Arguments.of(blankConstitutionEnemyCreationDto, "constitution"),
						Arguments.of(blankWillpowerEnemyCreationDto, "willpower"),
						Arguments.of(blankPerceptionEnemyCreationDto, "perception"),
						Arguments.of(blankLuckEnemyCreationDto, "luck"),
						Arguments.of(blankLevelEnemyCreationDto, "level")
				);
			}

			@DisplayName("With an enemy with incorrect size name")
			@ParameterizedTest
			@MethodSource("provideEnemyWithFailSizeNameArguments")
			void createEnemy_shouldReturn404_whenIncorrectSizeName(EnemyCreationDto enemyCreationDto) throws JsonProcessingException {
				Response response = doCreateEnemyRequest(enemyCreationDto, executorUserFactory.generateAdmin().token());

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

			@DisplayName("With an enemy with less than minimum attribute value")
			@ParameterizedTest
			@EnumSource(AttributeEnum.class)
			void createEnemy_shouldReturn404_whenLessThanMinimumAttributeValue(AttributeEnum attribute) throws JsonProcessingException {
				EnemyCreationDto enemyCreationDto = switch (attribute) {
					case AttributeEnum.STRENGTH -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withStrength(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.DEXTERITY -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withDexterity(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.INTELLIGENCE -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withIntelligence(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.CONSTITUTION -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withConstitution(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.WILLPOWER -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withWillpower(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.PERCEPTION -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withPerception(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.LUCK -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withLuck(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
				};

				Response response = doCreateEnemyRequest(enemyCreationDto, executorUserFactory.generateAdmin().token());

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

			@DisplayName("With an enemy with less than minimum level value")
			@Test
			void createEnemy_shouldReturn404_whenLessThanMinimumLevel() throws JsonProcessingException {
				EnemyCreationDto enemyCreationDto = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withLevel(LESS_THAN_MINIMUM_LEVEL)
						.build();

				Response response = doCreateEnemyRequest(enemyCreationDto, executorUserFactory.generateAdmin().token());

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto[] messages = response.as(GenericFieldErrorDto[].class);
				GenericFieldErrorDto message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals("level") && m.error().equals("must be greater than or equal to 1"))
								.findFirst()
								.orElse(null);

				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With an enemy with more than maximum attribute value")
			@ParameterizedTest
			@EnumSource(AttributeEnum.class)
			void createEnemy_shouldReturn404_whenMoreThanMaximumAttributeValue(AttributeEnum attribute) throws JsonProcessingException {
				EnemyCreationDto enemyCreationDto = switch (attribute) {
					case AttributeEnum.STRENGTH -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withStrength(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.DEXTERITY -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withDexterity(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.INTELLIGENCE -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withIntelligence(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.CONSTITUTION -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withConstitution(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.WILLPOWER -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withWillpower(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.PERCEPTION -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withPerception(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.LUCK -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withLuck(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
				};

				Response response = doCreateEnemyRequest(enemyCreationDto, executorUserFactory.generateAdmin().token());

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

			@DisplayName("With an enemy with more than maximum level")
			@Test
			void createEnemy_shouldReturn404_whenMoreThanMaximumLevel() throws JsonProcessingException {
				EnemyCreationDto enemyCreationDto = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withLevel(MORE_THAN_MAXIMUM_LEVEL)
						.build();

				Response response = doCreateEnemyRequest(enemyCreationDto, executorUserFactory.generateAdmin().token());

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto[] messages = response.as(GenericFieldErrorDto[].class);
				GenericFieldErrorDto message =
						Arrays.stream(messages)
								.filter(m -> m.field().equals("level") && m.error().equals("must be less than or equal to 99"))
								.findFirst()
								.orElse(null);

				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With an enemy with more than maximum distributed attribute value")
			@ParameterizedTest
			@EnumSource(AttributeEnum.class)
			void createEnemy_shouldReturn404_whenMoreThanMaximumDistributedAttributeValue(AttributeEnum attribute) throws JsonProcessingException {
				EnemyCreationDto enemyCreationDto = switch (attribute) {
					case AttributeEnum.STRENGTH -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withStrength(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
					case AttributeEnum.DEXTERITY -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withDexterity(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
					case AttributeEnum.INTELLIGENCE -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withIntelligence(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
					case AttributeEnum.CONSTITUTION -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withConstitution(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
					case AttributeEnum.WILLPOWER -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withWillpower(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
					case AttributeEnum.PERCEPTION -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withPerception(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
					case AttributeEnum.LUCK -> EnemyCreationDtoFactory.aEnemyCreationDto()
							.withLuck(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
				};

				Response response = doCreateEnemyRequest(enemyCreationDto, executorUserFactory.generateAdmin().token());

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto message = response.as(GenericFieldErrorDto.class);
				assertThat(message.error())
						.isEqualTo("Enemy attributes not valid");

				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With an enemy with more than maximum distributed attribute value and not initial level")
			@Test
			void createEnemy_shouldReturn404_whenMoreThanMaximumDistributedAttributeValueAndNotInitialLevel() throws JsonProcessingException {
				EnemyCreationDto enemyCreationDto =  EnemyCreationDtoFactory.aEnemyCreationDto()
						.withLevel(LEVEL_TWO)
						.withLuck(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
						.withStrength(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
						.build();

				Response response = doCreateEnemyRequest(enemyCreationDto, executorUserFactory.generateAdmin().token());

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto message = response.as(GenericFieldErrorDto.class);
				assertThat(message.error())
						.isEqualTo("Enemy attributes not valid");

				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With an enemy with blank attribute")
			@ParameterizedTest
			@MethodSource("provideEnemyWithBlankFieldArguments")
			void createEnemy_shouldReturn404_whenBlankAttributeValue(EnemyCreationDto enemyCreationDto, String field) throws JsonProcessingException {
				Response response = doCreateEnemyRequest(enemyCreationDto, executorUserFactory.generateAdmin().token());

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

			@DisplayName("With an enemy with blank name")
			@Test
			void createEnemy_shouldReturn404_whenBlankNameValue() throws JsonProcessingException {
				EnemyCreationDto blankNameEnemyCreationDto = EnemyCreationDtoFactory.aEnemyCreationDto()
						.withName("")
						.build();

				Response response = doCreateEnemyRequest(blankNameEnemyCreationDto, executorUserFactory.generateAdmin().token());

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
			void createEnemy_shouldReturn403_whenNotAuthenticated() throws JsonProcessingException {
				EnemyCreationDto enemyCreationDto = EnemyCreationDtoFactory.aEnemyCreationDto()
						.build();

				Response response = doCreateEnemyRequest(enemyCreationDto, null);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

			@DisplayName("With a user not authorized")
			@Test
			void createEnemy_shouldReturn403_whenNotAuthorized() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				EnemyCreationDto enemyCreationDto = EnemyCreationDtoFactory.aEnemyCreationDto()
						.build();

				Response response = doCreateEnemyRequest(enemyCreationDto, executorUser.token());

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

		}

	}

	@DisplayName("When updating an enemy")
	@Nested
	class Update {

		protected Response doUpdateEnemyRequest(EnemyUpdateDto enemyUpdateDto, String executorToken, Enemy enemyToBeUpdated) throws JsonProcessingException {
			String path = ENEMY_PATH.concat("/").concat(String.valueOf(enemyToBeUpdated.getId()));

			if (executorToken != null) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(executorToken))
						.body(objectMapper.writeValueAsString(enemyUpdateDto))
						.put(path)
						.then()
						.log().all()
						.extract()
						.response();
			}
			return given()
					.log().all()
					.contentType(JSON)
					.body(objectMapper.writeValueAsString(enemyUpdateDto))
					.put(path)
					.then()
					.log().all()
					.extract()
					.response();
		}

		@DisplayName("Must success")
		@Nested
		class Success {

			private static Stream<Arguments> provideEnemySuccessArguments() {
				EnemyUpdateDto simpleEnemyUpdateDto = EnemyUpdateDtoFactory.aEnemyUpdateDto()
						.build();
				EnemyUpdateDto minimumEnemyUpdateDto = EnemyUpdateDtoFactory.aEnemyUpdateDto()
						.withName(NAME_WITH_MINIMUM_CHARACTERS)
						.build();
				EnemyUpdateDto maximumEnemyUpdateDto = EnemyUpdateDtoFactory.aEnemyUpdateDto()
						.withName(NAME_WITH_MAXIMUM_CHARACTERS)
						.build();
				EnemyUpdateDto specialNameEnemyUpdateDto = EnemyUpdateDtoFactory.aEnemyUpdateDto()
						.withName(NAME_WITH_ESPECIAL_CHARACTERS)
						.build();


				return Stream.of(
						Arguments.of(simpleEnemyUpdateDto),
						Arguments.of(minimumEnemyUpdateDto),
						Arguments.of(maximumEnemyUpdateDto),
						Arguments.of(specialNameEnemyUpdateDto)
				);
			}

			@DisplayName("With an enemy with all correct information")
			@ParameterizedTest
			@MethodSource("provideEnemySuccessArguments")
			void updateEnemy_shouldReturn200_whenAllInformationIsCorrect(EnemyUpdateDto enemyUpdateDto) throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateAdmin();
				Enemy enemy = enemyFactory.generateWithHighStrength();

				Response response = doUpdateEnemyRequest(enemyUpdateDto, executorUser.token(), enemy);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.OK.value());

				EnemyResponseDto enemyResponseDto = response.as(EnemyResponseDto.class);
				assertThat(enemyResponseDto.getId())
						.isEqualTo(enemy.getId());
				assertThat(enemyResponseDto.getName())
						.isEqualTo(enemyUpdateDto.getName());

				compareEnemyWithDatabaseEnemy(enemyResponseDto);
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			private static Stream<Arguments> provideEnemyWithFailSizeNameArguments() {
				EnemyUpdateDto lessThanMinimumEnemyUpdateDto = EnemyUpdateDtoFactory.aEnemyUpdateDto()
						.withName(NAME_WITH_LESS_THAN_MINIMUM_CHARACTERS)
						.build();
				EnemyUpdateDto moreThanMaximumCharacterCreationDto = EnemyUpdateDtoFactory.aEnemyUpdateDto()
						.withName(NAME_WITH_MORE_THAN_MAXIMUM_CHARACTERS)
						.build();

				return Stream.of(
						Arguments.of(lessThanMinimumEnemyUpdateDto),
						Arguments.of(moreThanMaximumCharacterCreationDto)
				);
			}

			@DisplayName("With an enemy with incorrect size name information")
			@ParameterizedTest
			@MethodSource("provideEnemyWithFailSizeNameArguments")
			void updateEnemy_shouldReturn404_whenIncorrectSizeName(EnemyUpdateDto enemyUpdateDto) throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateAdmin();
				Enemy enemy = enemyFactory.generateWithHighStrength();

				Response response = doUpdateEnemyRequest(enemyUpdateDto, executorUser.token(), enemy);

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

			@DisplayName("With an enemy without name")
			@Test
			void updateEnemy_shouldReturn404_whenWithoutName() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateAdmin();
				Enemy enemy = enemyFactory.generateWithHighStrength();

				EnemyUpdateDto enemyUpdateDto = EnemyUpdateDtoFactory.aEnemyUpdateDto()
						.withName("")
						.build();

				Response response = doUpdateEnemyRequest(enemyUpdateDto, executorUser.token(), enemy);

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

			@DisplayName("With an enemy not authenticated")
			@Test
			void updateEnemy_shouldReturn403_whenNotAuthenticated() throws JsonProcessingException {
				Enemy enemy = enemyFactory.generateWithHighStrength();

				EnemyUpdateDto enemyUpdateDto = EnemyUpdateDtoFactory.aEnemyUpdateDto()
						.build();

				Response response = doUpdateEnemyRequest(enemyUpdateDto, null, enemy);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

			@DisplayName("With an enemy not authorized")
			@Test
			void updateEnemy_shouldReturn403_whenNotAuthorized() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				Enemy enemy = enemyFactory.generateWithHighStrength();

				EnemyUpdateDto enemyUpdateDto = EnemyUpdateDtoFactory.aEnemyUpdateDto()
						.build();

				Response response = doUpdateEnemyRequest(enemyUpdateDto, executorUser.token(), enemy);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

			@DisplayName("With a update of a non existent enemy")
			@Test
			void updateEnemy_shouldReturn404_whenUpdatingNonExistentCharacter() throws JsonProcessingException {
				EnemyUpdateDto enemyUpdateDto = EnemyUpdateDtoFactory.aEnemyUpdateDto()
						.build();

				Long enemyIdNonExistent = 0L;
				Enemy enemyToBeUpdated = new Enemy();
				enemyToBeUpdated.setId(enemyIdNonExistent);

				Response response = doUpdateEnemyRequest(enemyUpdateDto, executorUserFactory.generateAdmin().token(), enemyToBeUpdated);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.NOT_FOUND.value());
			}

		}

	}

	@DisplayName("When getting all enemies")
	@Nested
	class getAll {

		protected Response doGetAllEnemyRequest(String executorToken) {
			if (executorToken != null) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(executorToken))
						.get(ENEMY_PATH)
						.then()
						.log().all()
						.extract()
						.response();
			}
			return given()
					.log().all()
					.contentType(JSON)
					.get(ENEMY_PATH)
					.then()
					.log().all()
					.extract()
					.response();
		}

		@DisplayName("Must success")
		@Nested
		class Success {

			@DisplayName("With none enemy inserted")
			@Test
			void getAllEnemy_shouldReturn200_whenNoneEnemyInserted() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();

				Response response = doGetAllEnemyRequest(executorUser.token());

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

				List<EnemyResponseDto> enemies = response.jsonPath().getList("content", EnemyResponseDto.class);

				assertThat(enemies.size())
						.isEqualTo(0);
			}

			@DisplayName("With some enemy inserted")
			@Test
			void getAllEnemy_shouldReturn200_whenSomeEnemyInserted() throws JsonProcessingException {
				//creating enemysResponseDto in db for the search
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				Enemy enemy = enemyFactory.generateWithHighStrength();
				Enemy secondaryEnemy = enemyFactory.generateWithHighDexterity();

				Response response = doGetAllEnemyRequest(executorUser.token());

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

				List<EnemyResponseDto> enemysResponseDto = response.jsonPath().getList("content", EnemyResponseDto.class);

				EnemyResponseDto enemyResponse = enemysResponseDto.stream()
						.filter(u -> u.getId().equals(enemy.getId()))
						.findFirst()
						.orElse(null);
				assertThat(enemyResponse)
						.isNotNull();
				compareEnemyWithDatabaseEnemy(enemyResponse);

				enemyResponse = enemysResponseDto.stream()
						.filter(u -> u.getId().equals(secondaryEnemy.getId()))
						.findFirst()
						.orElse(null);
				assertThat(enemyResponse)
						.isNotNull();
				compareEnemyWithDatabaseEnemy(enemyResponse);
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			@DisplayName("With an user not authenticated")
			@Test
			void getAllEnemy_shouldReturn403_whenNotAuthenticated() {
				Response response = doGetAllEnemyRequest(null);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

		}

	}

	@DisplayName("When getting an enemy")
	@Nested
	class Get {

		protected Response doGetEnemyRequest(String executorToken, Enemy enemySearched) {
			String path = ENEMY_PATH.concat("/").concat(String.valueOf(enemySearched.getId()));

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

			@DisplayName("With a user getting an enemy")
			@Test
			void getEnemy_shouldReturn200_whenUserGettingnemy() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				Enemy enemy = enemyFactory.generateWithHighStrength();

				Response response = doGetEnemyRequest(executorUser.token(), enemy);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.OK.value());

				EnemyResponseDto enemyResponseDto = response.as(EnemyResponseDto.class);
				assertThat(enemyResponseDto.getId())
						.isEqualTo(enemy.getId());
				assertThat(enemyResponseDto.getName())
						.isEqualTo(enemy.getName());
				assertThat(enemyResponseDto.getLevel())
						.isEqualTo(enemy.getBaseUnitAttributes().getLevel());
				assertThat(enemyResponseDto.getStrength())
						.isEqualTo(enemy.getBaseUnitAttributes().getStrength());
				assertThat(enemyResponseDto.getDexterity())
						.isEqualTo(enemy.getBaseUnitAttributes().getDexterity());
				assertThat(enemyResponseDto.getIntelligence())
						.isEqualTo(enemy.getBaseUnitAttributes().getIntelligence());
				assertThat(enemyResponseDto.getConstitution())
						.isEqualTo(enemy.getBaseUnitAttributes().getConstitution());
				assertThat(enemyResponseDto.getWillpower())
						.isEqualTo(enemy.getBaseUnitAttributes().getWillpower());
				assertThat(enemyResponseDto.getPerception())
						.isEqualTo(enemy.getBaseUnitAttributes().getPerception());
				assertThat(enemyResponseDto.getLuck())
						.isEqualTo(enemy.getBaseUnitAttributes().getLuck());

				compareEnemyWithDatabaseEnemy(enemyResponseDto);
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			@DisplayName("With a user not authenticated")
			@Test
			void getEnemy_shouldReturn403_whenNotAuthenticated() {
				Enemy enemy = enemyFactory.generateWithHighStrength();

				Response response = doGetEnemyRequest(null, enemy);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

			@DisplayName("With a search of a non existent enemy")
			@Test
			void getEnemy_shouldReturn401_whenSearchingNonExistentEnemy() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();

				Long enemyIdNonExistent = 0L;
				Enemy enemyToBeSearched = new Enemy();
				enemyToBeSearched.setId(enemyIdNonExistent);

				Response response = doGetEnemyRequest(executorUser.token(), enemyToBeSearched);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.NOT_FOUND.value());
			}

		}

	}

	@DisplayName("When deleting an enemy")
	@Nested
	class Delete {

		protected Response doDeleteEnemyRequest(String executorToken, Enemy enemyDeleted) {
			String path = ENEMY_PATH.concat("/").concat(String.valueOf(enemyDeleted.getId()));

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

			@DisplayName("With a user deleting an enemy")
			@Test
			void deleteEnemy_shouldReturn200_whenUserDeletingEnemy() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateAdmin();
				Enemy enemy = enemyFactory.generateWithHighStrength();

				Response response = doDeleteEnemyRequest(executorUser.token(), enemy);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.NO_CONTENT.value());

				Optional<Enemy> enemyInDatabase = enemyRepository.findById(enemy.getId());
				assertThat(enemyInDatabase)
						.isNotPresent();
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			@DisplayName("With an enemy not authenticated")
			@Test
			void deleteEnemy_shouldReturn403_whenNotAuthenticated() {
				Enemy enemy = enemyFactory.generateWithHighStrength();

				Response response = doDeleteEnemyRequest(null, enemy);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

			@DisplayName("With an user not authorized")
			@Test
			void deleteEnemy_shouldReturn403_whenNotAuthorized() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				Enemy secondaryEnemy = enemyFactory.generateWithHighStrength();

				Response response = doDeleteEnemyRequest(executorUser.token(), secondaryEnemy);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

			@DisplayName("With a delete of a non existent enemy")
			@Test
			void getEnemy_shouldReturn404_whenDeletingNonExistentEnemy() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateAdmin();

				Long enemyIdNonExistent = 0L;
				Enemy enemyToBeDeleted = new Enemy();
				enemyToBeDeleted.setId(enemyIdNonExistent);

				Response response = doDeleteEnemyRequest(executorUser.token(), enemyToBeDeleted);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.NOT_FOUND.value());
			}

		}

	}

}
