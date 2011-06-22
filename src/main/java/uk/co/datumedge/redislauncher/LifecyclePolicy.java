package uk.co.datumedge.redislauncher;

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
}
