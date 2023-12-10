package handler.user;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;
import layer.model.user.UserDto;

public class CreateUserFunction extends GenericUserFunction {

	public CreateUserFunction() {
		super(StatusCode.CREATED);
	}

	@Override
	public String doAction(APIGatewayProxyRequestEvent requestEvent) {
		UserDto userDto = toDto(requestEvent.getBody());
		getDynamoDBService().createUser(userDto);
		return getJsonResponse("User created: " + userDto.getEmail());
	}

}
