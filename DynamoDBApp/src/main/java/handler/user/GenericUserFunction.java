package handler.user;

import handler.GenericFunction;
import handler.StatusCode;
import layer.model.user.RequestBody;
import layer.model.user.User;
import layer.service.user.DynamoDBUserServiceImpl;
import layer.service.user.UserService;

public abstract class GenericUserFunction extends GenericFunction<User> {

	protected static final String EMAIL_KEY = "email";
	protected static final String USER_WITH_EMAIL_NOT_FOUND_MESSAGE = "User with email %s not found";

	protected GenericUserFunction(StatusCode successCode) {
		super(successCode);
	}

	protected UserService getDynamoDBService() {
		return DynamoDBServiceHelper.INSTANCE;
	}

	private static class DynamoDBServiceHelper {
		private static final UserService INSTANCE = new DynamoDBUserServiceImpl();
	}

	protected RequestBody extractRequestBodyParameters(String inputBody) {
		if (inputBody == null || inputBody.isBlank()) {
			return RequestBody.builder().build();
		}
		return getGson().fromJson(inputBody, RequestBody.class);
	}

}
