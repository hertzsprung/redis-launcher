package uk.co.datumedge.redislauncher;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Test;

public class ReplyTest {
	@Test
	public void returnsReplyWithoutCarriageReturnAndNewLineCharacters() throws IOException {
		Reply reply = new Reply(inputStreamFor("+PONG\r\n"));
		Assert.assertThat(reply.parse(), is(equalTo("+PONG")));
	}

	@Test
	public void returnsStringWhenEndOfStreamIsReached() throws IOException {
		Reply reply = new Reply(inputStreamFor("FOO"));
		Assert.assertThat(reply.parse(), is(equalTo("FOO")));
	}

	private ByteArrayInputStream inputStreamFor(String string) {
		return new ByteArrayInputStream(string.getBytes(Charset.forName("UTF-8")));
	}
}
