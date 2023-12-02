package handler.user;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;
import layer.service.DynamoDBException;

public class DeleteUserFunction extends GenericUserFunction {

	public DeleteUserFunction() {
		super(StatusCode.OK);
	}

	@Override
	public String doAction(APIGatewayProxyRequestEvent requestEvent) {
		try {
			String email = requestEvent.getPathParameters().get(EMAIL_KEY);
			getDynamoDBService().deleteUser(email);
			return getJsonResponse(String.format("User with email %s has been deleted", email));
		} catch (DynamoDBException e) {
			return getJsonResponse(e.getMessage());
		}
	}

}
