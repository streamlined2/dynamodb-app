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
	public List<User> getUserList(String lastKey, String limit) {
		return getNotFilteredEntityList(User.class, TABLE_PARTITION_KEY, lastKey, limit);
	}

	@Override
	public List<User> getUserListByQuery(String rangeKey, String lastKey, String limit,
			Optional<RequestBody> parameters) {
		try {
			if (Checks.isValidNameParameter(parameters)) {
				return getFilteredUserList(rangeKey, lastKey, limit, COUNTRY_NAME_INDEX, NAME_BODY_PARAMETER,
						parameters.get().getName());
			}
			if (Checks.isValidLocationParameter(parameters)) {
				return getFilteredUserList(rangeKey, lastKey, limit, COUNTRY_LOCATION_INDEX, LOCATION_BODY_PARAMETER,
						parameters.get().getLocation());
			}
			if (Checks.isValidAgeParameter(parameters)) {
				return getFilteredUserList(rangeKey, lastKey, limit, COUNTRY_BIRTHDAY_INDEX, BIRTHDAY_BODY_PARAMETER,
						parameters.get().getAgeLimits().get(0), parameters.get().getAgeLimits().get(1));
			}
			return getUserList(lastKey, limit);
		} catch (JsonSyntaxException e) {
			return getUserList(lastKey, limit);
		}
	}

	private List<User> getFilteredUserList(String rangeKey, String lastKey, String limit, String indexName,
			String queryParameter, String parameterValue) {
		if (Checks.hasValidLimit(limit)) {
			return getPaginatedFilteredUserList(rangeKey, lastKey, limit, indexName, queryParameter, parameterValue);
		}
		return getNotPaginatedFilteredUserList(indexName, queryParameter, parameterValue);
	}

	private List<User> getFilteredUserList(String rangeKey, String lastKey, String limit, String indexName,
			String queryParameter, String parameterLowValue, String parameterUpValue) {
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

		String conditionExpression = MessageFormat.format("{0} = :{1} and begins_with ({2}, :{3})", partitionKeyLabel,
				PARTITION_KEY_ALIAS, sortKeyLabel, SORT_KEY_ALIAS);

		Map<String, String> expressionAttributeNames = getExpressionAttributeNamesMap(sortKeyName, partitionKeyLabel,
				sortKeyLabel);

		return new DynamoDBQueryExpression<User>().withIndexName(indexName).withConsistentRead(false)
				.withKeyConditionExpression(conditionExpression).withExpressionAttributeNames(expressionAttributeNames)
				.withExpressionAttributeValues(expressionAttributeValues);
	}

	private List<User> getNotPaginatedFilteredUserList(String indexName, String sortKeyName, String sortKeyLowValue,
			String sortKeyUpValue) {

		DynamoDBQueryExpression<User> queryExpression = getExpressionForNonPaginatedFilteredUserList(indexName,
				sortKeyName, sortKeyLowValue, sortKeyUpValue);

		return getDynamoDBMapper().queryPage(User.class, queryExpression).getResults();
	}

	private DynamoDBQueryExpression<User> getExpressionForNonPaginatedFilteredUserList(String indexName,
			String sortKeyName, String sortKeyLowValue, String sortKeyUpValue) {
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

		return new DynamoDBQueryExpression<User>().withIndexName(indexName).withConsistentRead(false)
				.withKeyConditionExpression(conditionExpression).withExpressionAttributeNames(expressionAttributeNames)
				.withExpressionAttributeValues(expressionAttributeValues);
	}

	private List<User> getPaginatedFilteredUserList(String rangeKey, String lastKey, String limit, String indexName,
			String sortKeyName, String sortKeyValue) {

		Map<String, AttributeValue> startKey = getIndexStringStartKeyMap(sortKeyName, lastKey, rangeKey);
		DynamoDBQueryExpression<User> queryExpression = getExpressionForNonPaginatedFilteredUserList(indexName,
				sortKeyName, sortKeyValue).withLimit(Utils.getIntegerValue(limit).orElse(MAX_LIMIT))
				.withExclusiveStartKey(startKey);
		return getDynamoDBMapper().queryPage(User.class, queryExpression).getResults();
	}

	private List<User> getPaginatedFilteredUserList(String rangeKey, String lastKey, String limit, String indexName,
			String sortKeyName, String sortKeyLowValue, String sortKeyUpValue) {

		DynamoDBQueryExpression<User> queryExpression = getExpressionForPaginatedFilteredUserList(rangeKey, lastKey,
				limit, indexName, sortKeyName, sortKeyLowValue, sortKeyUpValue);
		return getDynamoDBMapper().queryPage(User.class, queryExpression).getResults();
	}

	private DynamoDBQueryExpression<User> getExpressionForPaginatedFilteredUserList(String rangeKey, String lastKey,
			String limit, String indexName, String sortKeyName, String sortKeyLowValue, String sortKeyUpValue) {

		Map<String, AttributeValue> startKey = getIndexNumericStartKeyMap(sortKeyName, lastKey, rangeKey);
		return getExpressionForNonPaginatedFilteredUserList(indexName, sortKeyName, sortKeyLowValue, sortKeyUpValue)
				.withLimit(Utils.getIntegerValue(limit).orElse(MAX_LIMIT)).withExclusiveStartKey(startKey);
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

}
