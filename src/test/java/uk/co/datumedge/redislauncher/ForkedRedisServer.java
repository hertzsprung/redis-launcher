package uk.co.datumedge.redislauncher;

import static uk.co.datumedge.redislauncher.Execution.anExecution;

import java.io.IOException;

public final class ForkedRedisServer {
	public static void main(String[] args) {
		try {
			LocalRedisServer redisServer = new LocalRedisServer(
					anExecution().build(),
					ConnectionProperties.DEFAULT,
					new AlwaysDestroyLifecyclePolicy());
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
