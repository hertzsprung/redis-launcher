package uk.co.datumedge.redislauncher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.exec.CommandLine;

final class ProgrammaticConfiguration extends Configuration {
	private final Map<String, String> properties;

	ProgrammaticConfiguration(CommandLine commandLine, int port, Map<String, String> properties) {
		super(commandLine, port);
		this.properties = properties;
	}

	@Override
	CommandLine commandLine() throws IOException {
		return super.commandLine().addArgument("-");
	}

	@Override
	InputStream inputStream() {
		StringBuilder builder = new StringBuilder("port " + port);
		for (Entry<String, String> entry : properties.entrySet()) {
			builder.append('\n').append(entry.getKey()).append(' ').append(entry.getValue());
		}
		return new ByteArrayInputStream(builder.toString().getBytes(Charset.forName("UTF-8")));
	}
}
