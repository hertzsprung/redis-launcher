package uk.co.datumedge.redislauncher;

import java.io.IOException;

public class DestroyOnErrorLifecyclePolicy implements LifecyclePolicy {
	@Override
	public int getMaximumConnectionAttempts() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMaximumReadinessAttempts() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public long getShutdownTimeoutMillis() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void failedToStart(RedisServer redisServer) {
		redisServer.destroy();
	}

	@Override
	public void failedToStop(RedisServer redisServer) throws IOException {
		redisServer.destroy();
	}
}
