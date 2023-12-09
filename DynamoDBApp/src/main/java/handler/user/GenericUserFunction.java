package handler.user;

import java.util.List;

import handler.GenericFunction;
import handler.StatusCode;
import layer.model.SortingOrder;
import layer.model.user.User;
import layer.model.user.UserData;
import layer.service.Utils;
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

	protected UserData toUserData(RequestBody requestBody) {
		return UserData.builder().name(requestBody.getName()).location(requestBody.getLocation())
				.minAge(Utils.getIntegerValue(requestBody.getAgeLimits().get(0)).orElse(null))
				.maxAge(Utils.getIntegerValue(requestBody.getAgeLimits().get(1)).orElse(null))
				.sorting(SortingOrder.getByLabel(requestBody.getSorting()).orElse(null)).build();
	}

	protected RequestBody toRequestBody(UserData userData) {
		return RequestBody.builder().name(userData.getName()).location(userData.getLocation())
				.sorting(userData.getSorting().getLabel())
				.ageLimits(List.of(userData.getMinAge().toString(), userData.getMaxAge().toString())).build();
	}

}
