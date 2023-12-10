package handler.user;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;

public class UpdateUserFunction extends GenericUserFunction {

	protected UpdateUserFunction() {
		super(StatusCode.OK);
	}

	@Override
	public String doAction(APIGatewayProxyRequestEvent requestEvent) {
		String email = requestEvent.getPathParameters().get(EMAIL_KEY);
		getDynamoDBService().updateUser(email, toDto(requestEvent.getBody()));
		return getJsonResponse("User updated: " + email);
	}
}
