package layer.service.message;

import java.util.List;
import java.util.Optional;

import layer.model.chat.Message;

public interface MessageService {

	List<Message> getMessageList(Optional<String> lastKey, Optional<String> limit);

	void createMessage(Message message);

	void deleteMessage(String id);

	void updateMessage(String id, Message message);

	Optional<Message> findMessage(String id);

}
