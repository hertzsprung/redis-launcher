package uk.co.datumedge.redislauncher;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

public class KeepRunningOnErrorLifecyclePolicyTest {
	private static final RedisServer IGNORED_REDIS_SERVER = null;
	private final KeepRunningOnErrorLifecyclePolicy lifecyclePolicy = new KeepRunningOnErrorLifecyclePolicy();

	@Test
	public void doesNothingWhenRedisServerFailedToConnect() {
		lifecyclePolicy.failedToConnect(IGNORED_REDIS_SERVER);
	}

	@Test
	public void doesNothingWhenRedisServerNotReady() {
		lifecyclePolicy.serverNotReady(IGNORED_REDIS_SERVER);
	}

	@Test
	public void throwsIOExceptionWhenRedisServerFailedToShutdown() throws IOException {
		Throwable cause = new RuntimeException();
		try {
			lifecyclePolicy.failedToShutdown(IGNORED_REDIS_SERVER, cause);
			fail("Expected IOException");
		} catch (IOException e) {
			assertThat(e.getCause(), is(equalTo(cause)));
		}
	}
}
