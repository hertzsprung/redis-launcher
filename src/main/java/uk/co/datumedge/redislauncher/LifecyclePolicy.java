package uk.co.datumedge.redislauncher;

import org.apache.commons.exec.ProcessDestroyer;

/**
 * An interface for defining a policy of a redis server's lifecycle.
 */
public interface LifecyclePolicy {
	/**
	 * Invoked when the server failed to start, or it could not be determined that the server has started.
	 *
	 * @param redisServer
	 *            the server which failed to start
	 */
	void failedToStart(RedisServer redisServer);
	
	/**
	 * Invoked when the server failed to stop, or it could not be determined that the server has stopped.
	 *
	 * @param redisServer
	 *            the server which failed to start
	 * @throws FailedToStopException optionally thrown by the implementing class
	 */
	void failedToStop(RedisServer redisServer) throws FailedToStopException;

	/**
	 * Get a {@code ProcessDestroyer} that can be used to destroy a redis server outside of its normal lifecycle. This
	 * is useful for destroying a redis server process when the JVM terminates, for example. An implementation should
	 * return {@link NullProcessDestroyer#INSTANCE} if no special behaviour is needed.
	 *
	 * @return a {@code ProcessDestroyer} instance
	 */
	ProcessDestroyer getProcessDestroyer();
}
