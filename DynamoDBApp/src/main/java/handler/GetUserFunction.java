package handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

public class GetUserFunction extends GenericFunction {

	public GetUserFunction() {
		super(StatusCode.OK);
	}

	@Override
	public String doAction(APIGatewayProxyRequestEvent requestEvent) {
		return getDynamoDBService().findUser(requestEvent.getPathParameters());
	}

}
