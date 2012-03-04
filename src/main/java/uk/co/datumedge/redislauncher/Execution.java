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

	/**
	 * Creates an {@code Execution} builder.
	 *
	 * @return an {@code Execution} builder instance
	 */
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

		/**
		 * Sets the {@code Configuration} used by the builder.
		 *
		 * @return the updated builder
		 */
		public Builder withConfiguration(Configuration configuration) {
			this.configuration = configuration;
			return this;
		}

		/**
		 * Sets the {@code OutputStream} to which {@code stdout} is sent.
		 *
		 * @return the updated builder
		 */
		public Builder withOutputStream(OutputStream outputStream) {
			this.outputStream = outputStream;
			return this;
		}

		/**
		 * Sets the {@code OutputStream} to which {@code stderr} is sent.
		 *
		 * @return the updated builder
		 */
		public Builder withErrorStream(OutputStream errorStream) {
			this.errorStream = errorStream;
			return this;
		}

		/**
		 * Creates an {@code Execution} instance from the current builder. If no {@code Configuration} was specified,
		 * {@linkplain Configuration.Builder#build() default configuration} is used. If no {@code outputStream} was
		 * specified, {@code stdout} is swallowed. If no {@code errorStream} was specified, {@code stderr} is swallowed.
		 *
		 * @return an {@code Execution} instance
		 * @throws NullPointerException
		 *             if no {@code Configuration} was specified and the {@code redislauncher.command} system
		 *             property does not exist
		 */
		public Execution build() {
			if (configuration == null) configuration = defaultConfiguration();
			return new Execution(configuration, outputStream, errorStream);
		}
	}
}
