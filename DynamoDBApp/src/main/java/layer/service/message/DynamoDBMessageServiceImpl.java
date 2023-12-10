package layer.service.message;

import java.util.List;
import java.util.Optional;

import layer.model.ListParameters;
import layer.model.chat.Message;
import layer.service.DynamoDBException;
import layer.service.GenericDynamoDBServiceImpl;

public class DynamoDBMessageServiceImpl extends GenericDynamoDBServiceImpl<Message> implements MessageService {

	private static final String MESSAGE_WITH_ID_NOT_FOUND = "Message with id %s not found";
	private static final String TABLE_PARTITION_KEY = "id";

	@Override
	public List<Message> getMessageList(ListParameters listParameters) {
		return getNotFilteredEntityList(Message.class, TABLE_PARTITION_KEY, listParameters);
	}

	@Override
	public void createMessage(Message message) {
		Message existingMessage = getDynamoDBMapper().load(Message.class, message.getId());
		if (existingMessage != null) {
			throw new DynamoDBException(String.format("Message with id %s already exists", message.getId()));
		}
		getDynamoDBMapper().save(message);
	}

	@Override
	public void deleteMessage(String id) {
		Message existingMessage = getDynamoDBMapper().load(Message.class, id);
		if (existingMessage == null) {
			throw new DynamoDBException(String.format(MESSAGE_WITH_ID_NOT_FOUND, id));
		}
		getDynamoDBMapper().delete(existingMessage);
	}

	@Override
	public void updateMessage(String id, Message message) {
		Message existingMessage = getDynamoDBMapper().load(Message.class, id);
		if (existingMessage == null) {
			throw new DynamoDBException(String.format(MESSAGE_WITH_ID_NOT_FOUND, id));
		}
		message.setId(id);
		getDynamoDBMapper().save(message);
	}

	@Override
	public Optional<Message> findMessage(String id) {
		return Optional.ofNullable(getDynamoDBMapper().load(Message.class, id));
	}

}
