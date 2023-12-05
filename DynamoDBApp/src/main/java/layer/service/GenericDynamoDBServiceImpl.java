package layer.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public abstract class GenericDynamoDBServiceImpl<T> {

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

	protected List<T> getNotFilteredEntityList(Class<T> entityClass, String partitionKey, Optional<String> lastKey,
			Optional<String> limit) {
		if (Checks.hasValidLimit(limit)) {
			return getPaginatedNotFilteredEntityList(entityClass, partitionKey, lastKey, limit);
		}
		return getNotPaginatedNotFilteredEntityList(entityClass);
	}

	protected List<T> getPaginatedNotFilteredEntityList(Class<T> entityClass, String partitionKey,
			Optional<String> lastKey, Optional<String> limit) {
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression().withConsistentRead(false)
				.withLimit(Utils.getLimit(limit));
		if (Checks.isValidTableLastHashKey(lastKey)) {
			scanExpression = scanExpression
					.withExclusiveStartKey(Map.of(partitionKey, new AttributeValue().withS(lastKey.get())));
		}
		return getDynamoDBMapper().scanPage(entityClass, scanExpression).getResults();
	}

	protected List<T> getNotPaginatedNotFilteredEntityList(Class<T> entityClass) {
		return getDynamoDBMapper().scan(entityClass, new DynamoDBScanExpression());
	}

}
