package uk.co.datumedge.redislauncher;

import java.io.IOException;
import java.io.InputStream;

class InputStreamGobbler extends Thread {
	private static final int BUFFER_SIZE = 2048;
	private final InputStream inputStream;
	private final byte[] buffer = new byte[BUFFER_SIZE];

	public InputStreamGobbler(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public void run() {
		try {
			while (true) {
				inputStream.read(buffer);
			}
		} catch (IOException e) {
			// thrown when stream is closed
		}
	}
}
