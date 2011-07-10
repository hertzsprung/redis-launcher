package uk.co.datumedge.redislauncher;

import java.io.IOException;

public interface LifecyclePolicy {

	int getMaximumConnectionAttempts();

	/**
	 * The maximum number of attempts at checking that the server is ready to accept requests.
	 */
	int getMaximumReadinessAttempts();

	/**
	 * The maximum time to wait for the server process to exit after requesting shutdown.
	 */
	long getShutdownTimeoutMillis();
	void failedToConnect(RedisServer redisServer);
	void serverNotReady(RedisServer redisServer);
	void failedToShutdown(RedisServer redisServer) throws IOException;
}
