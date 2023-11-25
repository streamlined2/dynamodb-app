package handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

public class UpdateUserFunction extends GenericFunction {

	protected UpdateUserFunction() {
		super(StatusCode.OK);
	}

	@Override
	public String doAction(APIGatewayProxyRequestEvent requestEvent) {
		return getDynamoDBService().updateUser(requestEvent.getPathParameters(), requestEvent.getBody());
	}
}
