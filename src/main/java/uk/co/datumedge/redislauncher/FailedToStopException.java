package uk.co.datumedge.redislauncher;

import java.io.IOException;

/**
 * Checked exception thrown when a redis server could not be stopped.
 */
public final class FailedToStopException extends IOException {
	private static final long serialVersionUID = -4300622288855492173L;

	public FailedToStopException(String message) {
		super(message);
	}
}
