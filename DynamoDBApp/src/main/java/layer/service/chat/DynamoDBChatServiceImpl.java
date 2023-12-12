package layer.service.chat;

import java.util.List;
import java.util.Optional;

import layer.model.ListParameters;
import layer.model.chat.Chat;
import layer.model.chat.ChatDto;
import layer.service.DynamoDBException;
import layer.service.GenericDynamoDBServiceImpl;

public class DynamoDBChatServiceImpl extends GenericDynamoDBServiceImpl<Chat, ChatDto> implements ChatService {

	private static final String CHAT_WITH_NAME_NOT_FOUND = "Chat with name %s not found";
	private static final String TABLE_PARTITION_KEY = "name";

	@Override
	public List<ChatDto> getChatList(ListParameters listParameters) {
		return getNotFilteredEntityList(Chat.class, TABLE_PARTITION_KEY, listParameters);
	}

	@Override
	public void createChat(ChatDto chatDto) {
		Chat existingChat = getDynamoDBMapper().load(Chat.class, chatDto.getName());
		if (existingChat != null) {
			throw new DynamoDBException(String.format("Chat with name %s already exists", chatDto.getName()));
		}
		getDynamoDBMapper().save(chatDto.toEntity());
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
	public void updateChat(String name, ChatDto chatDto) {
		Chat existingChat = getDynamoDBMapper().load(Chat.class, name);
		if (existingChat == null) {
			throw new DynamoDBException(String.format(CHAT_WITH_NAME_NOT_FOUND, name));
		}
		Chat chat = chatDto.toEntity();
		chat.setName(name);
		getDynamoDBMapper().save(chat);
	}

	@Override
	public Optional<ChatDto> findChat(String name) {
		return Optional.ofNullable(getDynamoDBMapper().load(Chat.class, name)).map(Chat::toDto);
	}

}
