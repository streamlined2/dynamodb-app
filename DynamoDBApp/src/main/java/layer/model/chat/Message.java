package layer.model.chat;

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
@DynamoDBTable(tableName = "it-marathon-v3-message-db")
public class Message {

	@DynamoDBHashKey(attributeName = "chat_name")
	@EqualsAndHashCode.Include
	private String chatName;

	@DynamoDBAutoGeneratedTimestamp(strategy = DynamoDBAutoGenerateStrategy.CREATE)
	@DynamoDBRangeKey(attributeName = "created_time")
	@EqualsAndHashCode.Include
	private Long createdTime;

	@DynamoDBAttribute(attributeName = "sender_email")
	@EqualsAndHashCode.Include
	private String senderEmail;

	@DynamoDBAttribute(attributeName = "receiver_email")
	@EqualsAndHashCode.Include
	private String receiverEmail;

	@DynamoDBAttribute(attributeName = "text")
	private String text;

	@DynamoDBAttribute(attributeName = "seen")
	private Boolean seen;

}
