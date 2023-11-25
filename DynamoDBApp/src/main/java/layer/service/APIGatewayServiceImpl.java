package layer.service;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;

import java.util.Map;

public class APIGatewayServiceImpl implements APIGatewayService {

	@Override
	public APIGatewayProxyResponseEvent getApiGatewayProxyResponseEvent(String message, int statusCode) {
		Map<String, String> headers = ofEntries(
				entry("Content-Type", "application/json"),
				entry("X-Custom-Header", "application/json"), 
				entry("Access-Control-Allow-Origin", "*"),
				entry("Access-Control-Allow-Headers", "*"), 
				entry("Access-Control-Allow-Methods", "*"));
		return new APIGatewayProxyResponseEvent()
				.withStatusCode(statusCode)
				.withHeaders(headers)
				.withBody(message);
	}
}
