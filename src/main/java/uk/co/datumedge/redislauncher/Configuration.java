package uk.co.datumedge.redislauncher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.exec.CommandLine;

/**
 * Configuration for a redis server.
 */
public final class Configuration {
	/**
	 * A system property key to specify the path to a {@code redis-server} executable.
	 */
	public static final String COMMAND_PROPERTY = "redislauncher.command";

	private final boolean programmatic;
	private final CommandLine commandLine;

	/**
	 * The port on which the redis server accepts connections.
	 */
	public final int port;

	private Configuration(boolean programmatic, CommandLine commandLine, int port) {
		this.programmatic = programmatic;
		this.commandLine = commandLine;
		this.port = port;
	}

	/**
	 * Creates a {@code Configuration} builder which has the redis server take its configuration from a
	 * {@code redis.conf} file specified on the {@code CommandLine}. If {@code redis-server} is configured to use a
	 * non-default port, the configuration builder must be configured with the same port. If no {@code redis.conf} file
	 * is specified on the {@code CommandLine}, {@code redis-server} will use the default configuration.
	 *
	 * <h6>Examples</h6>
	 * Create a configuration with no explicit configuration file which accepts connections on the default port 6379:
	 *
	 * <pre>
	 * Configuration configuration = staticConfiguration()
	 * 	.withCommandLine(new CommandLine(&quot;/path/to/redis-server&quot;))
	 * 	.build();
	 * </pre>
	 *
	 * Create a configuration with a {@code redis.conf} file using a non-default port 6380:
	 * <pre>
	 * Configuration configuration = staticConfiguration()
	 * 		.withCommandLine(new CommandLine(&quot;/path/to/redis-server&quot;).addArgument(&quot;/path/to/redis.conf&quot;))
	 * 		.withPort(6380)
	 * 		.build();
	 * </pre>
	 *
	 * @return a {@code Configuration} builder instance
	 */
	public static Builder staticConfiguration() {
		return new Builder();
	}

	/**
	 * Creates a {@code Configuration} built programmatically. When the {@code redis-server} executable is started, this
	 * configuration is passed to the process over {@code stdin}. No {@code redis.conf} file must be passed as a
	 * {@code CommandLine} argument when using programmatic configuration.
	 *
	 * @return a {@code Configuration} builder instance
	 */
	public static Builder programmaticConfiguration() {
		return new Builder().isProgrammatic();
	}

	static Configuration defaultConfiguration() {
		return new Builder().build();
	}

	CommandLine commandLine() throws IOException {
		CommandLine commandLine = new CommandLine(this.commandLine);
		if (programmatic) commandLine.addArgument("-");
		return commandLine;
	}

	InputStream inputStream() {
		if (programmatic) {
			// TODO: don't hardwire these configuration parameters here
			return new ByteArrayInputStream(("port " + port + "\nsave 60 10000\ndbfilename dump.rdb\ndir ./").getBytes(Charset.forName("UTF-8")));
		} else {
			return null;
		}
	}

	/**
	 * A builder of {@code Configuration} instances.
	 */
	public static final class Builder {
		private static final int DEFAULT_PORT = 6379;
		private CommandLine commandLine;
		private int port = DEFAULT_PORT;
		private boolean programmatic;

		private Builder() { }

		Builder isProgrammatic() {
			this.programmatic = true;
			return this;
		}

		/**
		 * Sets the {@code CommandLine} used to launch the redis server process.
		 *
		 * @return the updated builder
		 */
		public Builder withCommandLine(CommandLine commandLine) {
			this.commandLine = commandLine;
			return this;
		}

		/**
		 * Sets the port on which the redis server accepts connections.
		 *
		 * @return the updated builder
		 */
		public Builder withPort(int port) {
			this.port = port;
			return this;
		}

		/**
		 * Creates a {@code Configuration} instance from the current builder. If no {@code CommandLine} was
		 * specified, the {@code redislauncher.command} system property is used. If no port was specified, the default
		 * port 6379 is used.
		 *
		 * @return a {@code Configuration} instance
		 * @throws NullPointerException
		 *             if no {@code CommandLine} was specified and the {@code redislauncher.command} system
		 *             property does not exist
		 */
		public Configuration build() {
			if (commandLine == null) commandLine = defaultCommandLine();
			return new Configuration(programmatic, commandLine, port);
		}

		private CommandLine defaultCommandLine() {
			String command = System.getProperty(COMMAND_PROPERTY);
			if (command == null) {
				throw new NullPointerException(COMMAND_PROPERTY +
						" system property must be a path to a redis-server executable");
			}
			return new CommandLine(command);
		}
	}
}
