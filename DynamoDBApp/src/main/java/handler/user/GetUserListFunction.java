package handler.user;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;
import layer.model.user.User;

public class GetUserListFunction extends GenericUserFunction {

	public GetUserListFunction() {
		super(StatusCode.OK);
	}

	@Override
	public String doAction(APIGatewayProxyRequestEvent requestEvent) {
		Map<String, String> queryParameters = requestEvent.getQueryStringParameters();
		List<User> userList = getDynamoDBService().getUserList(extractHashKey(queryParameters),
				extractLimit(queryParameters));
		return getGson().toJson(userList);
	}

}
