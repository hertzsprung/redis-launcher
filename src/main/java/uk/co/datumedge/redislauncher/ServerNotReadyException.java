package uk.co.datumedge.redislauncher;

import java.io.IOException;

public final class ServerNotReadyException extends IOException {
	public ServerNotReadyException(String message) {
		super(message);
	}
}
