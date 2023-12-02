package handler.chat;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;
import layer.model.chat.Chat;
import layer.service.DynamoDBException;

public class CreateChatFunction extends GenericChatFunction {

	public CreateChatFunction() {
		super(StatusCode.CREATED);
	}

	@Override
	protected String doAction(APIGatewayProxyRequestEvent requestEvent) {
		try {
			Chat chat = toChat(requestEvent.getBody());
			getDynamoDBService().createChat(chat);
			return getJsonResponse("Chat created: " + chat.getName());
		} catch (DynamoDBException e) {
			return getJsonResponse(e.getMessage());
		}
	}

}
