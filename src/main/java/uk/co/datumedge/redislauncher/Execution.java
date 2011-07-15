package uk.co.datumedge.redislauncher;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.ProcessDestroyer;
import org.apache.commons.exec.PumpStreamHandler;

class Execution {
	private static class ExecutionProcessDestroyer implements ProcessDestroyer {
		private final Collection<Process> processes = new HashSet<Process>();

		@Override
		public boolean add(Process process) {
			return processes.add(process);
		}

		@Override
		public boolean remove(Process process) {
			return processes.remove(process);
		}

		@Override
		public int size() {
			return processes.size();
		}

		public void destroy() {
			for (Process process : processes) {
				process.destroy();
			}
		}
	}

	private final Executor executor;
	private final CommandLine commandLine;
	private ExecutionProcessDestroyer processDestroyer;

	public Execution(CommandLine commandLine) {
		this(new DefaultExecutor(), commandLine);
	}

	public Execution(Executor executor, CommandLine commandLine) {
		this.executor = executor;
		this.commandLine = commandLine;
	}

	public DefaultExecuteResultHandler start() throws ExecuteException, IOException {
		DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
		executor.execute(commandLine, handler);
		processDestroyer = new ExecutionProcessDestroyer();
		executor.setProcessDestroyer(processDestroyer);
		executor.setStreamHandler(new PumpStreamHandler(null, null));
		return handler;
	}

	public void destroy() {
		if (processDestroyer != null) {
			processDestroyer.destroy();
		}
	}
}
