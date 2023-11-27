package layer.service;

public class DynamoDBException extends RuntimeException {

	public DynamoDBException(String message) {
		super(message);
	}

	public DynamoDBException(String message, Throwable cause) {
		super(message, cause);
	}

}
