package handler.user;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;
import layer.model.user.RequestBody;
import layer.model.user.User;

public class GetUserListByQueryFunction extends GenericUserFunction {

	public GetUserListByQueryFunction() {
		super(StatusCode.OK);
	}

	@Override
	public String doAction(APIGatewayProxyRequestEvent requestEvent) {
		Optional<RequestBody> parameters = extractRequestBodyParameters(requestEvent.getBody());
		Map<String, String> queryParameters = requestEvent.getQueryStringParameters();
		List<User> userList = getDynamoDBService().getUserListByQuery(extractRangeKey(queryParameters),
				extractHashKey(queryParameters), extractLimit(queryParameters), parameters);
		return getGson().toJson(userList);
	}

}
