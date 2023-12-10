package layer.service.chat;

import java.util.List;
import java.util.Optional;

import layer.model.ListParameters;
import layer.model.chat.Chat;

public interface ChatService {

	List<Chat> getChatList(ListParameters listParameters);

	void createChat(Chat chat);

	void deleteChat(String name);

	void updateChat(String name, Chat chat);

	Optional<Chat> findChat(String name);

}
