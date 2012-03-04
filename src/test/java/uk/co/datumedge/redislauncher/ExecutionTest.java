package uk.co.datumedge.redislauncher;

import static org.hamcrest.MatcherAssert.assertThat;
import static uk.co.datumedge.redislauncher.Configuration.staticConfiguration;
import static uk.co.datumedge.redislauncher.Execution.anExecution;
import static uk.co.datumedge.redislauncher.Matchers.containsBytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.junit.Test;

public final class ExecutionTest {
	@Test(timeout=1000)
	public void writesProcessStdErrToErrorStream() throws IOException, InterruptedException {
		ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
		Execution execution = anExecution().withConfiguration(
				staticConfiguration()
				.withCommandLine(new CommandLine("java").addArgument("failing-argument")).build())
				.withErrorStream(errorStream)
				.build();
		waitForResult(execution.start(NullProcessDestroyer.INSTANCE));
		execution.destroy();
		assertThat(errorStream, containsBytes());
	}

	private void waitForResult(DefaultExecuteResultHandler handler) throws InterruptedException {
		while (!handler.hasResult()) {
			if (Thread.interrupted()) throw new InterruptedException();
		}
	}
}
