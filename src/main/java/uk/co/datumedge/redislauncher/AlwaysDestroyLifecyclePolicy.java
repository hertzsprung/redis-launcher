package uk.co.datumedge.redislauncher;

import org.apache.commons.exec.ProcessDestroyer;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;

/**
 * A lifecycle policy that destroys the server when an exception occurs, or when the JVM terminates.
 */
public final class AlwaysDestroyLifecyclePolicy implements LifecyclePolicy {
	/**
	 * {@inheritDoc} This implementation destroys the {@code redisServer}.
	 */
	@Override
	public void failedToStart(RedisServer redisServer) {
		redisServer.destroy();
	}

	/**
	 * {@inheritDoc} This implementation destroys the {@code redisServer}.
	 */
	@Override
	public void failedToStop(RedisServer redisServer) {
		redisServer.destroy();
	}

	@Override
	public ProcessDestroyer getProcessDestroyer() {
		return new ShutdownHookProcessDestroyer();
	}
}
