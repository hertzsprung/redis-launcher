package uk.co.datumedge.redislauncher;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.co.datumedge.redislauncher.KeepRunningOnErrorLifecyclePolicy.DEFAULT_MAXIMUM_CONNECTION_ATTEMPTS;
import static uk.co.datumedge.redislauncher.KeepRunningOnErrorLifecyclePolicy.DEFAULT_SHUTDOWN_TIMEOUT_MILLIS;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

@RunWith(JMock.class)
public class RedisServerTest {
	private static final int TIMEOUT = 60000;
	private final Mockery context = new JUnit4Mockery();
	private final LifecyclePolicy mockLifecyclePolicy = context.mock(LifecyclePolicy.class);
	private final RedisServer server;
	private final ProcessBuilder processBuilder;

	public RedisServerTest() {
		String command = System.getProperty(RedisServer.COMMAND_PROPERTY);
		if (command == null) Assert.fail(RedisServer.COMMAND_PROPERTY + " system property must be a path to a redis-server executable");
		processBuilder = new ProcessBuilder(command);
		server = new RedisServer(processBuilder);
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
		RedisServer server = new RedisServer(new ProcessBuilder("nonexistent-executable"));
		try {
			server.start();
		} finally {
			server.stop();
		}
	}

	@Test(expected=ConnectException.class)
	public void throwsConnectExceptionIfFailedToConnect() throws InterruptedException, IOException {
		RedisServer server = new RedisServer(new ProcessBuilder("java"));

		try {
			server.start();
		} finally {
			try {
				server.stop();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	@Test
	public void callsLifecyclePolicyWhenFailedToConnect() throws IOException, InterruptedException {
		final RedisServer server = new RedisServer(new ProcessBuilder("java"), mockLifecyclePolicy);

		context.checking(new Expectations() {{
			allowing(mockLifecyclePolicy).getMaximumConnectionAttempts(); will(returnValue(1));
			oneOf(mockLifecyclePolicy).failedToConnect(with(sameInstance(server)));
		}});

		try {
			server.start();
		} catch (ConnectException e) {
			// expected
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
	public void throwsServerNotReadyExceptionWhenNotReadyToAcceptRequestsAfterTimeout() throws IOException, InterruptedException {
		populateServerWithLargeDataSet();

		RedisServer server = redisServerWithOnlyOneReadinessAttempt();
		try {
			server.start();
		} finally {
			waitForServerToAcceptRequests();
			server.stop();
		}
	}

	@Test(timeout=TIMEOUT)
	public void callsLifecyclePolicyWhenNotReadyToAcceptRequestsAfterTimeout() throws IOException, InterruptedException {
		populateServerWithLargeDataSet();

		final RedisServer server = new RedisServer(processBuilder, mockLifecyclePolicy);

		context.checking(new Expectations() {{
			allowing(mockLifecyclePolicy).getMaximumConnectionAttempts(); will(returnValue(DEFAULT_MAXIMUM_CONNECTION_ATTEMPTS));
			allowing(mockLifecyclePolicy).getMaximumReadinessAttempts(); will(returnValue(1));
			allowing(mockLifecyclePolicy).getShutdownTimeoutMillis(); will(returnValue(DEFAULT_SHUTDOWN_TIMEOUT_MILLIS));

			oneOf(mockLifecyclePolicy).serverNotReady(with(sameInstance(server)));
		}});

		try {
			server.start();
		} catch (ServerNotReadyException e) {
			// expected
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
			pingForFiveSeconds();
		}
	}

	private RedisServer redisServerWithOnlyOneReadinessAttempt() {
		return new RedisServer(
				processBuilder,
				new KeepRunningOnErrorLifecyclePolicy.Builder()
						.withMaximumReadinessAttempts(1)
						.build());
	}

	private void pingForFiveSeconds() throws InterruptedException {
		Jedis jedis = new Jedis("localhost");
		try {
			for (int i=0; i<50; i++) {
				try {
					jedis.ping();
				} catch (JedisDataException e) {
					// ignored
				}
				TimeUnit.MILLISECONDS.sleep(100);
			}
		} finally {
			jedis.disconnect();
		}
	}

	@Test(expected=InterruptedException.class, timeout=TIMEOUT)
	public void throwsInterruptedExceptionIfWaitingForProcessExitTimesOut() throws IOException, InterruptedException {
		populateServerWithLargeDataSet();

		RedisServer server = new RedisServer(
				processBuilder,
				new KeepRunningOnErrorLifecyclePolicy.Builder()
						.withShutdownTimeoutMillis(1)
						.build());
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
