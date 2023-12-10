package layer.service.message;

import java.util.List;
import java.util.Optional;

import layer.model.ListParameters;
import layer.model.chat.MessageDto;

public interface MessageService {

	List<MessageDto> getMessageList(ListParameters listParameters);

	void createMessage(MessageDto message);

	void deleteMessage(String id);

	void updateMessage(String id, MessageDto message);

	Optional<MessageDto> findMessage(String id);

}
