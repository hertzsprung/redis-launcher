package uk.co.datumedge.redislauncher;

import java.io.IOException;
import java.io.InputStream;

final class Reply {
	private final InputStream input;

	public Reply(InputStream input) {
		this.input = input;
	}

	public String parse() throws IOException {
		StringBuilder builder = new StringBuilder();
		boolean seenCarriageReturn = false, seenNewline = false;
		while (!seenCarriageReturn || !seenNewline) { // TODO: should we check for ordering of \r and \n?
			int i = input.read();
			if (i == -1) {
				return builder.toString();
			}

			char c = (char) i;
			switch (c) {
			case '\r':
				seenCarriageReturn = true;
				break;
			case '\n':
				seenNewline = true;
				break;
			default:
				builder.append(c);
			}
		}
		return builder.toString();
	}
}
