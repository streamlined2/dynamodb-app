package layer.model.chat;

import java.util.HashSet;
import java.util.Set;

import layer.model.Dto;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ChatDto implements Dto<Chat> {

	private String name;
	private Long updatedTime;
	private Set<String> userEmails;

	public Chat toEntity() {
		return Chat.builder().name(getName()).updatedTime(getUpdatedTime()).userEmails(new HashSet<>(getUserEmails()))
				.build();
	}

}
