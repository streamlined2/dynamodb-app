package handler.user;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;
import layer.model.user.User;
import layer.service.DynamoDBException;

public class CreateUserFunction extends GenericUserFunction {

	public CreateUserFunction() {
		super(StatusCode.CREATED);
	}

	@Override
	public String doAction(APIGatewayProxyRequestEvent requestEvent) {
		try {
			User user = toUser(requestEvent.getBody());
			getDynamoDBService().createUser(user);
			return getJsonResponse("User created: " + user.getEmail());
		} catch (DynamoDBException e) {
			return getJsonResponse(e.getMessage());
		}
	}

}
