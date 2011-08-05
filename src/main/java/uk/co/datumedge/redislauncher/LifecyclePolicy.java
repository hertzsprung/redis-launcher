package uk.co.datumedge.redislauncher;

import java.io.IOException;

public interface LifecyclePolicy {
	void failedToStart(RedisServer redisServer);
	void failedToStop(RedisServer redisServer) throws IOException;
}
