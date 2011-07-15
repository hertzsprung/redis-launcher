package uk.co.datumedge.redislauncher;

import java.io.IOException;

public interface RedisServer {
	void start() throws IOException, InterruptedException;
	void stop() throws IOException;
	void destroy();
}
