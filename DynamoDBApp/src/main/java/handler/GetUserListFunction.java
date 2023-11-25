package handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

public class GetUserListFunction extends GenericFunction {

	public GetUserListFunction() {
		super(StatusCode.OK);
	}

	@Override
	public String doAction(APIGatewayProxyRequestEvent requestEvent) {
		return dynamoDBService.getUsersListResponse(requestEvent.getQueryStringParameters());
	}

}
