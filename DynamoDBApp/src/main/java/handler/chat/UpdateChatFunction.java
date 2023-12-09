package handler.chat;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;

public class UpdateChatFunction extends GenericChatFunction {

	public UpdateChatFunction() {
		super(StatusCode.OK);
	}

	@Override
	protected String doAction(APIGatewayProxyRequestEvent requestEvent) {
		String name = requestEvent.getPathParameters().get(CHAT_KEY);
		getDynamoDBService().updateChat(name, toEntity(requestEvent.getBody()));
		return getJsonResponse("Chat updated: " + name);
	}

}
