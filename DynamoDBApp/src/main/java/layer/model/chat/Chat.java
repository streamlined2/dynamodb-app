package layer.model.chat;

import java.util.HashSet;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGenerateStrategy;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedTimestamp;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@DynamoDBTable(tableName = "it-marathon-v3-chat-db")
public class Chat {

	@DynamoDBHashKey(attributeName = "name")
	@EqualsAndHashCode.Include
	private String name;
	
	@DynamoDBRangeKey(attributeName = "updated_time")
	@DynamoDBAutoGeneratedTimestamp(strategy = DynamoDBAutoGenerateStrategy.ALWAYS)
	private Long updatedTime;
	
	@DynamoDBAttribute(attributeName = "user_emails")
	private HashSet<String> userEmails;

}