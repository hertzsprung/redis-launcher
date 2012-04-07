package uk.co.datumedge.redislauncher;

import org.apache.commons.exec.CommandLine;

final class JavaCommandLine {
	public static CommandLine javaCommandLine() {
		return new CommandLine("java")
			.addArguments(new String[]{"-cp", System.getProperty("java.class.path")});
	}
}
