package handler.user;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;
import layer.model.user.UserDto;

public class GetUserListFunction extends GenericUserFunction {

	public GetUserListFunction() {
		super(StatusCode.OK);
	}

	@Override
	public String doAction(APIGatewayProxyRequestEvent requestEvent) {
		Map<String, String> queryParameters = requestEvent.getQueryStringParameters();
		List<UserDto> userDtoList = getDynamoDBService().getUserList(toListParameters(queryParameters));
		return getGson().toJson(userDtoList);
	}

}
