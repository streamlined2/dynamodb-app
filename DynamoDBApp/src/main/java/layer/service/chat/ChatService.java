package layer.service.chat;

import java.util.List;
import java.util.Optional;

import layer.model.ListParameters;
import layer.model.chat.ChatDto;

public interface ChatService {

	List<ChatDto> getChatList(ListParameters listParameters);

	void createChat(ChatDto chat);

	void deleteChat(String name);

	void updateChat(String name, ChatDto chat);

	Optional<ChatDto> findChat(String name);

}
