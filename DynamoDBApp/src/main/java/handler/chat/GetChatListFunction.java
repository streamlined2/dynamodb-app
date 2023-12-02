package handler.chat;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import handler.StatusCode;
import layer.model.chat.Chat;

public class GetChatListFunction extends GenericChatFunction {

	public GetChatListFunction(StatusCode successCode) {
		super(StatusCode.OK);
	}

	@Override
	protected String doAction(APIGatewayProxyRequestEvent requestEvent) {
		Map<String, String> queryParameters = requestEvent.getQueryStringParameters();
		List<Chat> chatList = getDynamoDBService().getChatList(extractHashKey(queryParameters),
				extractLimit(queryParameters));
		return getGson().toJson(chatList);
	}

}
