package uk.co.datumedge.redislauncher;

import static org.hamcrest.MatcherAssert.assertThat;
import static uk.co.datumedge.redislauncher.Configuration.programmaticConfiguration;
import static uk.co.datumedge.redislauncher.Matchers.containsBytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class ConfigurationTest {
	@Test
	public void suppliesPropertyInProgrammaticConfigurationInputStream() throws IOException {
		Configuration configuration = programmaticConfiguration().withProperty("key", "value").build();
		assertThat(toByteArray(configuration.inputStream()), containsBytes("key value".getBytes(Charset.forName("UTF-8"))));
	}

	private byte[] toByteArray(InputStream inputStream) throws IOException {
		ByteArrayOutputStream actual = new ByteArrayOutputStream();
		IOUtils.copy(inputStream, actual);
		return actual.toByteArray();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void doesNotAllowPortPropertyKey() {
		programmaticConfiguration().withProperty("port", "1234");
	}
}
