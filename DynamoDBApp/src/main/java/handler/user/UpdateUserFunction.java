package handler.user;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;
import layer.service.DynamoDBException;

public class UpdateUserFunction extends GenericUserFunction {

	protected UpdateUserFunction() {
		super(StatusCode.OK);
	}

	@Override
	public String doAction(APIGatewayProxyRequestEvent requestEvent) {
		try {
			String email = requestEvent.getPathParameters().get(EMAIL_KEY);
			getDynamoDBService().updateUser(email, toUser(requestEvent.getBody()));
			return getJsonResponse("User updated: " + email);
		} catch (DynamoDBException e) {
			return getJsonResponse(e.getMessage());
		}
	}
}
