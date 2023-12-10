package handler.user;

import java.util.Optional;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;
import layer.model.user.UserDto;

public class GetUserFunction extends GenericUserFunction {

	public GetUserFunction() {
		super(StatusCode.OK);
	}

	@Override
	public String doAction(APIGatewayProxyRequestEvent requestEvent) {
		String email = requestEvent.getPathParameters().get(EMAIL_KEY);
		Optional<UserDto> userDto = getDynamoDBService().findUser(email);
		return userDto.map(this::toJson)
				.orElse(getJsonResponse(String.format(USER_WITH_EMAIL_NOT_FOUND_MESSAGE, email)));
	}

}
