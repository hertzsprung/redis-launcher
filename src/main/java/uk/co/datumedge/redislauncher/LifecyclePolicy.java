package uk.co.datumedge.redislauncher;

import java.io.IOException;

public interface LifecyclePolicy {
	/**
	 * The maximum number of attempts at checking that the server is ready to accept requests.
	 */
	//@Deprecated
	int getMaximumReadinessAttempts();

	/**
	 * The maximum time to wait for the server process to exit after requesting shutdown.
	 */
	//@Deprecated
	long getShutdownTimeoutMillis();
	void failedToStart(RedisServer redisServer);
	void failedToStop(RedisServer redisServer) throws IOException;
}
