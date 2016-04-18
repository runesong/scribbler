package scribbler.content;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ResponseStatus(INTERNAL_SERVER_ERROR)
public class ResourceException extends RuntimeException
{
	public ResourceException() {}

	public ResourceException(String message) {
		super(message);
	}

	public ResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceException(Throwable cause) {
		super(cause);
	}
}
