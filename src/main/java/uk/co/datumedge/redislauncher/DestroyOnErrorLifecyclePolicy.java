package uk.co.datumedge.redislauncher;

import org.apache.commons.exec.ProcessDestroyer;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;

public class DestroyOnErrorLifecyclePolicy implements LifecyclePolicy {
	@Override
	public void failedToStart(RedisServer redisServer) {
		redisServer.destroy();
	}

	@Override
	public void failedToStop(RedisServer redisServer) {
		redisServer.destroy();
	}

	@Override
	public ProcessDestroyer getProcessDestroyer() {
		return new ShutdownHookProcessDestroyer();
	}
}
