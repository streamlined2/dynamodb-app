package handler.chat;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;
import layer.model.chat.ChatDto;

public class GetChatListFunction extends GenericChatFunction {

	public GetChatListFunction(StatusCode successCode) {
		super(StatusCode.OK);
	}

	@Override
	protected String doAction(APIGatewayProxyRequestEvent requestEvent) {
		Map<String, String> queryParameters = requestEvent.getQueryStringParameters();
		List<ChatDto> chatDtoList = getDynamoDBService().getChatList(toListParameters(queryParameters));
		return getGson().toJson(chatDtoList);
	}

}
