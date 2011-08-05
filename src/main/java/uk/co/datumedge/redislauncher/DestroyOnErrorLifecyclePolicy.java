package uk.co.datumedge.redislauncher;


public class DestroyOnErrorLifecyclePolicy implements LifecyclePolicy {
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
	public void failedToStop(RedisServer redisServer) {
		redisServer.destroy();
	}
}
