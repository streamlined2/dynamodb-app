package layer.model.chat;

import layer.model.Dto;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MessageDto implements Dto<Message> {

	private String id;
	private Long createdTime;
	private String chatName;
	private String senderEmail;
	private String receiverEmail;
	private String text;
	private Boolean seen;

	public Message toEntity() {
		return Message.builder().id(getId()).createdTime(getCreatedTime()).chatName(getChatName())
				.senderEmail(getSenderEmail()).receiverEmail(getReceiverEmail()).text(getText()).seen(getSeen())
				.build();
	}

}
