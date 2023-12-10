package handler.chat;

import handler.GenericFunction;
import handler.StatusCode;
import layer.model.chat.ChatDto;
import layer.service.chat.ChatService;
import layer.service.chat.DynamoDBChatServiceImpl;

public abstract class GenericChatFunction extends GenericFunction<ChatDto> {

	protected static final String CHAT_KEY = "chat";
	protected static final String CHAT_WITH_NAME_NOT_FOUND_MESSAGE = "Chat with name %s not found";

	protected GenericChatFunction(StatusCode successCode) {
		super(successCode);
	}

	protected ChatService getDynamoDBService() {
		return DynamoDBServiceHelper.INSTANCE;
	}

	private static class DynamoDBServiceHelper {
		private static final ChatService INSTANCE = new DynamoDBChatServiceImpl();
	}

}
