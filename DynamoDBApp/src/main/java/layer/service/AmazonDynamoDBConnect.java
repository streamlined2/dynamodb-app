package layer.service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import lombok.Getter;

@Getter
public class AmazonDynamoDBConnect {

	private final DynamoDBMapper dynamoDBMapper;

	public AmazonDynamoDBConnect() {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
		dynamoDBMapper = new DynamoDBMapper(client);
	}
}
