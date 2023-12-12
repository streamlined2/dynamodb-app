package layer.service;

import java.util.List;
import java.util.Map;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import layer.model.Dto;
import layer.model.Entity;
import layer.model.ListParameters;

public abstract class GenericDynamoDBServiceImpl<E extends Entity<D>, D extends Dto<E>> {

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

	protected List<D> getNotFilteredEntityList(Class<E> entityClass, String partitionKey,
			ListParameters listParameters) {
		if (listParameters.hasValidLimit()) {
			return getPaginatedNotFilteredEntityList(entityClass, partitionKey, listParameters);
		}
		return getNotPaginatedNotFilteredEntityList(entityClass);
	}

	protected List<D> getPaginatedNotFilteredEntityList(Class<E> entityClass, String partitionKey,
			ListParameters listParameters) {
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression().withConsistentRead(false)
				.withLimit(listParameters.getLimit());
		if (listParameters.isValidTableLastHashKey()) {
			scanExpression = scanExpression.withExclusiveStartKey(
					Map.of(partitionKey, new AttributeValue().withS(listParameters.getHashKey().get())));
		}
		return Entity.toDtoList(getDynamoDBMapper().scanPage(entityClass, scanExpression).getResults());
	}

	protected List<D> getNotPaginatedNotFilteredEntityList(Class<E> entityClass) {
		return Entity.toDtoList(getDynamoDBMapper().scan(entityClass, new DynamoDBScanExpression()));
	}

}
