package handler;

import java.util.Map;
import java.util.Optional;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;

import layer.model.ResponseMessage;
import layer.service.APIGatewayService;
import layer.service.APIGatewayServiceImpl;

public abstract class GenericFunction
		implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final String LIMIT_QUERY_PARAMETER = "limit";
	private static final String HASH_KEY_QUERY_PARAMETER = "hashkey";
	private static final String RANGE_KEY_QUERY_PARAMETER = "rangekey";

	private final StatusCode successCode;

	protected GenericFunction(StatusCode successCode) {
		this.successCode = successCode;
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

	protected Optional<String> extractLimit(Map<String, String> queryParameters) {
		return extractParameter(LIMIT_QUERY_PARAMETER, queryParameters);
	}

	protected Optional<String> extractHashKey(Map<String, String> queryParameters) {
		return extractParameter(HASH_KEY_QUERY_PARAMETER, queryParameters);
	}

	protected Optional<String> extractRangeKey(Map<String, String> queryParameters) {
		return extractParameter(RANGE_KEY_QUERY_PARAMETER, queryParameters);
	}

	private Optional<String> extractParameter(String parameterName, Map<String, String> queryParameters) {
		return Optional.ofNullable(queryParameters.getOrDefault(parameterName, null));
	}

	protected String getJsonResponse(String message) {
		return getGson().toJson(ResponseMessage.builder().message(message).build());
	}

}
