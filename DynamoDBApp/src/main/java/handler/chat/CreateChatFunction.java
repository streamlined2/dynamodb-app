package handler.chat;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;
import layer.model.chat.Chat;

public class CreateChatFunction extends GenericChatFunction {

	public CreateChatFunction() {
		super(StatusCode.CREATED);
	}

	@Override
	protected String doAction(APIGatewayProxyRequestEvent requestEvent) {
		Chat chat = toEntity(requestEvent.getBody());
		getDynamoDBService().createChat(chat);
		return getJsonResponse("Chat created: " + chat.getName());
	}

}
