package layer.service.user;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.gson.JsonSyntaxException;

import layer.model.Entity;
import layer.model.ListParameters;
import layer.model.user.User;
import layer.model.user.UserData;
import layer.model.user.UserDto;
import layer.service.DynamoDBException;
import layer.service.GenericDynamoDBServiceImpl;
import static java.util.Map.ofEntries;
import static java.util.Map.entry;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DynamoDBUserServiceImpl extends GenericDynamoDBServiceImpl<User, UserDto> implements UserService {

	private static final Set<String> SOCIAL_MEDIA_NAMES = Set.of("linkedin", "telegram", "skype", "instagram",
			"facebook");
	private static final String USER_WITH_EMAIL_NOT_FOUND = "User with email %s not found";

	private static final String SORT_KEY_UP_ALIAS = "sortUpAlias";
	private static final String SORT_KEY_LOW_ALIAS = "sortLowAlias";
	private static final String SORT_KEY_ALIAS = "sortAlias";
	private static final String PARTITION_KEY_ALIAS = "partAlias";

	private static final String EMAIL_KEY = "email";
	private static final String TABLE_PARTITION_KEY = EMAIL_KEY;
	private static final String INDEX_PARTITION_KEY = "country";
	private static final String INDEX_PARTITION_KEY_VALUE = "Ukraine";

	private static final String NAME_BODY_PARAMETER = "name";
	private static final String LOCATION_BODY_PARAMETER = "location";
	private static final String BIRTHDAY_BODY_PARAMETER = "birthday";

	private static final String COUNTRY_NAME_INDEX = "country-name-index";
	private static final String COUNTRY_LOCATION_INDEX = "country-location-index";
	private static final String COUNTRY_BIRTHDAY_INDEX = "country-birthday-index";

	@Override
	public void createUser(UserDto userDto) {
		User existingUser = getDynamoDBMapper().load(User.class, userDto.getEmail());
		if (existingUser != null) {
			throw new DynamoDBException(String.format("User with email %s already exists", userDto.getEmail()));
		}
		checkSocialMedia(userDto, "User cannot be created: incorrect social media links %s");
		getDynamoDBMapper().save(userDto.toEntity());
	}

	private void checkSocialMedia(UserDto userDto, String message) {
		Set<String> incorrectSocialMedia = getIncorrectSocialMedia(userDto.getSocialMedia());
		if (!incorrectSocialMedia.isEmpty()) {
			throw new DynamoDBException(String.format(message, incorrectSocialMedia.toString()));
		}
	}

	private Set<String> getIncorrectSocialMedia(Set<String> socialMedia) {
		Set<String> incorrectSocialMedia = new HashSet<>(socialMedia);
		incorrectSocialMedia.removeAll(SOCIAL_MEDIA_NAMES);
		return incorrectSocialMedia;
	}

	@Override
	public Optional<UserDto> findUser(String email) {
		return Optional.ofNullable(getDynamoDBMapper().load(User.class, email)).map(User::toDto);
	}

	@Override
	public void updateUser(String email, UserDto userDto) {
		User existingUser = getDynamoDBMapper().load(User.class, email);
		if (existingUser == null) {
			throw new DynamoDBException(String.format(USER_WITH_EMAIL_NOT_FOUND, email));
		}
		checkSocialMedia(userDto, "User cannot be updated: incorrect social media links %s");
		User user = userDto.toEntity();
		user.setEmail(email);
		getDynamoDBMapper().save(user);
	}

	@Override
	public void deleteUser(String email) {
		User existingUser = getDynamoDBMapper().load(User.class, email);
		if (existingUser == null) {
			throw new DynamoDBException(String.format(USER_WITH_EMAIL_NOT_FOUND, email));
		}
		getDynamoDBMapper().delete(existingUser);
	}

	@Override
	public List<UserDto> getUserList(ListParameters listParameters) {
		return getNotFilteredEntityList(User.class, TABLE_PARTITION_KEY, listParameters);
	}

	@Override
	public List<UserDto> getUserListByQuery(ListParameters listParameters, UserData userData) {
		try {
			if (userData.isNameValid()) {
				return getFilteredUserList(listParameters, COUNTRY_NAME_INDEX, NAME_BODY_PARAMETER, userData.getName());
			}
			if (userData.isLocationValid()) {
				return getFilteredUserList(listParameters, COUNTRY_LOCATION_INDEX, LOCATION_BODY_PARAMETER,
						userData.getLocation());
			}
			if (userData.isAgeValid()) {
				return getFilteredUserList(listParameters, COUNTRY_BIRTHDAY_INDEX, BIRTHDAY_BODY_PARAMETER,
						userData.getMinAgeOrDefault(), userData.getMaxAgeOrDefault());
			}
			return getUserList(listParameters);
		} catch (JsonSyntaxException e) {
			return getUserList(listParameters);
		}
	}

	private List<UserDto> getFilteredUserList(ListParameters listParameters, String indexName, String queryParameter,
			String parameterValue) {

		DynamoDBQueryExpression<User> queryExpression = getNonPaginatedFilteredUserListExpression(indexName,
				queryParameter, parameterValue);

		if (listParameters.hasValidLimit()) {
			queryExpression = queryExpression.withLimit(listParameters.getLimit());
		}
		if (listParameters.hasValidIndexHashAndRangeKeys()) {
			queryExpression = queryExpression.withExclusiveStartKey(getStartKeyWithStringRangeKey(
					listParameters.getHashKey().get(), listParameters.getRangeKey().get(), queryParameter));
		}
		return Entity.toDtoList(getDynamoDBMapper().queryPage(User.class, queryExpression).getResults());
	}

	private Map<String, AttributeValue> getStartKeyWithStringRangeKey(String hashKey, String rangeKey,
			String sortKeyName) {
		return ofEntries(entry(TABLE_PARTITION_KEY, new AttributeValue().withS(hashKey)),
				entry(INDEX_PARTITION_KEY, new AttributeValue().withS(INDEX_PARTITION_KEY_VALUE)),
				entry(sortKeyName, new AttributeValue().withS(rangeKey)));
	}

	private DynamoDBQueryExpression<User> getNonPaginatedFilteredUserListExpression(String indexName,
			String sortKeyName, String sortKeyValue) {

		final String partitionKeyLabel = "#" + INDEX_PARTITION_KEY;
		final String sortKeyLabel = "#" + sortKeyName;

		Map<String, AttributeValue> expressionAttributeValues = getExpressionAttributeValuesMap(sortKeyValue,
				PARTITION_KEY_ALIAS, SORT_KEY_ALIAS);
		Map<String, String> expressionAttributeNames = getExpressionAttributeNamesMap(sortKeyName, partitionKeyLabel,
				sortKeyLabel);

		String conditionExpression = MessageFormat.format("{0} = :{1} and begins_with ({2}, :{3})", partitionKeyLabel,
				PARTITION_KEY_ALIAS, sortKeyLabel, SORT_KEY_ALIAS);
		return new DynamoDBQueryExpression<User>().withIndexName(indexName).withConsistentRead(false)
				.withKeyConditionExpression(conditionExpression).withExpressionAttributeNames(expressionAttributeNames)
				.withExpressionAttributeValues(expressionAttributeValues);
	}

	private static Map<String, AttributeValue> getExpressionAttributeValuesMap(String sortKeyValue,
			String partitionKeyAlias, String sortKeyAlias) {
		return ofEntries(entry(":" + partitionKeyAlias, new AttributeValue().withS(INDEX_PARTITION_KEY_VALUE)),
				entry(":" + sortKeyAlias, new AttributeValue().withS(sortKeyValue)));
	}

	private static Map<String, String> getExpressionAttributeNamesMap(String sortKeyName, String partitionKeyLabel,
			String sortKeyLabel) {
		return ofEntries(entry(partitionKeyLabel, INDEX_PARTITION_KEY), entry(sortKeyLabel, sortKeyName));
	}

	private <T extends Number> List<UserDto> getFilteredUserList(ListParameters listParameters, String indexName,
			String queryParameter, T parameterLowValue, T parameterUpValue) {

		DynamoDBQueryExpression<User> queryExpression = getNonPaginatedFilteredUserListExpression(indexName,
				queryParameter, parameterLowValue, parameterUpValue);
		if (listParameters.hasValidLimit()) {
			queryExpression = queryExpression.withLimit(listParameters.getLimit());
		}
		if (listParameters.hasValidIndexHashAndRangeKeys()) {
			queryExpression = queryExpression.withExclusiveStartKey(getStartKeyWithNumericRangeKey(
					listParameters.getHashKey().get(), listParameters.getRangeKey().get(), queryParameter));
		}
		return Entity.toDtoList(getDynamoDBMapper().queryPage(User.class, queryExpression).getResults());
	}

	private <T extends Number> DynamoDBQueryExpression<User> getNonPaginatedFilteredUserListExpression(String indexName,
			String sortKeyName, T sortKeyLowValue, T sortKeyUpValue) {

		final String partitionKeyLabel = "#" + INDEX_PARTITION_KEY;
		final String sortKeyLabel = "#" + sortKeyName;

		Map<String, AttributeValue> expressionAttributeValues = ofEntries(
				entry(":" + PARTITION_KEY_ALIAS, new AttributeValue().withS(INDEX_PARTITION_KEY_VALUE)),
				entry(":" + SORT_KEY_LOW_ALIAS, new AttributeValue().withN(getSortKeyValue(sortKeyLowValue))),
				entry(":" + SORT_KEY_UP_ALIAS, new AttributeValue().withN(getSortKeyValue(sortKeyUpValue))));

		String conditionExpression = MessageFormat.format("{0} = :{1} and {2} between :{3} and :{4}", partitionKeyLabel,
				PARTITION_KEY_ALIAS, sortKeyLabel, SORT_KEY_LOW_ALIAS, SORT_KEY_UP_ALIAS);

		Map<String, String> expressionAttributeNames = getExpressionAttributeNamesMap(sortKeyName, partitionKeyLabel,
				sortKeyLabel);

		return new DynamoDBQueryExpression<User>().withIndexName(indexName).withConsistentRead(false)
				.withKeyConditionExpression(conditionExpression).withExpressionAttributeNames(expressionAttributeNames)
				.withExpressionAttributeValues(expressionAttributeValues);
	}

	private Map<String, AttributeValue> getStartKeyWithNumericRangeKey(String hashKey, String rangeKey,
			String sortKeyName) {
		return ofEntries(entry(TABLE_PARTITION_KEY, new AttributeValue().withS(hashKey)),
				entry(INDEX_PARTITION_KEY, new AttributeValue().withS(INDEX_PARTITION_KEY_VALUE)),
				entry(sortKeyName, new AttributeValue().withN(rangeKey)));
	}

	private String getSortKeyValue(Number sortKeyValue) {
		return Instant.now().minus(sortKeyValue.longValue(), ChronoUnit.YEARS).toString();
	}

}
