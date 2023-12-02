package layer.service;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public abstract class GenericDynamoDBServiceImpl<T> {

	protected static final Integer MAX_LIMIT = Integer.MAX_VALUE;

	protected DynamoDBMapper getDynamoDBMapper() {
		return DynamoDBMapperHelper.INSTANCE;
	}

	private static class DynamoDBMapperHelper {
		static {
			AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
			INSTANCE = new DynamoDBMapper(client);
		}
		private static final DynamoDBMapper INSTANCE;
	}

	protected List<T> getNotFilteredEntityList(Class<T> entityClass, String partitionKey, String lastKey,
			String limit) {
		if (Checks.hasValidLimit(limit)) {
			return getPaginatedNotFilteredEntityList(entityClass, partitionKey, lastKey, limit);
		}
		return getNotPaginatedNotFilteredEntityList(entityClass);
	}

	protected List<T> getPaginatedNotFilteredEntityList(Class<T> entityClass, String partitionKey, String lastKey,
			String limit) {
		Map<String, AttributeValue> startKey = getTableStartKeyMap(partitionKey, lastKey);
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression().withConsistentRead(false)
				.withLimit(Utils.getIntegerValue(limit).orElse(MAX_LIMIT)).withExclusiveStartKey(startKey);
		return getDynamoDBMapper().scanPage(entityClass, scanExpression).getResults();
	}

	protected static Map<String, AttributeValue> getTableStartKeyMap(String partitionKey, String lastHashKey) {
		if (Checks.isValidTableLastHashKey(lastHashKey)) {
			return Map.of(partitionKey, new AttributeValue().withS(lastHashKey));
		}
		return null;// TODO
	}

	protected List<T> getNotPaginatedNotFilteredEntityList(Class<T> entityClass) {
		return getDynamoDBMapper().scan(entityClass, new DynamoDBScanExpression());
	}

}
