package uk.co.datumedge.redislauncher;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

public final class KeepRunningOnErrorLifecyclePolicyTest {
	private static final RedisServer IGNORED_REDIS_SERVER = null;
	private static final Throwable NONEXISTENT_CAUSE = null;
	private final LifecyclePolicy lifecyclePolicy = new KeepRunningOnErrorLifecyclePolicy();

	@Test
	public void doesNothingWhenRedisServerFailedToStart() {
		lifecyclePolicy.failedToStart(IGNORED_REDIS_SERVER);
	}

	@Test(expected=FailedToStopException.class)
	public void throwsFailedToStopExceptionWhenRedisServerFailedToStop() throws IOException {
		lifecyclePolicy.failedToStop(IGNORED_REDIS_SERVER, NONEXISTENT_CAUSE);
	}
	
	@Test
	public void throwsExceptionContainingCauseWhenRedisServerFailedToStop() throws IOException {
		Throwable cause = new IOException();
		try {
			lifecyclePolicy.failedToStop(IGNORED_REDIS_SERVER, cause);
			fail("Expected FailedToStopException");
		} catch (FailedToStopException e) {
			assertThat(e.getCause(), is(cause));
		}
	}

	@Test
	public void providesNullProcessDestroyer() {
		assertThat(lifecyclePolicy.getProcessDestroyer(), is(instanceOf(NullProcessDestroyer.class)));
	}
}
