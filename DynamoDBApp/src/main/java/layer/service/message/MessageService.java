package layer.service.message;

import java.util.List;
import java.util.Optional;

import layer.model.ListParameters;
import layer.model.chat.Message;

public interface MessageService {

	List<Message> getMessageList(ListParameters listParameters);

	void createMessage(Message message);

	void deleteMessage(String id);

	void updateMessage(String id, Message message);

	Optional<Message> findMessage(String id);

}
