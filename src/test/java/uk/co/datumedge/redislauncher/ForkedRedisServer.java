package uk.co.datumedge.redislauncher;

import java.io.IOException;

import org.apache.commons.exec.CommandLine;

public class ForkedRedisServer {
	public static void main(String[] args) {
		try {
			Execution execution = new Execution(new CommandLine(System.getProperty(LocalRedisServer.COMMAND_PROPERTY)));
			LocalRedisServer redisServer = new LocalRedisServer(execution, ConnectionProperties.DEFAULT, new DestroyOnErrorLifecyclePolicy());
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
