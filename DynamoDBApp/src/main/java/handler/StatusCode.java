package handler;

public enum StatusCode {

	OK(200), CREATED(201), SERVICE_UNAVAILABLE(503);

	private int code;

	StatusCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

}
