package uk.co.datumedge.redislauncher;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.ProcessDestroyer;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * An execution of a redis server.  Used by a {@link LocalRedisServer} to start and stop a redis server process.
 */
public final class Execution {
	private static final OutputStream IGNORED_OUTPUT_STREAM = null;
	private static final OutputStream IGNORED_ERROR_STREAM = null;
	private final CommandLine commandLine;
	private ExecutionProcessDestroyer executionProcessDestroyer;

	/**
	 * Constructs a new {@code Execution} using the {@code commandLine}.
	 *
	 * @param commandLine
	 *            a {@code CommandLine} instance.
	 */
	public Execution(CommandLine commandLine) {
		this.commandLine = commandLine;
	}

	DefaultExecuteResultHandler start(ProcessDestroyer lifecyleProcessDestroyer) throws IOException {
		Executor executor = new DefaultExecutor();
		DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
		this.executionProcessDestroyer = new ExecutionProcessDestroyer();
		executor.setProcessDestroyer(new CompositeProcessDestroyer(
				this.executionProcessDestroyer, lifecyleProcessDestroyer));
		executor.setStreamHandler(new PumpStreamHandler(IGNORED_OUTPUT_STREAM, IGNORED_ERROR_STREAM));
		executor.execute(commandLine, handler);
		return handler;
	}

	void destroy() {
		if (executionProcessDestroyer != null) {
			executionProcessDestroyer.destroy();
		}
	}
}
