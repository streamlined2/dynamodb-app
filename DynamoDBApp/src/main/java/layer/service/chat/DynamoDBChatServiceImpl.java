package layer.service.chat;

import java.util.List;
import java.util.Optional;

import layer.model.chat.Chat;

public class DynamoDBChatServiceImpl implements ChatService {

	public DynamoDBChatServiceImpl() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Chat> getChatList(String lastKey, String limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createChat(Chat chat) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteChat(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateChat(String name, Chat chat) {
		// TODO Auto-generated method stub

	}

	@Override
	public Optional<Chat> findChat(String name) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

}
