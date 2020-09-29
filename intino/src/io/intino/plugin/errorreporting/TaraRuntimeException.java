package io.intino.plugin.errorreporting;

public class TaraRuntimeException extends RuntimeException {

	public TaraRuntimeException(String message) {
		super(message);
	}

	public TaraRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
