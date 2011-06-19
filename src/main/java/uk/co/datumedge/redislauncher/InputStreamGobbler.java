package uk.co.datumedge.redislauncher;

import java.io.IOException;
import java.io.InputStream;

class InputStreamGobbler extends Thread {
	private static final int BUFFER_SIZE = 2048;
	private final InputStream inputStream;
	private final byte[] buffer = new byte[BUFFER_SIZE];

	/**
	 * Create an instance that will gobble from the specified {@code InputStream}.
	 *
	 * @param inputStream an {@code InputStream} instance
	 */
	public InputStreamGobbler(InputStream inputStream) {
		if (inputStream == null) throw new NullPointerException("inputStream is null");
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
