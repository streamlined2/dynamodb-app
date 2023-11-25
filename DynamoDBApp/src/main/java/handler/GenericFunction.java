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

	protected final DynamoDBService dynamoDBService;
	protected final APIGatewayService apiGatewayService;
	private final StatusCode successCode;

	protected GenericFunction(StatusCode successCode) {
		this.successCode = successCode;
		dynamoDBService = new DynamoDBServiceImpl();
		apiGatewayService = new APIGatewayServiceImpl();
	}

	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
		try {
			String message = doAction(requestEvent);
			return apiGatewayService.getApiGatewayProxyResponseEvent(message, successCode.getCode());
		} catch (RuntimeException e) {
			return apiGatewayService.getApiGatewayProxyResponseEvent(
					String.format("An error occurred while executing the lambda function: %s; message: %s",
							e.getClass().getName(), e.getMessage()),
					StatusCode.SERVICE_UNAVAILABLE.getCode());
		}
	}

	protected abstract String doAction(APIGatewayProxyRequestEvent requestEvent);

}
