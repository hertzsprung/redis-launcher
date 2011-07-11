package uk.co.datumedge.redislauncher;

import java.io.IOException;

import org.junit.Test;

public class KeepRunningOnErrorLifecyclePolicyTest {
	private static final RedisServer IGNORED_REDIS_SERVER = null;
	private final KeepRunningOnErrorLifecyclePolicy lifecyclePolicy = new KeepRunningOnErrorLifecyclePolicy();

	@Test
	public void doesNothingWhenRedisServerFailedToStart() {
		lifecyclePolicy.failedToStart(IGNORED_REDIS_SERVER);
	}

	@Test(expected=IOException.class)
	public void throwsIOExceptionWhenRedisServerFailedToStop() throws IOException {
		lifecyclePolicy.failedToStop(IGNORED_REDIS_SERVER);
	}
}
