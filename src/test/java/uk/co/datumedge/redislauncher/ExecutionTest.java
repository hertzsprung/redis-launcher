package uk.co.datumedge.redislauncher;

import static org.hamcrest.MatcherAssert.assertThat;
import static uk.co.datumedge.redislauncher.Configuration.staticConfiguration;
import static uk.co.datumedge.redislauncher.Execution.anExecution;
import static uk.co.datumedge.redislauncher.JavaCommandLine.javaCommandLine;
import static uk.co.datumedge.redislauncher.Matchers.containsBytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.junit.Test;

public final class ExecutionTest {
	@Test(timeout=1000)
	public void writesProcessStdOutToOutputStream() throws IOException, InterruptedException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		Execution execution = anExecutionOf(StdOutPrinter.class).withOutputStream(outputStream).build();
		execute(execution);
		
		assertThat(outputStream, containsBytes());
	}
	
	@Test(timeout=1000)
	public void writesProcessStdErrToErrorStream() throws IOException, InterruptedException {
		ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
		
		Execution execution = anExecutionOf(StdErrorPrinter.class).withErrorStream(errorStream).build();
		execute(execution);
		
		assertThat(errorStream, containsBytes());
	}

	private Execution.Builder anExecutionOf(Class<?> mainClass) {
		return anExecution().withConfiguration(staticConfiguration()
				.withCommandLine(commandLineFor(mainClass)).build());
	}
	
	private CommandLine commandLineFor(Class<?> mainClass) {
		return javaCommandLine().addArgument(mainClass.getCanonicalName());
	}
	
	private void execute(Execution execution) throws InterruptedException, IOException {
		waitForResult(execution.start(NullProcessDestroyer.INSTANCE));
		execution.destroy();
	}

	private void waitForResult(DefaultExecuteResultHandler handler) throws InterruptedException {
		while (!handler.hasResult()) {
			if (Thread.interrupted()) throw new InterruptedException();
		}
	}
}
