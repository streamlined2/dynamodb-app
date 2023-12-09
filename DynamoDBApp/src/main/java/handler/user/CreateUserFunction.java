package handler.user;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;
import layer.model.user.User;

public class CreateUserFunction extends GenericUserFunction {

	public CreateUserFunction() {
		super(StatusCode.CREATED);
	}

	@Override
	public String doAction(APIGatewayProxyRequestEvent requestEvent) {
		User user = toEntity(requestEvent.getBody());
		getDynamoDBService().createUser(user);
		return getJsonResponse("User created: " + user.getEmail());
	}

}
