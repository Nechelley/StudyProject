package com.study_project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study_project.config.RestControllerTestConfig;
import com.study_project.controller.dto.*;
import com.study_project.enums.AttributeEnum;
import com.study_project.enums.ProfileEnum;
import com.study_project.factory.CharacterCreationDtoFactory;
import com.study_project.factory.CharacterUpdateDtoFactory;
import com.study_project.model.Character;
import com.study_project.model.User;
import com.study_project.record.ExecutorUser;
import com.study_project.repository.CharacterRepository;
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
public class CharacterControllerIT extends AbstractIT implements MysqlSetup {

	private static final short MAX_INITIAL_ATTRIBUTE = 15;
	private static final String BEARER_PREFIX = "Bearer ";
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String CHARACTER_PATH = "/character";
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
	private CharacterRepository characterRepository;

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
		characterRepository.deleteAll();
		userRepository.deleteAll();
	}

	protected void compareCharacterWithDatabaseCharacter(CharacterResponseDto characterResponseDto, User user) {
		Optional<Character> characterInDatabase = characterRepository.findById(characterResponseDto.getId());
		assertThat(characterInDatabase)
				.isPresent();
		assertThat(characterInDatabase.get().getId())
				.isEqualTo(characterResponseDto.getId());
		assertThat(characterInDatabase.get().getName())
				.isEqualTo(characterResponseDto.getName());
		assertThat(characterInDatabase.get().getBaseCharacterAttributes().getLevel())
				.isEqualTo(characterResponseDto.getLevel());
		assertThat(characterInDatabase.get().getBaseCharacterAttributes().getStrength())
				.isEqualTo(characterResponseDto.getStrength());
		assertThat(characterInDatabase.get().getBaseCharacterAttributes().getDexterity())
				.isEqualTo(characterResponseDto.getDexterity());
		assertThat(characterInDatabase.get().getBaseCharacterAttributes().getIntelligence())
				.isEqualTo(characterResponseDto.getIntelligence());
		assertThat(characterInDatabase.get().getBaseCharacterAttributes().getConstitution())
				.isEqualTo(characterResponseDto.getConstitution());
		assertThat(characterInDatabase.get().getBaseCharacterAttributes().getWillpower())
				.isEqualTo(characterResponseDto.getWillpower());
		assertThat(characterInDatabase.get().getBaseCharacterAttributes().getPerception())
				.isEqualTo(characterResponseDto.getPerception());
		assertThat(characterInDatabase.get().getBaseCharacterAttributes().getLuck())
				.isEqualTo(characterResponseDto.getLuck());
		assertThat(characterInDatabase.get().getUser().getId())
				.isEqualTo(user.getId());
	}

	@DisplayName("When creating a initial character")
	@Nested
	class Create {

		protected Response doCreateCharacterRequest(CharacterCreationDto characterCreationDto, String executorToken) throws JsonProcessingException {
			if (executorToken != null) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(executorToken))
						.body(objectMapper.writeValueAsString(characterCreationDto))
						.post(CHARACTER_PATH)
						.then()
						.log().all()
						.extract()
						.response();
			}

			return given()
					.log().all()
					.contentType(JSON)
					.body(objectMapper.writeValueAsString(characterCreationDto))
					.post(CHARACTER_PATH)
					.then()
					.log().all()
					.extract()
					.response();
		}

		@DisplayName("Must success")
		@Nested
		class Success {

			private static Stream<Arguments> provideCharacterSuccessArguments() {
				CharacterCreationDto characterCreationDtoWithoutDistribution = CharacterCreationDtoFactory.aCharacterCreationDto()
						.build();
				CharacterCreationDto characterCreationDtoWithMinimumName = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withName(NAME_WITH_MINIMUM_CHARACTERS)
						.build();
				CharacterCreationDto characterCreationDtoWithMaximumName = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withName(NAME_WITH_MAXIMUM_CHARACTERS)
						.build();
				CharacterCreationDto characterCreationDtoWithSpecialName = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withName(NAME_WITH_ESPECIAL_CHARACTERS)
						.build();
				CharacterCreationDto characterCreationDtoWithPointsDistributed = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withStrength((short) 11)
						.withDexterity((short) 11)
						.withIntelligence((short) 11)
						.withConstitution((short) 11)
						.withWillpower((short) 11)
						.build();
				CharacterCreationDto characterCreationDtoWithMaxStrength = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withStrength(MAX_INITIAL_ATTRIBUTE)
						.build();
				CharacterCreationDto characterCreationDtoWithMaxDexterity = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withDexterity(MAX_INITIAL_ATTRIBUTE)
						.build();
				CharacterCreationDto characterCreationDtoWithMaxIntelligence = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withIntelligence(MAX_INITIAL_ATTRIBUTE)
						.build();
				CharacterCreationDto characterCreationDtoWithMaxConstitution = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withConstitution(MAX_INITIAL_ATTRIBUTE)
						.build();
				CharacterCreationDto characterCreationDtoWithMaxWillpower = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withWillpower(MAX_INITIAL_ATTRIBUTE)
						.build();
				CharacterCreationDto characterCreationDtoWithMaxPerception = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withPerception(MAX_INITIAL_ATTRIBUTE)
						.build();
				CharacterCreationDto characterCreationDtoWithMaxLuck = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withLuck(MAX_INITIAL_ATTRIBUTE)
						.build();

				return Stream.of(
						Arguments.of(characterCreationDtoWithoutDistribution),
						Arguments.of(characterCreationDtoWithMinimumName),
						Arguments.of(characterCreationDtoWithMaximumName),
						Arguments.of(characterCreationDtoWithSpecialName),
						Arguments.of(characterCreationDtoWithPointsDistributed),
						Arguments.of(characterCreationDtoWithMaxStrength),
						Arguments.of(characterCreationDtoWithMaxDexterity),
						Arguments.of(characterCreationDtoWithMaxIntelligence),
						Arguments.of(characterCreationDtoWithMaxConstitution),
						Arguments.of(characterCreationDtoWithMaxWillpower),
						Arguments.of(characterCreationDtoWithMaxPerception),
						Arguments.of(characterCreationDtoWithMaxLuck)
				);
			}

			@DisplayName("With a character with all correct information")
			@ParameterizedTest
			@MethodSource("provideCharacterSuccessArguments")
			void createCharacter_shouldReturn201_whenAllInformationIsCorrect(CharacterCreationDto characterCreationDto) throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();

				Response response = doCreateCharacterRequest(characterCreationDto, executorUser.token());

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.CREATED.value());

				CharacterResponseDto characterResponseDto = response.as(CharacterResponseDto.class);
				assertThat(characterResponseDto.getId())
						.isNotNull();
				assertThat(characterResponseDto.getName())
						.isEqualTo(characterCreationDto.getName());
				assertThat(characterResponseDto.getLevel())
						.isEqualTo(INITIAL_LEVEL);
				assertThat(characterResponseDto.getStrength())
						.isEqualTo(characterCreationDto.getStrength());
				assertThat(characterResponseDto.getDexterity())
						.isEqualTo(characterCreationDto.getDexterity());
				assertThat(characterResponseDto.getIntelligence())
						.isEqualTo(characterCreationDto.getIntelligence());
				assertThat(characterResponseDto.getConstitution())
						.isEqualTo(characterCreationDto.getConstitution());
				assertThat(characterResponseDto.getWillpower())
						.isEqualTo(characterCreationDto.getWillpower());
				assertThat(characterResponseDto.getPerception())
						.isEqualTo(characterCreationDto.getPerception());
				assertThat(characterResponseDto.getLuck())
						.isEqualTo(characterCreationDto.getLuck());

				compareCharacterWithDatabaseCharacter(characterResponseDto, executorUser.user());
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			private static Stream<Arguments> provideCharacterWithFailSizeNameArguments() {
				CharacterCreationDto lessThanMinimumCharacterCreationDto = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withName(NAME_WITH_LESS_THAN_MINIMUM_CHARACTERS)
						.build();
				CharacterCreationDto moreThanMaximumCharacterCreationDto = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withName(NAME_WITH_MORE_THAN_MAXIMUM_CHARACTERS)
						.build();

				return Stream.of(
						Arguments.of(lessThanMinimumCharacterCreationDto),
						Arguments.of(moreThanMaximumCharacterCreationDto)
				);
			}

			private static Stream<Arguments> provideCharacterWithBlankFieldArguments() {
				CharacterCreationDto blankStrengthCharacterCreationDto = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withStrength(null)
						.build();
				CharacterCreationDto blankDexterityCharacterCreationDto = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withDexterity(null)
						.build();
				CharacterCreationDto blankIntelligenceCharacterCreationDto = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withIntelligence(null)
						.build();
				CharacterCreationDto blankConstitutionCharacterCreationDto = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withConstitution(null)
						.build();
				CharacterCreationDto blankWillpowerCharacterCreationDto = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withWillpower(null)
						.build();
				CharacterCreationDto blankPerceptionCharacterCreationDto = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withPerception(null)
						.build();
				CharacterCreationDto blankLuckCharacterCreationDto = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withLuck(null)
						.build();

				return Stream.of(
						Arguments.of(blankStrengthCharacterCreationDto, "strength"),
						Arguments.of(blankDexterityCharacterCreationDto, "dexterity"),
						Arguments.of(blankIntelligenceCharacterCreationDto, "intelligence"),
						Arguments.of(blankConstitutionCharacterCreationDto, "constitution"),
						Arguments.of(blankWillpowerCharacterCreationDto, "willpower"),
						Arguments.of(blankPerceptionCharacterCreationDto, "perception"),
						Arguments.of(blankLuckCharacterCreationDto, "luck")
				);
			}

			@DisplayName("With a character with incorrect size name")
			@ParameterizedTest
			@MethodSource("provideCharacterWithFailSizeNameArguments")
			void createCharacter_shouldReturn404_whenIncorrectSizeName(CharacterCreationDto characterCreationDto) throws JsonProcessingException {
				Response response = doCreateCharacterRequest(characterCreationDto, executorUserFactory.generateBasic().token());

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

			@DisplayName("With a character with less than minimum attribute value")
			@ParameterizedTest
			@EnumSource(AttributeEnum.class)
			void createCharacter_shouldReturn404_whenLessThanMinimumAttributeValue(AttributeEnum attribute) throws JsonProcessingException {
				CharacterCreationDto characterCreationDto = switch (attribute) {
					case AttributeEnum.STRENGTH -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withStrength(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.DEXTERITY -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withDexterity(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.INTELLIGENCE -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withIntelligence(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.CONSTITUTION -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withConstitution(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.WILLPOWER -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withWillpower(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.PERCEPTION -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withPerception(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.LUCK -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withLuck(LESS_THAN_MINIMUM_ATTRIBUTE)
							.build();
				};

				Response response = doCreateCharacterRequest(characterCreationDto, executorUserFactory.generateBasic().token());

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

			@DisplayName("With a character with more than maximum attribute value")
			@ParameterizedTest
			@EnumSource(AttributeEnum.class)
			void createCharacter_shouldReturn404_whenMoreThanMaximumAttributeValue(AttributeEnum attribute) throws JsonProcessingException {
				CharacterCreationDto characterCreationDto = switch (attribute) {
					case AttributeEnum.STRENGTH -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withStrength(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.DEXTERITY -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withDexterity(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.INTELLIGENCE -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withIntelligence(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.CONSTITUTION -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withConstitution(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.WILLPOWER -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withWillpower(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.PERCEPTION -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withPerception(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
					case AttributeEnum.LUCK -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withLuck(MORE_THAN_MAXIMUM_ATTRIBUTE)
							.build();
				};

				Response response = doCreateCharacterRequest(characterCreationDto, executorUserFactory.generateBasic().token());

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

			@DisplayName("With a character with more than maximum distributed attribute value")
			@ParameterizedTest
			@EnumSource(AttributeEnum.class)
			void createCharacter_shouldReturn404_whenMoreThanMaximumDistributedAttributeValue(AttributeEnum attribute) throws JsonProcessingException {
				CharacterCreationDto characterCreationDto = switch (attribute) {
					case AttributeEnum.STRENGTH -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withStrength(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
					case AttributeEnum.DEXTERITY -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withDexterity(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
					case AttributeEnum.INTELLIGENCE -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withIntelligence(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
					case AttributeEnum.CONSTITUTION -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withConstitution(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
					case AttributeEnum.WILLPOWER -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withWillpower(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
					case AttributeEnum.PERCEPTION -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withPerception(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
					case AttributeEnum.LUCK -> CharacterCreationDtoFactory.aCharacterCreationDto()
							.withLuck(MORE_THAN_MAXIMUM_DISTRIBUTED_ATTRIBUTE)
							.build();
				};

				Response response = doCreateCharacterRequest(characterCreationDto, executorUserFactory.generateBasic().token());

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST.value());

				GenericFieldErrorDto message = response.as(GenericFieldErrorDto.class);
				assertThat(message.error())
						.isEqualTo("Character attributes not valid");

				assertThat(message)
						.isNotNull();
			}

			@DisplayName("With a character with blank attribute")
			@ParameterizedTest
			@MethodSource("provideCharacterWithBlankFieldArguments")
			void createCharacter_shouldReturn404_whenBlankAttributeValue(CharacterCreationDto characterCreationDto, String field) throws JsonProcessingException {
				Response response = doCreateCharacterRequest(characterCreationDto, executorUserFactory.generateBasic().token());

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

			@DisplayName("With a character with blank name")
			@Test
			void createCharacter_shouldReturn404_whenBlankNameValue() throws JsonProcessingException {
				CharacterCreationDto blankNameCharacterCreationDto = CharacterCreationDtoFactory.aCharacterCreationDto()
						.withName("")
						.build();

				Response response = doCreateCharacterRequest(blankNameCharacterCreationDto, executorUserFactory.generateBasic().token());

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
			void createCharacter_shouldReturn403_whenNotAuthenticated() throws JsonProcessingException {
				CharacterCreationDto characterCreationDto = CharacterCreationDtoFactory.aCharacterCreationDto()
						.build();

				Response response = doCreateCharacterRequest(characterCreationDto, null);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

		}

	}

	@DisplayName("When updating a character")
	@Nested
	class Update {

		protected Response doUpdateCharacterRequest(CharacterUpdateDto characterUpdateDto, String executorToken, Character characterToBeUpdated) throws JsonProcessingException {
			String path = CHARACTER_PATH.concat("/").concat(String.valueOf(characterToBeUpdated.getId()));

			if (executorToken != null) {
				return given()
						.log().all()
						.contentType(JSON)
						.header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(executorToken))
						.body(objectMapper.writeValueAsString(characterUpdateDto))
						.put(path)
						.then()
						.log().all()
						.extract()
						.response();
			}
			return given()
					.log().all()
					.contentType(JSON)
					.body(objectMapper.writeValueAsString(characterUpdateDto))
					.put(path)
					.then()
					.log().all()
					.extract()
					.response();
		}

		@DisplayName("Must success")
		@Nested
		class Success {

			private static Stream<Arguments> provideCharacterSuccessArguments() {
				CharacterUpdateDto simpleCharacterUpdateDto = CharacterUpdateDtoFactory.aCharacterUpdateDto()
						.build();
				CharacterUpdateDto minimumCharacterUpdateDto = CharacterUpdateDtoFactory.aCharacterUpdateDto()
						.withName(NAME_WITH_MINIMUM_CHARACTERS)
						.build();
				CharacterUpdateDto maximumCharacterUpdateDto = CharacterUpdateDtoFactory.aCharacterUpdateDto()
						.withName(NAME_WITH_MAXIMUM_CHARACTERS)
						.build();
				CharacterUpdateDto specialNameCharacterUpdateDto = CharacterUpdateDtoFactory.aCharacterUpdateDto()
						.withName(NAME_WITH_ESPECIAL_CHARACTERS)
						.build();


				return Stream.of(
						Arguments.of(simpleCharacterUpdateDto),
						Arguments.of(minimumCharacterUpdateDto),
						Arguments.of(maximumCharacterUpdateDto),
						Arguments.of(specialNameCharacterUpdateDto)
				);
			}

			@DisplayName("With a character with all correct information")
			@ParameterizedTest
			@MethodSource("provideCharacterSuccessArguments")
			void updateCharacter_shouldReturn200_whenAllInformationIsCorrect(CharacterUpdateDto characterUpdateDto) throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				Character character = characterFactory.generateWithHighStrength(executorUser.user());

				Response response = doUpdateCharacterRequest(characterUpdateDto, executorUser.token(), character);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.OK.value());

				CharacterResponseDto characterResponseDto = response.as(CharacterResponseDto.class);
				assertThat(characterResponseDto.getId())
						.isEqualTo(character.getId());
				assertThat(characterResponseDto.getName())
						.isEqualTo(characterUpdateDto.getName());

				compareCharacterWithDatabaseCharacter(characterResponseDto, executorUser.user());
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			private static Stream<Arguments> provideCharacterWithFailSizeNameArguments() {
				CharacterUpdateDto lessThanMinimumCharacterUpdateDto = CharacterUpdateDtoFactory.aCharacterUpdateDto()
						.withName(NAME_WITH_LESS_THAN_MINIMUM_CHARACTERS)
						.build();
				CharacterUpdateDto moreThanMaximumCharacterCreationDto = CharacterUpdateDtoFactory.aCharacterUpdateDto()
						.withName(NAME_WITH_MORE_THAN_MAXIMUM_CHARACTERS)
						.build();

				return Stream.of(
						Arguments.of(lessThanMinimumCharacterUpdateDto),
						Arguments.of(moreThanMaximumCharacterCreationDto)
				);
			}

			@DisplayName("With a character with incorrect size name information")
			@ParameterizedTest
			@MethodSource("provideCharacterWithFailSizeNameArguments")
			void updateCharacter_shouldReturn404_whenIncorrectSizeName(CharacterUpdateDto characterUpdateDto) throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				Character character = characterFactory.generateWithHighStrength(executorUser.user());

				Response response = doUpdateCharacterRequest(characterUpdateDto, executorUser.token(), character);

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

			@DisplayName("With a character without name")
			@Test
			void updateCharacter_shouldReturn404_whenWithoutName() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				Character character = characterFactory.generateWithHighStrength(executorUser.user());

				CharacterUpdateDto characterUpdateDto = CharacterUpdateDtoFactory.aCharacterUpdateDto()
						.withName("")
						.build();

				Response response = doUpdateCharacterRequest(characterUpdateDto, executorUser.token(), character);

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

			@DisplayName("With a character not authenticated")
			@Test
			void updateCharacter_shouldReturn403_whenNotAuthenticated() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				Character character = characterFactory.generateWithHighStrength(executorUser.user());

				CharacterUpdateDto characterUpdateDto = CharacterUpdateDtoFactory.aCharacterUpdateDto()
						.build();

				Response response = doUpdateCharacterRequest(characterUpdateDto, null, character);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

			@DisplayName("With a character not authorized")
			@Test
			void updateCharacter_shouldReturn401_whenNotAuthorized() throws JsonProcessingException {
				ExecutorUser ownerUser = executorUserFactory.generateBasic();
				ExecutorUser executorUser = executorUserFactory.generateBasicSecondary();
				Character character = characterFactory.generateWithHighStrength(ownerUser.user());

				CharacterUpdateDto characterUpdateDto = CharacterUpdateDtoFactory.aCharacterUpdateDto()
						.build();

				Response response = doUpdateCharacterRequest(characterUpdateDto, executorUser.token(), character);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.UNAUTHORIZED.value());
			}

			@DisplayName("With a update of a non existent character")
			@Test
			void updateCharacter_shouldReturn404_whenUpdatingNonExistentCharacter() throws JsonProcessingException {
				CharacterUpdateDto characterUpdateDto = CharacterUpdateDtoFactory.aCharacterUpdateDto()
						.build();

				Long characterIdNonExistent = 0L;
				Character characterToBeUpdated = new Character();
				characterToBeUpdated.setId(characterIdNonExistent);

				Response response = doUpdateCharacterRequest(characterUpdateDto, executorUserFactory.generateBasic().token(), characterToBeUpdated);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.NOT_FOUND.value());
			}

		}

	}

	@DisplayName("When getting all characters from a user")
	@Nested
	class getAll {

		protected Response doGetAllCharacterFromUserRequest(String executorToken) {
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

			@DisplayName("With a user with none character")
			@Test
			void getAllCharacter_shouldReturn200_whenUsingUserWithNoneCharacter() throws JsonProcessingException {
				//creating characters in db for the search
				ExecutorUser executorUser = executorUserFactory.generateBasic();

				Response response = doGetAllCharacterFromUserRequest(executorUser.token());

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

				List<CharacterResponseDto> characters = response.jsonPath().getList("content", CharacterResponseDto.class);

				assertThat(characters.size())
						.isEqualTo(0);
			}

			@DisplayName("With a user with one character")
			@Test
			void getAllCharacter_shouldReturn200_whenUsingUserWithOneCharacter() throws JsonProcessingException {
				//creating characters in db for the search
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				Character character = characterFactory.generateWithHighStrength(executorUser.user());

				Response response = doGetAllCharacterFromUserRequest(executorUser.token());

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

				List<CharacterResponseDto> characters = response.jsonPath().getList("content", CharacterResponseDto.class);

				CharacterResponseDto characterResponse = characters.stream()
						.filter(u -> u.getId().equals(character.getId()))
						.findFirst()
						.orElse(null);
				assertThat(characterResponse)
						.isNotNull();
				compareCharacterWithDatabaseCharacter(characterResponse, executorUser.user());
			}

			@DisplayName("With a user with more than one character")
			@Test
			void getAllCharacter_shouldReturn200_whenUsingUserWithMoreThanOneCharacter() throws JsonProcessingException {
				//creating characters in db for the search
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				Character character = characterFactory.generateWithHighStrength(executorUser.user());
				Character secondaryCharacter = characterFactory.generateWithHighDexterity(executorUser.user());

				Response response = doGetAllCharacterFromUserRequest(executorUser.token());

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

				List<CharacterResponseDto> characters = response.jsonPath().getList("content", CharacterResponseDto.class);

				CharacterResponseDto characterResponse = characters.stream()
						.filter(u -> u.getId().equals(character.getId()))
						.findFirst()
						.orElse(null);
				assertThat(characterResponse)
						.isNotNull();
				compareCharacterWithDatabaseCharacter(characterResponse, executorUser.user());

				characterResponse = characters.stream()
						.filter(u -> u.getId().equals(secondaryCharacter.getId()))
						.findFirst()
						.orElse(null);
				assertThat(characterResponse)
						.isNotNull();
				compareCharacterWithDatabaseCharacter(characterResponse, executorUser.user());
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			@DisplayName("With a character not authenticated")
			@Test
			void getAllCharacter_shouldReturn403_whenNotAuthenticated() {
				Response response = doGetAllCharacterFromUserRequest(null);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

		}

	}

	@DisplayName("When getting a character")
	@Nested
	class Get {

		protected Response doGetCharacterRequest(String executorToken, Character characterSearched) {
			String path = CHARACTER_PATH.concat("/").concat(String.valueOf(characterSearched.getId()));

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

			@DisplayName("With a user getting a character he own")
			@Test
			void getCharacter_shouldReturn200_whenUserGettingOwnCharacter() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				Character character = characterFactory.generateWithHighStrength(executorUser.user());

				Response response = doGetCharacterRequest(executorUser.token(), character);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.OK.value());

				CharacterResponseDto characterResponseDto = response.as(CharacterResponseDto.class);
				assertThat(characterResponseDto.getId())
						.isEqualTo(character.getId());
				assertThat(characterResponseDto.getName())
						.isEqualTo(character.getName());
				assertThat(characterResponseDto.getLevel())
						.isEqualTo(character.getBaseCharacterAttributes().getLevel());
				assertThat(characterResponseDto.getStrength())
						.isEqualTo(character.getBaseCharacterAttributes().getStrength());
				assertThat(characterResponseDto.getDexterity())
						.isEqualTo(character.getBaseCharacterAttributes().getDexterity());
				assertThat(characterResponseDto.getIntelligence())
						.isEqualTo(character.getBaseCharacterAttributes().getIntelligence());
				assertThat(characterResponseDto.getConstitution())
						.isEqualTo(character.getBaseCharacterAttributes().getConstitution());
				assertThat(characterResponseDto.getWillpower())
						.isEqualTo(character.getBaseCharacterAttributes().getWillpower());
				assertThat(characterResponseDto.getPerception())
						.isEqualTo(character.getBaseCharacterAttributes().getPerception());
				assertThat(characterResponseDto.getLuck())
						.isEqualTo(character.getBaseCharacterAttributes().getLuck());

				compareCharacterWithDatabaseCharacter(characterResponseDto, executorUser.user());
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			@DisplayName("With a user not authenticated")
			@Test
			void getCharacter_shouldReturn403_whenNotAuthenticated() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				Character character = characterFactory.generateWithHighStrength(executorUser.user());

				Response response = doGetCharacterRequest(null, character);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

			@DisplayName("With a user not authorized")
			@Test
			void getCharacter_shouldReturn401_whenNotAuthorized() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();

				ExecutorUser secondaryExecutorUser = executorUserFactory.generateBasicSecondary();
				Character secondaryCharacter = characterFactory.generateWithHighStrength(secondaryExecutorUser.user());


				Response response = doGetCharacterRequest(executorUser.token(), secondaryCharacter);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.UNAUTHORIZED.value());
			}

			@DisplayName("With a search of a non existent character")
			@Test
			void getCharacter_shouldReturn401_whenAdminSearchingNonExistentCharacter() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();

				Long characterIdNonExistent = 0L;
				Character characterToBeSearched = new Character();
				characterToBeSearched.setId(characterIdNonExistent);

				Response response = doGetCharacterRequest(executorUser.token(), characterToBeSearched);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.NOT_FOUND.value());
			}

		}

	}

	@DisplayName("When deleting a character")
	@Nested
	class Delete {

		protected Response doDeleteCharacterRequest(String executorToken, Character characterDeleted) {
			String path = CHARACTER_PATH.concat("/").concat(String.valueOf(characterDeleted.getId()));

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

			@DisplayName("With a user deleting a character he own")
			@Test
			void deleteCharacter_shouldReturn200_whenUserDeletingOwnCharacter() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				Character character = characterFactory.generateWithHighStrength(executorUser.user());

				Response response = doDeleteCharacterRequest(executorUser.token(), character);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.NO_CONTENT.value());

				Optional<Character> characterInDatabase = characterRepository.findById(character.getId());
				assertThat(characterInDatabase)
						.isNotPresent();
			}

		}

		@DisplayName("Must fail")
		@Nested
		class Fail {

			@DisplayName("With a character not authenticated")
			@Test
			void deleteCharacter_shouldReturn403_whenNotAuthenticated() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();
				Character character = characterFactory.generateWithHighStrength(executorUser.user());

				Response response = doDeleteCharacterRequest(null, character);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.FORBIDDEN.value());
			}

			@DisplayName("With a character not authorized")
			@Test
			void deleteCharacter_shouldReturn401_whenNotAuthorized() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();

				ExecutorUser secondaryExecutorUser = executorUserFactory.generateBasicSecondary();
				Character secondaryCharacter = characterFactory.generateWithHighStrength(secondaryExecutorUser.user());

				Response response = doDeleteCharacterRequest(executorUser.token(), secondaryCharacter);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.UNAUTHORIZED.value());
			}

			@DisplayName("With a delete of a non existent character")
			@Test
			void getCharacter_shouldReturn404_whenDeletingNonExistentCharacter() throws JsonProcessingException {
				ExecutorUser executorUser = executorUserFactory.generateBasic();

				Long characterIdNonExistent = 0L;
				Character characterToBeDeleted = new Character();
				characterToBeDeleted.setId(characterIdNonExistent);

				Response response = doDeleteCharacterRequest(executorUser.token(), characterToBeDeleted);

				assertThat(response.statusCode())
						.isEqualTo(HttpStatus.NOT_FOUND.value());
			}

		}

	}

}
