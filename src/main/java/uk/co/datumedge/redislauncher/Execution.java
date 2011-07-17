package uk.co.datumedge.redislauncher;

import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

public class Execution {
	private final CommandLine commandLine;
	private ExecutionProcessDestroyer processDestroyer;

	public Execution(CommandLine commandLine) {
		this.commandLine = commandLine;
	}

	DefaultExecuteResultHandler start() throws ExecuteException, IOException {
		DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
		Executor executor = new DefaultExecutor();
		executor.execute(commandLine, handler);
		processDestroyer = new ExecutionProcessDestroyer();
		executor.setProcessDestroyer(processDestroyer);
		executor.setStreamHandler(new PumpStreamHandler(null, null));
		return handler;
	}

	void destroy() {
		if (processDestroyer != null) {
			processDestroyer.destroy();
		}
	}
}
