package handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

public class GetUserListByQueryFunction extends GenericFunction {

	public GetUserListByQueryFunction() {
		super(StatusCode.OK);
	}

	@Override
	public String doAction(APIGatewayProxyRequestEvent requestEvent) {
		return dynamoDBService.getUsersListByQueryResponse(requestEvent.getQueryStringParameters(),
				requestEvent.getBody());
	}

}
