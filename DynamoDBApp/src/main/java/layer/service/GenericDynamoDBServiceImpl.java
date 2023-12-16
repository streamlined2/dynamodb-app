package layer.service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import layer.model.Dto;
import layer.model.Entity;

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

}
