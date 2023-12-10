package handler.chat;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;
import layer.model.chat.ChatDto;

public class CreateChatFunction extends GenericChatFunction {

	public CreateChatFunction() {
		super(StatusCode.CREATED);
	}

	@Override
	protected String doAction(APIGatewayProxyRequestEvent requestEvent) {
		ChatDto chatDto = toDto(requestEvent.getBody());
		getDynamoDBService().createChat(chatDto);
		return getJsonResponse("Chat created: " + chatDto.getName());
	}

}
