package uk.co.datumedge.redislauncher;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

public class BlockingInputStream extends InputStream {
	private final InputStream inputStream;
	private final CountDownLatch latch = new CountDownLatch(1);

	public BlockingInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public int read() throws IOException {
		int i = inputStream.read();
		if (i == -1) {
			boolean latchReleased = false;
			do {
				try {
					latch.await();
					latchReleased = true;
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
			} while (latchReleased == false);
			throw new IOException("Stream closed");
		}

		return i;
	}

	@Override
	public void close() throws IOException {
		latch.countDown();
	}
}
