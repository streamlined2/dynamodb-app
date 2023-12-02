package layer.service.chat;

import java.util.List;
import java.util.Optional;

import layer.model.chat.Chat;
import layer.service.DynamoDBException;
import layer.service.GenericDynamoDBServiceImpl;

public class DynamoDBChatServiceImpl extends GenericDynamoDBServiceImpl<Chat> implements ChatService {

	private static final String CHAT_WITH_NAME_NOT_FOUND = "Chat with name %s not found";
	private static final String TABLE_PARTITION_KEY = "name";

	@Override
	public List<Chat> getChatList(String lastKey, String limit) {
		return getNotFilteredEntityList(Chat.class, TABLE_PARTITION_KEY, lastKey, limit);
	}

	@Override
	public void createChat(Chat chat) {
		Chat existingChat = getDynamoDBMapper().load(Chat.class, chat.getName());
		if (existingChat != null) {
			throw new DynamoDBException(String.format("Chat with name %s already exists", chat.getName()));
		}
		getDynamoDBMapper().save(chat);
	}

	@Override
	public void deleteChat(String name) {
		Chat existingChat = getDynamoDBMapper().load(Chat.class, name);
		if (existingChat == null) {
			throw new DynamoDBException(String.format(CHAT_WITH_NAME_NOT_FOUND, name));
		}
		getDynamoDBMapper().delete(existingChat);
	}

	@Override
	public void updateChat(String name, Chat chat) {
		Chat existingChat = getDynamoDBMapper().load(Chat.class, name);
		if (existingChat == null) {
			throw new DynamoDBException(String.format(CHAT_WITH_NAME_NOT_FOUND, name));
		}
		chat.setName(name);
		getDynamoDBMapper().save(chat);
	}

	@Override
	public Optional<Chat> findChat(String name) {
		return Optional.ofNullable(getDynamoDBMapper().load(Chat.class, name));
	}

}
