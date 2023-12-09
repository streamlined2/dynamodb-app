package layer.service.user;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.gson.JsonSyntaxException;

import layer.model.user.RequestBody;
import layer.model.user.User;
import layer.service.Checks;
import layer.service.DynamoDBException;
import layer.service.GenericDynamoDBServiceImpl;
import layer.service.Utils;

import static java.util.Map.ofEntries;
import static java.util.Map.entry;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DynamoDBUserServiceImpl extends GenericDynamoDBServiceImpl<User> implements UserService {

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
	public void createUser(User user) {
		User existingUser = getDynamoDBMapper().load(User.class, user.getEmail());
		if (existingUser != null) {
			throw new DynamoDBException(String.format("User with email %s already exists", user.getEmail()));
		}
		checkSocialMedia(user, "User cannot be created: incorrect social media links %s");
		getDynamoDBMapper().save(user);
	}

	private void checkSocialMedia(User user, String message) {
		Set<String> incorrectSocialMedia = Checks.getIncorrectSocialMedia(user.getSocialMedia());
		if (!incorrectSocialMedia.isEmpty()) {
			throw new DynamoDBException(String.format(message, incorrectSocialMedia.toString()));
		}
	}

	@Override
	public Optional<User> findUser(String email) {
		return Optional.ofNullable(getDynamoDBMapper().load(User.class, email));
	}

	@Override
	public void updateUser(String email, User user) {
		User existingUser = getDynamoDBMapper().load(User.class, email);
		if (existingUser == null) {
			throw new DynamoDBException(String.format(USER_WITH_EMAIL_NOT_FOUND, email));
		}
		checkSocialMedia(user, "User cannot be updated: incorrect social media links %s");
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
	public List<User> getUserList(Optional<String> lastKey, Optional<String> limit) {
		return getNotFilteredEntityList(User.class, TABLE_PARTITION_KEY, lastKey, limit);
	}

	@Override
	public List<User> getUserListByQuery(Optional<String> rangeKey, Optional<String> lastKey, Optional<String> limit,
			RequestBody parameters) {
		try {
			if (parameters.isNameValid()) {
				return getFilteredUserList(rangeKey, lastKey, limit, COUNTRY_NAME_INDEX, NAME_BODY_PARAMETER,
						parameters.getName());
			}
			if (parameters.isLocationValid()) {
				return getFilteredUserList(rangeKey, lastKey, limit, COUNTRY_LOCATION_INDEX, LOCATION_BODY_PARAMETER,
						parameters.getLocation());
			}
			if (parameters.isAgeValid()) {
				return getFilteredUserList(rangeKey, lastKey, limit, COUNTRY_BIRTHDAY_INDEX, BIRTHDAY_BODY_PARAMETER,
						parameters.getMinAgeOrDefault(), parameters.getMaxAgeOrDefault());
			}
			return getUserList(lastKey, limit);
		} catch (JsonSyntaxException e) {
			return getUserList(lastKey, limit);
		}
	}

	private List<User> getFilteredUserList(Optional<String> rangeKey, Optional<String> lastKey, Optional<String> limit,
			String indexName, String queryParameter, String parameterValue) {
		if (Checks.hasValidLimit(limit)) {
			return getPaginatedFilteredUserList(rangeKey, lastKey, limit, indexName, queryParameter, parameterValue);
		}
		return getNotPaginatedFilteredUserList(indexName, queryParameter, parameterValue);
	}

	private <T extends Number> List<User> getFilteredUserList(Optional<String> rangeKey, Optional<String> lastKey,
			Optional<String> limit, String indexName, String queryParameter, T parameterLowValue, T parameterUpValue) {
		if (Checks.hasValidLimit(limit)) {
			return getPaginatedFilteredUserList(rangeKey, lastKey, limit, indexName, queryParameter, parameterLowValue,
					parameterUpValue);
		}
		return getNotPaginatedFilteredUserList(indexName, queryParameter, parameterLowValue, parameterUpValue);
	}

	private List<User> getNotPaginatedFilteredUserList(String indexName, String sortKeyName, String sortKeyValue) {
		DynamoDBQueryExpression<User> queryExpression = getExpressionForNonPaginatedFilteredUserList(indexName,
				sortKeyName, sortKeyValue);
		return getDynamoDBMapper().queryPage(User.class, queryExpression).getResults();
	}

	private DynamoDBQueryExpression<User> getExpressionForNonPaginatedFilteredUserList(String indexName,
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

	private <T extends Number> List<User> getNotPaginatedFilteredUserList(String indexName, String sortKeyName,
			T sortKeyLowValue, T sortKeyUpValue) {

		DynamoDBQueryExpression<User> queryExpression = getExpressionForNonPaginatedFilteredUserList(indexName,
				sortKeyName, sortKeyLowValue, sortKeyUpValue);
		return getDynamoDBMapper().queryPage(User.class, queryExpression).getResults();
	}

	private <T extends Number> DynamoDBQueryExpression<User> getExpressionForNonPaginatedFilteredUserList(
			String indexName, String sortKeyName, T sortKeyLowValue, T sortKeyUpValue) {

		final String partitionKeyLabel = "#" + INDEX_PARTITION_KEY;
		final String sortKeyLabel = "#" + sortKeyName;

		Map<String, AttributeValue> expressionAttributeValues = ofEntries(
				entry(":" + PARTITION_KEY_ALIAS, new AttributeValue().withS(INDEX_PARTITION_KEY_VALUE)),
				entry(":" + SORT_KEY_LOW_ALIAS, new AttributeValue().withN(Utils.getSortKeyValue(sortKeyLowValue))),
				entry(":" + SORT_KEY_UP_ALIAS, new AttributeValue().withN(Utils.getSortKeyValue(sortKeyUpValue))));

		String conditionExpression = MessageFormat.format("{0} = :{1} and {2} between :{3} and :{4}", partitionKeyLabel,
				PARTITION_KEY_ALIAS, sortKeyLabel, SORT_KEY_LOW_ALIAS, SORT_KEY_UP_ALIAS);

		Map<String, String> expressionAttributeNames = getExpressionAttributeNamesMap(sortKeyName, partitionKeyLabel,
				sortKeyLabel);

		return new DynamoDBQueryExpression<User>().withIndexName(indexName).withConsistentRead(false)
				.withKeyConditionExpression(conditionExpression).withExpressionAttributeNames(expressionAttributeNames)
				.withExpressionAttributeValues(expressionAttributeValues);
	}

	private List<User> getPaginatedFilteredUserList(Optional<String> rangeKey, Optional<String> lastKey,
			Optional<String> limit, String indexName, String sortKeyName, String sortKeyValue) {

		DynamoDBQueryExpression<User> queryExpression = getExpressionForNonPaginatedFilteredUserList(indexName,
				sortKeyName, sortKeyValue).withLimit(Utils.getLimit(limit));
		if (Checks.isValidIndexHashAndRangeKeys(lastKey, rangeKey)) {
			queryExpression = queryExpression
					.withExclusiveStartKey(getStartKeyWithStringRangeKey(lastKey.get(), rangeKey.get(), sortKeyName));
		}
		return getDynamoDBMapper().queryPage(User.class, queryExpression).getResults();
	}

	private Map<String, AttributeValue> getStartKeyWithStringRangeKey(String lastKey, String rangeKey,
			String sortKeyName) {
		return ofEntries(entry(TABLE_PARTITION_KEY, new AttributeValue().withS(lastKey)),
				entry(INDEX_PARTITION_KEY, new AttributeValue().withS(INDEX_PARTITION_KEY_VALUE)),
				entry(sortKeyName, new AttributeValue().withS(rangeKey)));
	}

	private <T extends Number> List<User> getPaginatedFilteredUserList(Optional<String> rangeKey,
			Optional<String> lastKey, Optional<String> limit, String indexName, String sortKeyName, T sortKeyLowValue,
			T sortKeyUpValue) {

		DynamoDBQueryExpression<User> queryExpression = getExpressionForPaginatedFilteredUserList(rangeKey, lastKey,
				limit, indexName, sortKeyName, sortKeyLowValue, sortKeyUpValue);
		return getDynamoDBMapper().queryPage(User.class, queryExpression).getResults();
	}

	private <T extends Number> DynamoDBQueryExpression<User> getExpressionForPaginatedFilteredUserList(
			Optional<String> rangeKey, Optional<String> lastKey, Optional<String> limit, String indexName,
			String sortKeyName, T sortKeyLowValue, T sortKeyUpValue) {

		DynamoDBQueryExpression<User> queryExpression = getExpressionForNonPaginatedFilteredUserList(indexName,
				sortKeyName, sortKeyLowValue, sortKeyUpValue).withLimit(Utils.getLimit(limit));
		if (Checks.isValidIndexHashAndRangeKeys(lastKey, rangeKey)) {
			queryExpression = queryExpression
					.withExclusiveStartKey(getStartKeyWithNumericRangeKey(lastKey.get(), rangeKey.get(), sortKeyName));
		}
		return queryExpression;
	}

	private Map<String, AttributeValue> getStartKeyWithNumericRangeKey(String lastKey, String rangeKey,
			String sortKeyName) {
		return ofEntries(entry(TABLE_PARTITION_KEY, new AttributeValue().withS(lastKey)),
				entry(INDEX_PARTITION_KEY, new AttributeValue().withS(INDEX_PARTITION_KEY_VALUE)),
				entry(sortKeyName, new AttributeValue().withN(rangeKey)));
	}

	private static Map<String, String> getExpressionAttributeNamesMap(String sortKeyName, String partitionKeyLabel,
			String sortKeyLabel) {
		return ofEntries(entry(partitionKeyLabel, INDEX_PARTITION_KEY), entry(sortKeyLabel, sortKeyName));
	}

	private static Map<String, AttributeValue> getExpressionAttributeValuesMap(String sortKeyValue,
			String partitionKeyAlias, String sortKeyAlias) {
		return ofEntries(entry(":" + partitionKeyAlias, new AttributeValue().withS(INDEX_PARTITION_KEY_VALUE)),
				entry(":" + sortKeyAlias, new AttributeValue().withS(sortKeyValue)));
	}

}
