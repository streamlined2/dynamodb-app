package handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

public class CreateUserFunction extends GenericFunction {

	public CreateUserFunction() {
		super(StatusCode.CREATED);
	}

	@Override
	public String doAction(APIGatewayProxyRequestEvent requestEvent) {
		return dynamoDBService.createUser(requestEvent.getBody());
	}

}
