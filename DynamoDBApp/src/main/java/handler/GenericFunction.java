package handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import layer.service.APIGatewayService;
import layer.service.APIGatewayServiceImpl;
import layer.service.DynamoDBService;
import layer.service.DynamoDBServiceImpl;

public abstract class GenericFunction
		implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private final StatusCode successCode;

	protected GenericFunction(StatusCode successCode) {
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

}
