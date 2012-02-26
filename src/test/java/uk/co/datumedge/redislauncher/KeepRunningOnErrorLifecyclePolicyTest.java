package uk.co.datumedge.redislauncher;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

public class KeepRunningOnErrorLifecyclePolicyTest {
	private static final RedisServer IGNORED_REDIS_SERVER = null;
	private final LifecyclePolicy lifecyclePolicy = new KeepRunningOnErrorLifecyclePolicy();

	@Test
	public void doesNothingWhenRedisServerFailedToStart() {
		lifecyclePolicy.failedToStart(IGNORED_REDIS_SERVER);
	}

	@Test(expected=FailedToStopException.class)
	public void throwsIOExceptionWhenRedisServerFailedToStop() throws IOException {
		lifecyclePolicy.failedToStop(IGNORED_REDIS_SERVER);
	}

	@Test
	public void providesNullProcessDestroyer() {
		assertThat(lifecyclePolicy.getProcessDestroyer(), is(instanceOf(NullProcessDestroyer.class)));
	}
}
