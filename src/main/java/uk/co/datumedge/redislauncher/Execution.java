package uk.co.datumedge.redislauncher;

import static uk.co.datumedge.redislauncher.Configuration.defaultConfiguration;

import java.io.IOException;
import java.io.OutputStream;

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
	private ExecutionProcessDestroyer executionProcessDestroyer;
	final Configuration configuration;

	public static Builder anExecution() {
		return new Builder();
	}

	private Execution(Configuration configuration) {
		this.configuration = configuration;
	}

	DefaultExecuteResultHandler start(ProcessDestroyer lifecyleProcessDestroyer) throws IOException {
		Executor executor = new DefaultExecutor();
		DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
		this.executionProcessDestroyer = new ExecutionProcessDestroyer();
		executor.setProcessDestroyer(new CompositeProcessDestroyer(
				this.executionProcessDestroyer, lifecyleProcessDestroyer));
		executor.setStreamHandler(new PumpStreamHandler(
				IGNORED_OUTPUT_STREAM,
				IGNORED_ERROR_STREAM,
				configuration.inputStream()));
		executor.execute(configuration.commandLine(), handler);
		return handler;
	}

	void destroy() {
		if (executionProcessDestroyer != null) {
			executionProcessDestroyer.destroy();
		}
	}
	
	/**
	 * A builder of {@code Execution} instances.
	 */
	public static final class Builder {
		private Configuration configuration;

		private Builder() { }

		public Builder withConfiguration(Configuration configuration) {
			this.configuration = configuration;
			return this;
		}

		/**
		 * @return an {@code Execution} instance
		 * @throws NullPointerException
		 */
		public Execution build() {
			if (configuration == null) configuration = defaultConfiguration();
			return new Execution(configuration);
		}
	}
}
