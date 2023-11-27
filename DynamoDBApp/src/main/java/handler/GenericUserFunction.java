package handler;

import java.util.Map;
import java.util.Optional;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;

import layer.model.RequestBody;
import layer.model.ResponseMessage;
import layer.model.User;
import layer.service.APIGatewayService;
import layer.service.APIGatewayServiceImpl;
import layer.service.DynamoDBService;
import layer.service.DynamoDBServiceImpl;

public abstract class GenericUserFunction
		implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	protected static final String EMAIL_KEY = "email";
	protected static final String USER_WITH_EMAIL_NOT_FOUND = "User with email %s not found";
	private static final String COUNTRY_VALUE = "Ukraine";
	private static final String LIMIT_QUERY_PARAMETER = "limit";
	private static final String HASH_KEY_QUERY_PARAMETER = "hashkey";
	private static final String RANGE_KEY_QUERY_PARAMETER = "rangekey";

	private final StatusCode successCode;

	protected GenericUserFunction(StatusCode successCode) {
		this.successCode = successCode;
	}

	protected DynamoDBService getDynamoDBService() {
		return DynamoDBServiceHelper.INSTANCE;
	}

	private static class DynamoDBServiceHelper {
		private static final DynamoDBService INSTANCE = new DynamoDBServiceImpl();
	}

	protected APIGatewayService getAPIGatewayService() {
		return APIGatewayServiceHelper.INSTANCE;
	}

	private static class APIGatewayServiceHelper {
		private static final APIGatewayService INSTANCE = new APIGatewayServiceImpl();
	}

	protected Gson getGson() {
		return GsonHelper.INSTANCE;
	}

	private static class GsonHelper {
		private static final Gson INSTANCE = new Gson();
	}

	public final APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
		try {
			String message = doAction(requestEvent);
			return getAPIGatewayService().getApiGatewayProxyResponseEvent(message, successCode.getCode());
		} catch (RuntimeException e) {
			return getAPIGatewayService().getApiGatewayProxyResponseEvent(
					String.format("An error occurred while executing the lambda function: %s; message: %s",
							e.getClass().getName(), e.getMessage()),
					StatusCode.SERVICE_UNAVAILABLE.getCode());
		}
	}

	protected abstract String doAction(APIGatewayProxyRequestEvent requestEvent);

	protected User toUser(String inputBody) {
		User user = getGson().fromJson(inputBody, User.class);
		user.setCountry(COUNTRY_VALUE);
		return user;
	}

	protected String toJson(User user) {
		return getGson().toJson(user);
	}

	protected String getJsonResponse(String message) {
		return getGson().toJson(ResponseMessage.builder().message(message).build());
	}

	protected Optional<RequestBody> extractRequestBodyParameters(String inputBody) {
		if (inputBody == null || inputBody.isBlank()) {
			return Optional.empty();
		}
		return Optional.ofNullable(getGson().fromJson(inputBody, RequestBody.class));
	}

	protected String extractLimit(Map<String, String> queryParameters) {
		return queryParameters.getOrDefault(LIMIT_QUERY_PARAMETER, null);
	}

	protected String extractHashKey(Map<String, String> queryParameters) {
		return queryParameters.getOrDefault(HASH_KEY_QUERY_PARAMETER, null);
	}

	protected String extractRangeKey(Map<String, String> queryParameters) {
		return queryParameters.getOrDefault(RANGE_KEY_QUERY_PARAMETER, null);
	}

}
