package layer.service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.datamodeling.ScanResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import layer.model.RequestBody;
import layer.model.ResponseMessage;
import layer.model.User;
import static layer.service.Utils.getIntegerValue;

import static java.util.Map.ofEntries;
import static java.util.Map.entry;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DynamoDBServiceImpl implements DynamoDBService {

	private static final String SORT_KEY_UP_ALIAS = "sortUpAlias";
	private static final String SORT_KEY_LOW_ALIAS = "sortLowAlias";
	private static final String SORT_KEY_ALIAS = "sortAlias";
	private static final String PARTITION_KEY_ALIAS = "partAlias";
	private static final String EMAIL_KEY = "email";
	private static final String TABLE_PARTITION_KEY = EMAIL_KEY;
	private static final String INDEX_PARTITION_KEY = "country";
	private static final String COUNTRY_VALUE = "Ukraine";
	private static final String INDEX_PARTITION_KEY_VALUE = COUNTRY_VALUE;
	private static final String COUNTRY_NAME_INDEX = "country-name-index";
	private static final String COUNTRY_LOCATION_INDEX = "country-location-index";
	private static final String COUNTRY_BIRTHDAY_INDEX = "country-birthday-index";
	private static final String LIMIT_QUERY_PARAMETER = "limit";
	private static final String HASH_KEY_QUERY_PARAMETER = "hashkey";
	private static final String RANGE_KEY_QUERY_PARAMETER = "rangekey";
	private static final String NAME_BODY_PARAMETER = "name";
	private static final String LOCATION_BODY_PARAMETER = "location";
	private static final String BIRTHDAY_BODY_PARAMETER = "birthday";
	private static final String USER_WITH_EMAIL_NOT_FOUND = "User with email %s not found";

	private final Gson gson;
	private final DynamoDBMapper dynamoDBMapper;

	public DynamoDBServiceImpl() {
		gson = new Gson();
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
		dynamoDBMapper = new DynamoDBMapper(client);
	}

	@Override
	public String createUser(String inputBody) {

		User user = gson.fromJson(inputBody, User.class);
		user.setCountry(COUNTRY_VALUE);

		User existingUser = dynamoDBMapper.load(User.class, user.getEmail());
		if (existingUser != null) {
			return getJsonResponse(String.format("User with email %s already exists", user.getEmail()));
		}

		Set<String> incorrectSocialMedia = Checks.getIncorrectSocialMedia(user.getSocialMedia());
		if (!incorrectSocialMedia.isEmpty()) {
			return getJsonResponse(String.format("User cannot be created: incorrect social media links %s",
					incorrectSocialMedia.toString()));
		}
		dynamoDBMapper.save(user);
		return getJsonResponse("User created: " + user.getEmail());
	}

	@Override
	public String findUser(Map<String, String> pathParameters) {

		String email = pathParameters.get(EMAIL_KEY);

		User existingUser = dynamoDBMapper.load(User.class, email);
		if (existingUser == null) {
			return getJsonResponse(String.format(USER_WITH_EMAIL_NOT_FOUND, email));
		}
		return gson.toJson(existingUser);
	}

	@Override
	public String updateUser(Map<String, String> pathParameters, String inputBody) {

		String email = pathParameters.get(EMAIL_KEY);

		User existingUser = dynamoDBMapper.load(User.class, email);
		if (existingUser == null) {
			return getJsonResponse(String.format(USER_WITH_EMAIL_NOT_FOUND, email));
		}

		User user = gson.fromJson(inputBody, User.class);
		user.setEmail(email);
		user.setCountry(COUNTRY_VALUE);

		Set<String> incorrectSocialMedia = Checks.getIncorrectSocialMedia(user.getSocialMedia());
		if (!incorrectSocialMedia.isEmpty()) {
			return getJsonResponse(String.format("User cannot be updated: incorrect social media links %s",
					incorrectSocialMedia.toString()));
		}

		dynamoDBMapper.save(user);
		return getJsonResponse("User updated: " + email);
	}

	@Override
	public String deleteUser(Map<String, String> pathParameters) {

		String email = pathParameters.get(EMAIL_KEY);

		User existingUser = dynamoDBMapper.load(User.class, email);
		if (existingUser == null) {
			return getJsonResponse(String.format(USER_WITH_EMAIL_NOT_FOUND, email));
		}
		dynamoDBMapper.delete(existingUser);
		return getJsonResponse(String.format("User with email %s deleted", email));
	}

	@Override
	public String getUsersListResponse(Map<String, String> queryParameters) {
		return getNotFilteredUsersList(queryParameters);
	}

	@Override
	public String getUsersListByQueryResponse(Map<String, String> queryParameters, String inputBody) {
		if (inputBody == null) {
			return getNotFilteredUsersList(queryParameters);
		}
		try {
			RequestBody bodyParameters = extractRequestBodyParameters(inputBody);
			if (Checks.isValidNameParameter(bodyParameters)) {
				return getFilteredUsersList(queryParameters, COUNTRY_NAME_INDEX, NAME_BODY_PARAMETER,
						bodyParameters.getName());
			}
			if (Checks.isValidLocationParameter(bodyParameters)) {
				return getFilteredUsersList(queryParameters, COUNTRY_LOCATION_INDEX, LOCATION_BODY_PARAMETER,
						bodyParameters.getLocation());
			}
			if (Checks.isValidAgeParameter(bodyParameters)) {
				return getFilteredUsersList(queryParameters, BIRTHDAY_BODY_PARAMETER,
						bodyParameters.getAgeLimits().get(0), bodyParameters.getAgeLimits().get(1),
						COUNTRY_BIRTHDAY_INDEX);
			}
			return getNotFilteredUsersList(queryParameters);
		} catch (JsonSyntaxException e) {
			return getNotFilteredUsersList(queryParameters);
		}
	}

	private RequestBody extractRequestBodyParameters(String inputBody) {
		return gson.fromJson(inputBody, RequestBody.class);
	}

	private String getNotFilteredUsersList(Map<String, String> queryParameters) {
		if (hasValidLimit(queryParameters)) {
			return getPaginatedNotFilteredUsersList(queryParameters);
		}
		return getNotPaginatedNotFilteredUsersList();
	}

	private String getFilteredUsersList(Map<String, String> queryParameters, String indexName, String queryParameter,
			String parameterValue) {
		if (hasValidLimit(queryParameters)) {
			return getPaginatedFilteredUsersList(queryParameters, indexName, queryParameter, parameterValue);
		}
		return getNotPaginatedFilteredUsersList(indexName, queryParameter, parameterValue);
	}

	private String getFilteredUsersList(Map<String, String> queryParameters, String queryParameter,
			String parameterLowValue, String parameterUpValue, String indexName) {
		if (hasValidLimit(queryParameters)) {
			return getPaginatedFilteredUsersList(queryParameters, indexName, queryParameter, parameterLowValue,
					parameterUpValue);
		}
		return getNotPaginatedFilteredUsersList(indexName, queryParameter, parameterLowValue, parameterUpValue);
	}

	private String getNotPaginatedNotFilteredUsersList() {
		List<User> users = dynamoDBMapper.scan(User.class, new DynamoDBScanExpression());
		return gson.toJson(users);
	}

	private String getPaginatedNotFilteredUsersList(Map<String, String> stringParameters) {

		String lastKey = extractHashKey(stringParameters);

		Map<String, AttributeValue> startKey = getTableStartKeyMap(lastKey);

		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression().withConsistentRead(false)
				.withLimit(getIntegerValue(extractLimit(stringParameters))).withExclusiveStartKey(startKey);

		ScanResultPage<User> scanResultPage = dynamoDBMapper.scanPage(User.class, scanExpression);

		List<User> users = scanResultPage.getResults();

		return gson.toJson(users);
	}

	private String getNotPaginatedFilteredUsersList(String indexName, String sortKeyName, String sortKeyValue) {

		String partitionKeyLabel = "#" + INDEX_PARTITION_KEY;// TODO
		String sortKeyLabel = "#" + sortKeyName;// TODO

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
		List<User> users = queryResult.getResults();

		return gson.toJson(users);
	}

	private String getNotPaginatedFilteredUsersList(String indexName, String sortKeyName, String sortKeyLowValue,
			String sortKeyUpValue) {

		String sortKeyLowValueStr = Utils.getSortKeyLowValue(sortKeyUpValue);
		String sortKeyUpValueStr = Utils.getSortKeyUpperValue(sortKeyLowValue);

		String partitionKeyLabel = "#" + INDEX_PARTITION_KEY;// TODO
		String sortKeyLabel = "#" + sortKeyName;// TODO

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
		List<User> users = queryResult.getResults();

		return gson.toJson(users);
	}

	private String getPaginatedFilteredUsersList(Map<String, String> stringParameters, String indexName,
			String sortKeyName, String sortKeyValue) {

		Map<String, AttributeValue> startKey = getIndexStringStartKeyMap(sortKeyName, extractHashKey(stringParameters),
				extractRangeKey(stringParameters));

		String partitionKeyLabel = "#" + INDEX_PARTITION_KEY;// TODO
		String sortKeyLabel = "#" + sortKeyName;// TODO

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
				.withLimit(getIntegerValue(extractLimit(stringParameters))).withExclusiveStartKey(startKey);

		QueryResultPage<User> queryResult = dynamoDBMapper.queryPage(User.class, queryExpression);
		List<User> users = queryResult.getResults();

		return gson.toJson(users);
	}

	private String getPaginatedFilteredUsersList(Map<String, String> stringParameters, String indexName,
			String sortKeyName, String sortKeyLowValue, String sortKeyUpValue) {

		String sortKeyLowValueStr = Utils.getSortKeyLowValue(sortKeyUpValue);
		String sortKeyUpValueStr = Utils.getSortKeyUpperValue(sortKeyLowValue);

		Map<String, AttributeValue> startKey = getIndexNumericStartKeyMap(sortKeyName, extractHashKey(stringParameters),
				extractRangeKey(stringParameters));

		String partitionKeyLabel = "#" + INDEX_PARTITION_KEY;// TODO
		String sortKeyLabel = "#" + sortKeyName;// TODO

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
				.withLimit(Utils.getIntegerValue(extractLimit(stringParameters)))
				.withExclusiveStartKey(startKey);

		QueryResultPage<User> queryResult = dynamoDBMapper.queryPage(User.class, queryExpression);
		List<User> users = queryResult.getResults();

		return gson.toJson(users);
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

	private static String extractLimit(Map<String, String> queryParameters) {
		return queryParameters.getOrDefault(LIMIT_QUERY_PARAMETER, null);
	}

	private static String extractHashKey(Map<String, String> queryParameters) {
		return queryParameters.getOrDefault(HASH_KEY_QUERY_PARAMETER, null);
	}

	private static String extractRangeKey(Map<String, String> queryParameters) {
		return queryParameters.getOrDefault(RANGE_KEY_QUERY_PARAMETER, null);
	}

	private boolean hasValidLimit(Map<String, String> stringParameters) {
		if (stringParameters == null) {
			return false;
		}
		String limitStr = extractLimit(stringParameters);
		if (limitStr == null) {
			return false;
		}
		return Checks.isValidLimit(Utils.getIntegerValue(limitStr));
	}

	private String getJsonResponse(String message) {
		return gson.toJson(ResponseMessage.builder().message(message).build());
	}

}
