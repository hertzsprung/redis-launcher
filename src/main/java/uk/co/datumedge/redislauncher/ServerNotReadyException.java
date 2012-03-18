package uk.co.datumedge.redislauncher;

import java.io.IOException;

/**
 * Checked exception thrown when the server was not ready to serve requests during startup.
 */
public final class ServerNotReadyException extends IOException {
	private static final long serialVersionUID = -8014288142356052567L;

	/**
	 * Constructs a {@code ServerNotReadyException} with the specified detail message.
	 *
	 * @param message
	 *            the detail message
	 */
	public ServerNotReadyException(String message) {
		super(message);
	}
}
