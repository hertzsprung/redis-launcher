package uk.co.datumedge.redislauncher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class InputStreamGobblerTest {
	@Test(timeout=100)
	public void gobblesStreamContent() throws InterruptedException, IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{0});
		BlockingInputStream blockingInputStream = new BlockingInputStream(inputStream);
		InputStreamGobbler inputStreamGobbler = new InputStreamGobbler(blockingInputStream);
		inputStreamGobbler.start();
		try {
			waitForStreamToBeGobbled(inputStream);
			blockingInputStream.close();
		} finally {
			inputStreamGobbler.join();
		}
	}

	private void waitForStreamToBeGobbled(ByteArrayInputStream inputStream) throws InterruptedException {
		while (inputStream.available() != 0) {
			TimeUnit.MILLISECONDS.sleep(10);
		}
	}
}
