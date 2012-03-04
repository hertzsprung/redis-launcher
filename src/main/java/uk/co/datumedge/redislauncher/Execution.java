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
	private ExecutionProcessDestroyer executionProcessDestroyer;
	private final OutputStream outputStream;
	private final OutputStream errorStream;
	final Configuration configuration;

	public static Builder anExecution() {
		return new Builder();
	}

	private Execution(Configuration configuration, OutputStream outputStream, OutputStream errorStream) {
		this.configuration = configuration;
		this.outputStream = outputStream;
		this.errorStream = errorStream;
	}

	DefaultExecuteResultHandler start(ProcessDestroyer lifecyleProcessDestroyer) throws IOException {
		Executor executor = new DefaultExecutor();
		DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
		this.executionProcessDestroyer = new ExecutionProcessDestroyer();
		executor.setProcessDestroyer(new CompositeProcessDestroyer(
				this.executionProcessDestroyer, lifecyleProcessDestroyer));
		executor.setStreamHandler(new PumpStreamHandler(
				outputStream,
				errorStream,
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
		private OutputStream outputStream;
		private OutputStream errorStream;

		private Builder() { }

		public Builder withConfiguration(Configuration configuration) {
			this.configuration = configuration;
			return this;
		}
		
		public Builder withOutputStream(OutputStream outputStream) {
			this.outputStream = outputStream;
			return this;
		}
		
		public Builder withErrorStream(OutputStream errorStream) {
			this.errorStream = errorStream;
			return this;
		}

		/**
		 * @return an {@code Execution} instance
		 * @throws NullPointerException
		 */
		public Execution build() {
			if (configuration == null) configuration = defaultConfiguration();
			return new Execution(configuration, outputStream, errorStream);
		}
	}
}
