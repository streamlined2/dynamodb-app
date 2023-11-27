package handler;

import java.util.Optional;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import layer.model.User;

public class GetUserFunction extends GenericUserFunction {

	public GetUserFunction() {
		super(StatusCode.OK);
	}

	@Override
	public String doAction(APIGatewayProxyRequestEvent requestEvent) {
		String email = requestEvent.getPathParameters().get(EMAIL_KEY);
		Optional<User> user = getDynamoDBService().findUser(email);
		return user.map(this::toJson).orElse(getJsonResponse(String.format(USER_WITH_EMAIL_NOT_FOUND, email)));
	}

}
