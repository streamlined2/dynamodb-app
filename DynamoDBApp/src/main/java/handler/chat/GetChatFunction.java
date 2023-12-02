package handler.chat;

import java.util.Optional;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;
import layer.model.chat.Chat;

public class GetChatFunction extends GenericChatFunction {

	public GetChatFunction(StatusCode successCode) {
		super(successCode);
	}

	@Override
	protected String doAction(APIGatewayProxyRequestEvent requestEvent) {
		String name = requestEvent.getPathParameters().get(CHAT_KEY);
		Optional<Chat> chat = getDynamoDBService().findChat(name);
		return chat.map(this::toJson).orElse(getJsonResponse(String.format(CHAT_WITH_NAME_NOT_FOUND_MESSAGE, name)));
	}

}
