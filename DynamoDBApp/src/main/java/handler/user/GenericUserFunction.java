package handler.user;

import java.util.Optional;

import handler.GenericFunction;
import handler.StatusCode;
import layer.model.user.RequestBody;
import layer.model.user.User;
import layer.service.user.DynamoDBUserServiceImpl;
import layer.service.user.UserService;

public abstract class GenericUserFunction extends GenericFunction {

	protected static final String EMAIL_KEY = "email";
	protected static final String USER_WITH_EMAIL_NOT_FOUND_MESSAGE = "User with email %s not found";
	private static final String COUNTRY_VALUE = "Ukraine";

	protected GenericUserFunction(StatusCode successCode) {
		super(successCode);
	}

	protected UserService getDynamoDBService() {
		return DynamoDBServiceHelper.INSTANCE;
	}

	private static class DynamoDBServiceHelper {
		private static final UserService INSTANCE = new DynamoDBUserServiceImpl();
	}

	protected User toUser(String inputBody) {
		User user = getGson().fromJson(inputBody, User.class);
		user.setCountry(COUNTRY_VALUE);
		return user;
	}

	protected String toJson(User user) {
		return getGson().toJson(user);
	}

	protected Optional<RequestBody> extractRequestBodyParameters(String inputBody) {
		if (inputBody == null || inputBody.isBlank()) {
			return Optional.empty();
		}
		return Optional.ofNullable(getGson().fromJson(inputBody, RequestBody.class));
	}

}
