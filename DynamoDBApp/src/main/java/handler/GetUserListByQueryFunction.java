package handler;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import layer.model.RequestBody;
import layer.model.User;

public class GetUserListByQueryFunction extends GenericUserFunction {

	public GetUserListByQueryFunction() {
		super(StatusCode.OK);
	}

	@Override
	public String doAction(APIGatewayProxyRequestEvent requestEvent) {
		Optional<RequestBody> parameters = extractRequestBodyParameters(requestEvent.getBody());
		Map<String, String> stringParameters = requestEvent.getQueryStringParameters();
		List<User> userList = getDynamoDBService().getUserListByQuery(extractRangeKey(stringParameters),
				extractHashKey(stringParameters), extractLimit(stringParameters), parameters);
		return getGson().toJson(userList);
	}

}
