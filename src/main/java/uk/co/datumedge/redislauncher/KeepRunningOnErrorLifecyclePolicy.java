package uk.co.datumedge.redislauncher;

import java.io.IOException;

/**
 * A lifecycle policy that keeps the server running when an exception occurs.  The server will not be destroyed.
 */
public final class KeepRunningOnErrorLifecyclePolicy implements LifecyclePolicy {
	KeepRunningOnErrorLifecyclePolicy() {
	}

	@Override
	public void failedToStart(RedisServer redisServer) {
	}

	@Override
	public void failedToStop(RedisServer redisServer) throws IOException {
		throw new IOException("Failed to stop redis server");
	}
}
