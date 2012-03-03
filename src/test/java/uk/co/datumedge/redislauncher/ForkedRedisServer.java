package uk.co.datumedge.redislauncher;

import static uk.co.datumedge.redislauncher.Configuration.defaultConfiguration;

import java.io.IOException;

public class ForkedRedisServer {
	public static void main(String[] args) {
		try {
			Execution execution = new Execution(defaultConfiguration());
			LocalRedisServer redisServer = new LocalRedisServer(execution, ConnectionProperties.DEFAULT, new AlwaysDestroyLifecyclePolicy());
			redisServer.start();
			Thread.sleep(Long.MAX_VALUE);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
