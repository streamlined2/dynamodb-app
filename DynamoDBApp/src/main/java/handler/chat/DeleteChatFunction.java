package handler.chat;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;
import layer.service.DynamoDBException;

public class DeleteChatFunction extends GenericChatFunction {

	public DeleteChatFunction() {
		super(StatusCode.OK);
	}

	@Override
	protected String doAction(APIGatewayProxyRequestEvent requestEvent) {
		try {
			String name = requestEvent.getPathParameters().get(CHAT_KEY);
			getDynamoDBService().deleteChat(name);
			return getJsonResponse(String.format("Chat with name %s has been deleted", name));
		} catch (DynamoDBException e) {
			return getJsonResponse(e.getMessage());
		}
	}

}
