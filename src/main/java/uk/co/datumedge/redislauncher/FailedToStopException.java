package uk.co.datumedge.redislauncher;

import java.io.IOException;

/**
 * Checked exception thrown when a redis server could not be stopped.
 */
public final class FailedToStopException extends IOException {
	private static final long serialVersionUID = -4300622288855492173L;

	/**
	 * Constructs a {@code FailedToStopException} with the specified detail message and cause.
	 *
	 * @param message the detail message
	 * @param cause the cause.  A {@code null} value is permitted, and indicates that the cause is unknown.
	 */
	public FailedToStopException(String message, Throwable cause) {
		super(message, cause);
	}
}
