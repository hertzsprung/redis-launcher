package uk.co.datumedge.redislauncher;

import org.apache.commons.exec.ProcessDestroyer;

/**
 * A lifecycle policy that keeps the server running when an exception occurs.  The server will not be destroyed.
 */
public final class KeepRunningOnErrorLifecyclePolicy implements LifecyclePolicy {
	KeepRunningOnErrorLifecyclePolicy() {
	}

	@Override
	public void failedToStart(RedisServer redisServer) {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws FailedToStopException
	 *             always thrown when this method is invoked
	 */
	@Override
	public void failedToStop(RedisServer redisServer, Throwable cause) throws FailedToStopException {
		throw new FailedToStopException("Failed to stop redis server", cause);
	}

	@Override
	public ProcessDestroyer getProcessDestroyer() {
		return NullProcessDestroyer.INSTANCE;
	}
}
