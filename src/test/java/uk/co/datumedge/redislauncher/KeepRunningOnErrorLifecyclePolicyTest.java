package uk.co.datumedge.redislauncher;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

public class KeepRunningOnErrorLifecyclePolicyTest {
	private static final RedisServer IGNORED_REDIS_SERVER = null;
	private final KeepRunningOnErrorLifecyclePolicy lifecyclePolicy = new KeepRunningOnErrorLifecyclePolicy();

	@Test
	public void doesNothingWhenRedisServerFailedToStart() {
		lifecyclePolicy.failedToStart(IGNORED_REDIS_SERVER);
	}

	@Test
	public void throwsIOExceptionWhenRedisServerFailedToStop() throws IOException {
		Throwable cause = new RuntimeException();
		try {
			lifecyclePolicy.failedToStop(IGNORED_REDIS_SERVER, cause);
			fail("Expected IOException");
		} catch (IOException e) {
			assertThat(e.getCause(), is(sameInstance(cause)));
		}
	}
}
