package uk.co.datumedge.redislauncher;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

public class RedisServerTest {
	private static final int TIMEOUT = 60000;
	private final RedisServer server;
	private final ProcessBuilder processBuilder;

	public RedisServerTest() {
		String command = System.getProperty(RedisServer.COMMAND_PROPERTY);
		if (command == null) Assert.fail(RedisServer.COMMAND_PROPERTY + " system property must be a path to a redis-server executable");
		processBuilder = new ProcessBuilder(command);
		server = new RedisServer.Builder(processBuilder).build();
	}

	@Before
	public void setup() {
		checkServerIsStopped();
		deleteDatastore();
	}

	private void deleteDatastore() {
		new File("dump.rdb").delete();
	}

	private void checkServerIsStopped() {
		Jedis jedis = null;
		try {
			jedis = new Jedis("localhost");
			jedis.ping();
			fail("Server is running before test has started");
		} catch (JedisConnectionException e) {
			// expected
		} finally {
			if (jedis != null) jedis.disconnect();
		}
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
		try {
			jedis.ping();
			jedis.shutdown();
		} finally {
			jedis.disconnect();
		}
	}

	@Test
	public void stopDoesNothingWhenServerNotStarted() throws IOException, InterruptedException {
		server.stop();
	}

	@Test
	public void startDoesNothingWhenServerAlreadyStarted() throws IOException, InterruptedException {
		try {
			server.start();
			server.start();
		} finally {
			server.stop();
		}
	}

	@Test(expected=IOException.class)
	public void throwsIOExceptionWhenStartingIfCommandDoesNotExist() throws IOException, InterruptedException {
		RedisServer redisServer = new RedisServer.Builder(new ProcessBuilder("nonexistent-executable")).build();
		try {
			redisServer.start();
		} finally {
			server.stop();
		}
	}

	@Test(expected=ConnectException.class)
	public void throwsConnectExceptionIfUnableToConnect() throws InterruptedException, IOException {
		RedisServer redisServer = new RedisServer.Builder(new ProcessBuilder("java")).build();
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

	@Test
	public void canStartServerAgainAfterServerIsStopped() throws IOException, InterruptedException {
		try {
			server.start();
			server.stop();
			server.start();
		} finally {
			try {
				server.stop();
			} catch (IOException e) {
				// ignored
			}
		}
	}

	@Test(timeout=TIMEOUT)
	public void startBlocksUntilServerCanAcceptRequests() throws IOException, InterruptedException {
		populateServerWithLargeDataSet();

		Jedis jedis = null;
		try {
			server.start();
			jedis = new Jedis("localhost");
			assertThat(jedis.get("RedisServerTestKey0_0"), is(equalTo("value")));
		} finally {
			if (jedis != null) jedis.disconnect();
			server.stop();
		}
	}

	@Test(expected=ServerNotReadyException.class, timeout=TIMEOUT)
	public void throwsServerNotReadyExceptionIfNotReadyToAcceptRequestsBeforeTimeout() throws IOException, InterruptedException {
		populateServerWithLargeDataSet();

		RedisServer server = redisServerWithOnlyOneReadinessAttempt();
		try {
			server.start();
		} finally {
			waitForServerToAcceptRequests();
			server.stop();
		}
	}

	private void waitForServerToAcceptRequests() throws InterruptedException {
		Jedis jedis = new Jedis("localhost");
		try {
			while (true) {
				try {
					if ("PONG".equals(jedis.ping())) return;
					TimeUnit.MILLISECONDS.sleep(1000);
				} catch (JedisDataException e) {
					// ignore
				}
			}
		} finally {
			jedis.disconnect();
		}
	}

	@Test(expected=JedisConnectionException.class)
	public void canBeDestroyedWhenServerIsStartedButNotReadyToAcceptRequests() throws IOException, InterruptedException {
		populateServerWithLargeDataSet();

		RedisServer server = redisServerWithOnlyOneReadinessAttempt();
		try {
			server.start();
			fail("Did not thrown ServerNotReadyException");
		} catch (ServerNotReadyException e) {
			server.destroy();
			pingForOneSecond();
		}
	}

	private RedisServer redisServerWithOnlyOneReadinessAttempt() {
		RedisServer server = new RedisServer.Builder(processBuilder)
				.withLifecyclePolicy(new KeepRunningOnErrorLifecyclePolicy(1, 10000))
				.build();
		return server;
	}

	private void pingForOneSecond() throws InterruptedException {
		Jedis jedis = new Jedis("localhost");
		try {
			for (int i=0; i<10; i++) {
				jedis.ping();
				TimeUnit.MILLISECONDS.sleep(100);
			}
		} finally {
			jedis.disconnect();
		}
	}

	@Test(expected=InterruptedException.class)
	public void throwsInterruptedExceptionIfWaitingForProcessExitTimesOut() throws IOException, InterruptedException {
		populateServerWithLargeDataSet();

		RedisServer server = new RedisServer.Builder(processBuilder)
				.withLifecyclePolicy(new KeepRunningOnErrorLifecyclePolicy(5, 1))
				.build();
		try {
			server.start();
		} finally {
			try {
				server.stop();
			} finally {
				server.destroy();
			}
		}
	}

	private void populateServerWithLargeDataSet() throws IOException, InterruptedException {
		Jedis jedis = null;
		try {
			server.start();
			jedis = new Jedis("localhost");
			for (int i=0; i<100; i++) {
				Pipeline pipeline = jedis.pipelined();
				for (int j=0; j<1000; j++) {
					pipeline.set("RedisServerTestKey" + i + "_" + j, "value");
					pipeline.exec();
				}
			}
		} finally {
			if (jedis != null) jedis.disconnect();
			server.stop();
		}
	}
}
