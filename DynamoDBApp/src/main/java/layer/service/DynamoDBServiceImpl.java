package layer.service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.gson.JsonSyntaxException;

import layer.model.RequestBody;
import layer.model.User;
import static layer.service.Utils.getIntegerValue;

import static java.util.Map.ofEntries;
import static java.util.Map.entry;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DynamoDBServiceImpl implements DynamoDBService {

	private static final String SORT_KEY_UP_ALIAS = "sortUpAlias";
	private static final String SORT_KEY_LOW_ALIAS = "sortLowAlias";
	private static final String SORT_KEY_ALIAS = "sortAlias";
	private static final String PARTITION_KEY_ALIAS = "partAlias";
	private static final String EMAIL_KEY = "email";
	private static final String TABLE_PARTITION_KEY = EMAIL_KEY;
	private static final String INDEX_PARTITION_KEY = "country";
	private static final String INDEX_PARTITION_KEY_VALUE = "Ukraine";
	private static final String COUNTRY_NAME_INDEX = "country-name-index";
	private static final String COUNTRY_LOCATION_INDEX = "country-location-index";
	private static final String COUNTRY_BIRTHDAY_INDEX = "country-birthday-index";
	private static final String NAME_BODY_PARAMETER = "name";
	private static final String LOCATION_BODY_PARAMETER = "location";
	private static final String BIRTHDAY_BODY_PARAMETER = "birthday";
	private static final Integer MAX_LIMIT = Integer.valueOf(Integer.MAX_VALUE);
	private static final String USER_WITH_EMAIL_NOT_FOUND = "User with email %s not found";

	private final DynamoDBMapper dynamoDBMapper;

	public DynamoDBServiceImpl() {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
		dynamoDBMapper = new DynamoDBMapper(client);
	}

	@Override
	public void createUser(User user) {
		User existingUser = dynamoDBMapper.load(User.class, user.getEmail());
		if (existingUser != null) {
			throw new DynamoDBException(String.format("User with email %s already exists", user.getEmail()));
		}
		checkSocialMedia(user, "User cannot be created: incorrect social media links %s");
		dynamoDBMapper.save(user);
	}

	private void checkSocialMedia(User user, String message) {
		Set<String> incorrectSocialMedia = Checks.getIncorrectSocialMedia(user.getSocialMedia());
		if (!incorrectSocialMedia.isEmpty()) {
			throw new DynamoDBException(String.format(message, incorrectSocialMedia.toString()));
		}
	}

	@Override
	public Optional<User> findUser(String email) {
		return Optional.ofNullable(dynamoDBMapper.load(User.class, email));
	}

	@Override
	public void updateUser(String email, User user) {
		User existingUser = dynamoDBMapper.load(User.class, email);
		if (existingUser == null) {
			throw new DynamoDBException(String.format(USER_WITH_EMAIL_NOT_FOUND, email));
		}
		checkSocialMedia(user, "User cannot be updated: incorrect social media links %s");
		user.setEmail(email);
		dynamoDBMapper.save(user);
	}

	@Override
	public void deleteUser(String email) {
		User existingUser = dynamoDBMapper.load(User.class, email);
		if (existingUser == null) {
			throw new DynamoDBException(String.format(USER_WITH_EMAIL_NOT_FOUND, email));
		}
		dynamoDBMapper.delete(existingUser);
	}

	@Override
	public List<User> getUserList(String lastKey, String limit) {
		return getNotFilteredUsersList(lastKey, limit);
	}

	@Override
	public List<User> getUserListByQuery(String rangeKey, String lastKey, String limit,
			Optional<RequestBody> parameters) {
		try {
			if (Checks.isValidNameParameter(parameters)) {
				return getFilteredUsersList(rangeKey, lastKey, limit, COUNTRY_NAME_INDEX, NAME_BODY_PARAMETER,
						parameters.get().getName());
			}
			if (Checks.isValidLocationParameter(parameters)) {
				return getFilteredUsersList(rangeKey, lastKey, limit, COUNTRY_LOCATION_INDEX, LOCATION_BODY_PARAMETER,
						parameters.get().getLocation());
			}
			if (Checks.isValidAgeParameter(parameters)) {
				return getFilteredUsersList(rangeKey, lastKey, limit, COUNTRY_BIRTHDAY_INDEX, BIRTHDAY_BODY_PARAMETER,
						parameters.get().getAgeLimits().get(0), parameters.get().getAgeLimits().get(1));
			}
			return getNotFilteredUsersList(lastKey, limit);
		} catch (JsonSyntaxException e) {
			return getNotFilteredUsersList(lastKey, limit);
		}
	}

	private List<User> getNotFilteredUsersList(String lastKey, String limit) {
		if (hasValidLimit(limit)) {
			return getPaginatedNotFilteredUsersList(lastKey, limit);
		}
		return getNotPaginatedNotFilteredUsersList();
	}

	private List<User> getFilteredUsersList(String rangeKey, String lastKey, String limit, String indexName,
			String queryParameter, String parameterValue) {
		if (hasValidLimit(limit)) {
			return getPaginatedFilteredUsersList(rangeKey, lastKey, limit, indexName, queryParameter, parameterValue);
		}
		return getNotPaginatedFilteredUsersList(indexName, queryParameter, parameterValue);
	}

	private List<User> getFilteredUsersList(String rangeKey, String lastKey, String limit, String indexName,
			String queryParameter, String parameterLowValue, String parameterUpValue) {
		if (hasValidLimit(limit)) {
			return getPaginatedFilteredUsersList(rangeKey, lastKey, limit, indexName, queryParameter, parameterLowValue,
					parameterUpValue);
		}
		return getNotPaginatedFilteredUsersList(indexName, queryParameter, parameterLowValue, parameterUpValue);
	}

	private List<User> getNotPaginatedNotFilteredUsersList() {
		return dynamoDBMapper.scan(User.class, new DynamoDBScanExpression());
	}

	private List<User> getPaginatedNotFilteredUsersList(String lastKey, String limit) {
		Map<String, AttributeValue> startKey = getTableStartKeyMap(lastKey);
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression().withConsistentRead(false)
				.withLimit(getIntegerValue(limit).orElse(MAX_LIMIT)).withExclusiveStartKey(startKey);
		return dynamoDBMapper.scanPage(User.class, scanExpression).getResults();
	}

	private List<User> getNotPaginatedFilteredUsersList(String indexName, String sortKeyName, String sortKeyValue) {
		final String partitionKeyLabel = "#" + INDEX_PARTITION_KEY;
		final String sortKeyLabel = "#" + sortKeyName;

		Map<String, AttributeValue> expressionAttributeValues = getExpressionAttributeValuesMap(sortKeyValue,
				PARTITION_KEY_ALIAS, SORT_KEY_ALIAS);

		String conditionExpression = MessageFormat.format("{0} = :{1} and begins_with ({2}, :{3})", partitionKeyLabel,
				PARTITION_KEY_ALIAS, sortKeyLabel, SORT_KEY_ALIAS);

		Map<String, String> expressionAttributeNames = getExpressionAttributeNamesMap(sortKeyName, partitionKeyLabel,
				sortKeyLabel);

		DynamoDBQueryExpression<User> queryExpression = new DynamoDBQueryExpression<User>().withIndexName(indexName)
				.withConsistentRead(false).withKeyConditionExpression(conditionExpression)
				.withExpressionAttributeNames(expressionAttributeNames)
				.withExpressionAttributeValues(expressionAttributeValues);

		QueryResultPage<User> queryResult = dynamoDBMapper.queryPage(User.class, queryExpression);
		return queryResult.getResults();
	}

	private List<User> getNotPaginatedFilteredUsersList(String indexName, String sortKeyName, String sortKeyLowValue,
			String sortKeyUpValue) {

		String sortKeyLowValueStr = Utils.getSortKeyLowValue(sortKeyUpValue);
		String sortKeyUpValueStr = Utils.getSortKeyUpperValue(sortKeyLowValue);

		final String partitionKeyLabel = "#" + INDEX_PARTITION_KEY;
		final String sortKeyLabel = "#" + sortKeyName;

		Map<String, AttributeValue> expressionAttributeValues = ofEntries(
				entry(":" + PARTITION_KEY_ALIAS, new AttributeValue().withS(INDEX_PARTITION_KEY_VALUE)),
				entry(":" + SORT_KEY_LOW_ALIAS, new AttributeValue().withN(sortKeyLowValueStr)),
				entry(":" + SORT_KEY_UP_ALIAS, new AttributeValue().withN(sortKeyUpValueStr)));

		String conditionExpression = MessageFormat.format("{0} = :{1} and {2} between :{3} and :{4}", partitionKeyLabel,
				PARTITION_KEY_ALIAS, sortKeyLabel, SORT_KEY_LOW_ALIAS, SORT_KEY_UP_ALIAS);

		Map<String, String> expressionAttributeNames = getExpressionAttributeNamesMap(sortKeyName, partitionKeyLabel,
				sortKeyLabel);

		DynamoDBQueryExpression<User> queryExpression = new DynamoDBQueryExpression<User>().withIndexName(indexName)
				.withConsistentRead(false).withKeyConditionExpression(conditionExpression)
				.withExpressionAttributeNames(expressionAttributeNames)
				.withExpressionAttributeValues(expressionAttributeValues);

		QueryResultPage<User> queryResult = dynamoDBMapper.queryPage(User.class, queryExpression);
		return queryResult.getResults();
	}

	private List<User> getPaginatedFilteredUsersList(String rangeKey, String lastKey, String limit, String indexName,
			String sortKeyName, String sortKeyValue) {

		Map<String, AttributeValue> startKey = getIndexStringStartKeyMap(sortKeyName, lastKey, rangeKey);

		final String partitionKeyLabel = "#" + INDEX_PARTITION_KEY;
		final String sortKeyLabel = "#" + sortKeyName;

		Map<String, AttributeValue> expressionAttributeValues = getExpressionAttributeValuesMap(sortKeyValue,
				PARTITION_KEY_ALIAS, SORT_KEY_ALIAS);

		String conditionExpression = MessageFormat.format("{0} = :{1} and begins_with ({2}, :{3})", partitionKeyLabel,
				PARTITION_KEY_ALIAS, sortKeyLabel, SORT_KEY_ALIAS);

		Map<String, String> expressionAttributeNames = getExpressionAttributeNamesMap(sortKeyName, partitionKeyLabel,
				sortKeyLabel);

		DynamoDBQueryExpression<User> queryExpression = new DynamoDBQueryExpression<User>().withIndexName(indexName)
				.withConsistentRead(false).withKeyConditionExpression(conditionExpression)
				.withExpressionAttributeNames(expressionAttributeNames)
				.withExpressionAttributeValues(expressionAttributeValues)
				.withLimit(getIntegerValue(limit).orElse(MAX_LIMIT)).withExclusiveStartKey(startKey);

		QueryResultPage<User> queryResult = dynamoDBMapper.queryPage(User.class, queryExpression);
		return queryResult.getResults();
	}

	private List<User> getPaginatedFilteredUsersList(String rangeKey, String lastKey, String limit, String indexName,
			String sortKeyName, String sortKeyLowValue, String sortKeyUpValue) {

		String sortKeyLowValueStr = Utils.getSortKeyLowValue(sortKeyUpValue);
		String sortKeyUpValueStr = Utils.getSortKeyUpperValue(sortKeyLowValue);

		Map<String, AttributeValue> startKey = getIndexNumericStartKeyMap(sortKeyName, lastKey, rangeKey);

		final String partitionKeyLabel = "#" + INDEX_PARTITION_KEY;
		final String sortKeyLabel = "#" + sortKeyName;

		Map<String, AttributeValue> expressionAttributeValues = ofEntries(
				entry(":" + PARTITION_KEY_ALIAS, new AttributeValue().withS(INDEX_PARTITION_KEY_VALUE)),
				entry(":" + SORT_KEY_LOW_ALIAS, new AttributeValue().withN(sortKeyLowValueStr)),
				entry(":" + SORT_KEY_UP_ALIAS, new AttributeValue().withN(sortKeyUpValueStr)));

		String conditionExpression = MessageFormat.format("{0} = :{1} and {2} between :{3} and :{4}", partitionKeyLabel,
				PARTITION_KEY_ALIAS, sortKeyLabel, SORT_KEY_LOW_ALIAS, SORT_KEY_UP_ALIAS);

		Map<String, String> expressionAttributeNames = getExpressionAttributeNamesMap(sortKeyName, partitionKeyLabel,
				sortKeyLabel);

		DynamoDBQueryExpression<User> queryExpression = new DynamoDBQueryExpression<User>().withIndexName(indexName)
				.withConsistentRead(false).withKeyConditionExpression(conditionExpression)
				.withExpressionAttributeNames(expressionAttributeNames)
				.withExpressionAttributeValues(expressionAttributeValues)
				.withLimit(Utils.getIntegerValue(limit).orElse(MAX_LIMIT)).withExclusiveStartKey(startKey);

		QueryResultPage<User> queryResult = dynamoDBMapper.queryPage(User.class, queryExpression);
		return queryResult.getResults();
	}

	private static Map<String, AttributeValue> getTableStartKeyMap(String lastHashKey) {
		if (Checks.isValidTableLastHashKey(lastHashKey)) {
			return Map.of(TABLE_PARTITION_KEY, new AttributeValue().withS(lastHashKey));
		}
		return null;// TODO
	}

	private static Map<String, AttributeValue> getIndexStringStartKeyMap(String sortKeyName, String lastHashKey,
			String lastRangeKey) {
		if (Checks.isValidIndexHashAndRangeKeys(lastHashKey, lastRangeKey)) {
			return ofEntries(entry(TABLE_PARTITION_KEY, new AttributeValue().withS(lastHashKey)),
					entry(INDEX_PARTITION_KEY, new AttributeValue().withS(INDEX_PARTITION_KEY_VALUE)),
					entry(sortKeyName, new AttributeValue().withS(lastRangeKey)));
		}
		return null;// TODO
	}

	private static Map<String, AttributeValue> getIndexNumericStartKeyMap(String sortKeyName, String lastHashKey,
			String lastRangeKey) {
		if (Checks.isValidIndexHashAndRangeKeys(lastHashKey, lastRangeKey)) {
			return ofEntries(entry(TABLE_PARTITION_KEY, new AttributeValue().withS(lastHashKey)),
					entry(INDEX_PARTITION_KEY, new AttributeValue().withS(INDEX_PARTITION_KEY_VALUE)),
					entry(sortKeyName, new AttributeValue().withN(lastRangeKey)));
		}
		return null;// TODO
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

	private boolean hasValidLimit(String limit) {
		if (limit == null || limit.isBlank()) {
			return false;
		}
		return Checks.isValidLimit(Utils.getIntegerValue(limit));
	}

}
