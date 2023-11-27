package handler;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import layer.model.User;

public class GetUserListFunction extends GenericUserFunction {

	public GetUserListFunction() {
		super(StatusCode.OK);
	}

	@Override
	public String doAction(APIGatewayProxyRequestEvent requestEvent) {
		Map<String, String> stringParameters = requestEvent.getQueryStringParameters();
		List<User> userList = getDynamoDBService().getUserList(extractHashKey(stringParameters),
				extractLimit(stringParameters));
		return getGson().toJson(userList);
	}

}
