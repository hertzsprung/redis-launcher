package uk.co.datumedge.redislauncher;

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

	@Test(expected=IOException.class)
	public void throwsIOExceptionWhenRedisServerFailedToShutdown() throws IOException {
		lifecyclePolicy.failedToShutdown(IGNORED_REDIS_SERVER);
	}
}
