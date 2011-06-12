package uk.co.datumedge.redislauncher;

import java.io.IOException;

public class ServerNotReadyException extends IOException {
	public ServerNotReadyException(String message) {
		super(message);
	}
}
