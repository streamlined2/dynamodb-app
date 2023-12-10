package handler.chat;

import java.util.Optional;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;
import layer.model.chat.ChatDto;

public class GetChatFunction extends GenericChatFunction {

	public GetChatFunction(StatusCode successCode) {
		super(successCode);
	}

	@Override
	protected String doAction(APIGatewayProxyRequestEvent requestEvent) {
		String name = requestEvent.getPathParameters().get(CHAT_KEY);
		Optional<ChatDto> chatDto = getDynamoDBService().findChat(name);
		return chatDto.map(this::toJson).orElse(getJsonResponse(String.format(CHAT_WITH_NAME_NOT_FOUND_MESSAGE, name)));
	}

}
