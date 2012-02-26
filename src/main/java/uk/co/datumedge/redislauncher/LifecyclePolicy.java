package uk.co.datumedge.redislauncher;

import java.io.IOException;

import org.apache.commons.exec.ProcessDestroyer;

/**
 * An interface for defining a policy of a redis server's lifecycle.
 */
public interface LifecyclePolicy {
	void failedToStart(RedisServer redisServer);
	void failedToStop(RedisServer redisServer) throws IOException;

	/**
	 * Get a {@code ProcessDestroyer} that can be used to destroy a redis server outside of its normal lifecycle. This
	 * is useful for destroying a redis server process when the JVM terminates, for example. An implementation should
	 * return {@link NullProcessDestroyer#INSTANCE} if no special behaviour is needed.
	 *
	 * @return a {@code ProcessDestroyer} instance
	 */
	ProcessDestroyer getProcessDestroyer();
}
