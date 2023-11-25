package handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

public class DeleteUserFunction extends GenericFunction {

	public DeleteUserFunction() {
		super(StatusCode.OK);
	}

	@Override
	public String doAction(APIGatewayProxyRequestEvent requestEvent) {
		return getDynamoDBService().deleteUser(requestEvent.getPathParameters());
	}

}
