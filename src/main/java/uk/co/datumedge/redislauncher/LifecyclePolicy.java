package uk.co.datumedge.redislauncher;

import java.io.IOException;

import org.apache.commons.exec.ProcessDestroyer;

public interface LifecyclePolicy {
	void failedToStart(RedisServer redisServer);
	void failedToStop(RedisServer redisServer) throws IOException;
	ProcessDestroyer getProcessDestroyer();
}
