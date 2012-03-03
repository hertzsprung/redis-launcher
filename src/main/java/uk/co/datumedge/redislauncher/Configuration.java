package uk.co.datumedge.redislauncher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.apache.commons.exec.CommandLine;

public final class Configuration {
	/**
	 * A system property key to specify the path to a {@code redis-server} executable.
	 */
	public static final String COMMAND_PROPERTY = "redislauncher.command";

	private final CommandLine commandLine;
	final int port;

	private Configuration(CommandLine commandLine, int port) {
		this.commandLine = commandLine;
		this.port = port;
	}

	public static Configuration defaultConfiguration() {
		return new Builder().build();
	}

	CommandLine commandLine() throws IOException {
		CommandLine commandLine = new CommandLine(this.commandLine);
		commandLine.addArgument(createFile().getAbsolutePath());
		return commandLine;
	}

	// FIXME: don't write to file, write to redis-server's stdin instead 
	File createFile() throws IOException {
		File tempFile = File.createTempFile("redislauncher", ".conf");
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), Charset.forName("UTF-8"));
		try {
			writer.write("port ");
			writer.write(Integer.toString(port));
			writer.write('\n');
			writer.write("save 60 10000\ndbfilename dump.rdb\ndir ./");
			return tempFile;
		} finally {
			writer.close();
		}
	}

	public static final class Builder {
		private CommandLine commandLine;
		private int port = 6379;

		public Builder withCommandLine(CommandLine commandLine) {
			this.commandLine = commandLine;
			return this;
		}

		public Builder withPort(int port) {
			this.port = port;
			return this;
		}
		
		public Configuration build() {
			if (commandLine == null) commandLine = defaultCommandLine();
			return new Configuration(commandLine, port);
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
