package uk.co.datumedge.redislauncher;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.ConnectException;

import org.junit.Assert;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisServerTest {
	private final RedisServer server;

	public RedisServerTest() {
		String command = System.getProperty(RedisServer.COMMAND_PROPERTY);
		if (command == null) Assert.fail(RedisServer.COMMAND_PROPERTY + " system property must be a path to a redis-server executable");
		server = new RedisServer(new ProcessBuilder(command));
	}

	@Test
	public void canStartServerInstantiatedUsingSystemProperty() throws IOException, InterruptedException {
		RedisServer server = RedisServer.newInstance();
		try {
			server.start();
		} finally {
			server.stop();
		}
	}

	@Test(expected=NullPointerException.class)
	public void throwsNullPointerExceptionIfSystemPropertyIsAbsentWhenInstantiatingServer() {
		String command = System.getProperty(RedisServer.COMMAND_PROPERTY);
		try {
			System.clearProperty(RedisServer.COMMAND_PROPERTY);
			RedisServer.newInstance();
		} finally {
			System.setProperty(RedisServer.COMMAND_PROPERTY, command);
		}
	}

	@Test
	public void canBeConnectedToOnceStarted() throws IOException, InterruptedException {
		try {
			server.start();
			Jedis jedis = new Jedis("localhost");
			assertThat(jedis.ping(), is("PONG"));
		} finally {
			try {
				server.stop();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	@Test(expected=JedisConnectionException.class)
	public void cannotBeConnectedToOnceStopped() throws IOException, InterruptedException {
		server.start();
		server.stop();
		Jedis jedis = new Jedis("localhost");
		jedis.ping();
		jedis.shutdown();
	}

	@Test
	public void stopDoesNothingWhenServerNotStarted() throws IOException, InterruptedException {
		server.stop();
	}

	@Test(expected=IllegalStateException.class)
	public void startThrowsIllegalStateExceptionWhenServerAlreadyStarted() throws IOException, InterruptedException {
		try {
			server.start();
			server.start();
		} finally {
			server.stop();
		}
	}

	@Test(expected=IOException.class)
	public void throwsIOExceptionWhenStartingIfCommandDoesNotExist() throws IOException, InterruptedException {
		RedisServer redisServer = new RedisServer(new ProcessBuilder("nonexistent-executable"));
		try {
			redisServer.start();
		} finally {
			server.stop();
		}
	}

	@Test(expected=ConnectException.class)
	public void throwsConnectExceptionIfUnableToConnect() throws InterruptedException, IOException {
		RedisServer redisServer = new RedisServer(new ProcessBuilder("java"));
		try {
			redisServer.start();
		} finally {
			try {
				server.stop();
			} catch (IOException e) {
				// ignore
			}
		}
	}
}
