package handler.chat;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;
import layer.service.DynamoDBException;

public class UpdateChatFunction extends GenericChatFunction {

	public UpdateChatFunction() {
		super(StatusCode.OK);
	}

	@Override
	protected String doAction(APIGatewayProxyRequestEvent requestEvent) {
		try {
			String name = requestEvent.getPathParameters().get(CHAT_KEY);
			getDynamoDBService().updateChat(name, toChat(requestEvent.getBody()));
			return getJsonResponse("Chat updated: " + name);
		} catch (DynamoDBException e) {
			return getJsonResponse(e.getMessage());
		}
	}

}
