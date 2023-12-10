package layer.service;

import java.util.List;
import java.util.Map;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import layer.model.ListParameters;

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

	protected List<T> getNotFilteredEntityList(Class<T> entityClass, String partitionKey,
			ListParameters listParameters) {
		if (listParameters.hasValidLimit()) {
			return getPaginatedNotFilteredEntityList(entityClass, partitionKey, listParameters);
		}
		return getNotPaginatedNotFilteredEntityList(entityClass);
	}

	protected List<T> getPaginatedNotFilteredEntityList(Class<T> entityClass, String partitionKey,
			ListParameters listParameters) {
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression().withConsistentRead(false)
				.withLimit(listParameters.getLimit());
		if (listParameters.isValidTableLastHashKey()) {
			scanExpression = scanExpression.withExclusiveStartKey(
					Map.of(partitionKey, new AttributeValue().withS(listParameters.getHashKey().get())));
		}
		return getDynamoDBMapper().scanPage(entityClass, scanExpression).getResults();
	}

	protected List<T> getNotPaginatedNotFilteredEntityList(Class<T> entityClass) {
		return getDynamoDBMapper().scan(entityClass, new DynamoDBScanExpression());
	}

}
