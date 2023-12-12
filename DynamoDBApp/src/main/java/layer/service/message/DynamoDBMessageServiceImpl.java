package layer.service.message;

import java.util.List;
import java.util.Optional;

import layer.model.ListParameters;
import layer.model.chat.Message;
import layer.model.chat.MessageDto;
import layer.service.DynamoDBException;
import layer.service.GenericDynamoDBServiceImpl;

public class DynamoDBMessageServiceImpl extends GenericDynamoDBServiceImpl<Message, MessageDto>
		implements MessageService {

	private static final String MESSAGE_WITH_ID_NOT_FOUND = "Message with id %s not found";
	private static final String TABLE_PARTITION_KEY = "id";

	@Override
	public List<MessageDto> getMessageList(ListParameters listParameters) {
		return getNotFilteredEntityList(Message.class, TABLE_PARTITION_KEY, listParameters);
	}

	@Override
	public void createMessage(MessageDto messageDto) {
		Message existingMessage = getDynamoDBMapper().load(Message.class, messageDto.getId());
		if (existingMessage != null) {
			throw new DynamoDBException(String.format("Message with id %s already exists", messageDto.getId()));
		}
		Message message = messageDto.toEntity();
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
	public void updateMessage(String id, MessageDto messageDto) {
		Message existingMessage = getDynamoDBMapper().load(Message.class, id);
		if (existingMessage == null) {
			throw new DynamoDBException(String.format(MESSAGE_WITH_ID_NOT_FOUND, id));
		}
		Message message = messageDto.toEntity();
		message.setId(id);
		getDynamoDBMapper().save(message);
	}

	@Override
	public Optional<MessageDto> findMessage(String id) {
		return Optional.ofNullable(getDynamoDBMapper().load(Message.class, id)).map(Message::toDto);
	}

}
