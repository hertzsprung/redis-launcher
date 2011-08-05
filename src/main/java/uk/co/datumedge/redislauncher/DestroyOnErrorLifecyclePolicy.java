package uk.co.datumedge.redislauncher;

public class DestroyOnErrorLifecyclePolicy implements LifecyclePolicy {
	@Override
	public void failedToStart(RedisServer redisServer) {
		redisServer.destroy();
	}

	@Override
	public void failedToStop(RedisServer redisServer) {
		redisServer.destroy();
	}
}
