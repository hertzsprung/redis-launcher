package uk.co.datumedge.redislauncher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;

/**
 * A redis server which runs in a separate process on the same host as the JVM.
 */
public final class LocalRedisServer implements RedisServer, LocalRedisServerMBean {
	private static final int DEFAULT_PORT = 6379;

	/**
	 * A system property key to specify the path to a {@code redis-server} executable.
	 */
	public static final String COMMAND_PROPERTY = "redislauncher.command";

	private static final long DEFAULT_SLEEP_BETWEEN_CONNECT_RETRIES_MILLIS = 100;
	private static final long DEFAULT_SLEEP_BETWEEN_READINESS_RETRIES_MILLIS = 1000;
	private static final byte[] PING_COMMAND = "*1\r\n$4\r\nPING\r\n".getBytes(Charset.forName("UTF-8"));
	private static final LifecyclePolicy DEFAULT_LIFECYCLE_POLICY = new KeepRunningOnErrorLifecyclePolicy();

	private final LifecyclePolicy lifecyclePolicy;
	private final ConnectionProperties connectionProperties;
	private final Execution execution;

	private DefaultExecuteResultHandler executionResultHandler;
	private boolean started;

	/**
	 * Creates a new server instance using the {@code redislauncher.command} system property. The property value must be
	 * a path to a {@code redis-server} executable.
	 *
	 * @return a redis server instance
	 * @throws NullPointerException
	 *             if the {@code redislauncher.command} system property does not exist
	 */
	public static LocalRedisServer newInstance() {
		String command = System.getProperty(LocalRedisServer.COMMAND_PROPERTY);
		if (command == null) {
			throw new NullPointerException(LocalRedisServer.COMMAND_PROPERTY +
					" system property must be a path to a redis-server executable");
		}
		return new LocalRedisServer(new Execution(new CommandLine(command)));
	}

	/**
	 * Constructs a new {@code LocalRedisServer} instance using the {@code execution}.
	 * {@linkplain ConnectionProperties#DEFAULT Default connection properties} are used, and the server
	 * {@linkplain KeepRunningOnErrorLifecyclePolicy keeps running on error}.
	 *
	 * @param execution
	 *            an {@code Execution} instance
	 */
	public LocalRedisServer(Execution execution) {
		this(execution, ConnectionProperties.DEFAULT, DEFAULT_LIFECYCLE_POLICY);
	}

	/**
	 * Constructs a new {@code LocalRedisServer} instance using the {@code execution}, {@code connectionProperties} and
	 * {@code lifecyclePolicy}.
	 *
	 * @param execution
	 *            an {@code Execution} instance
	 * @param connectionProperties
	 *            a {@code ConnectionProperties} instance
	 * @param lifecyclePolicy
	 *            a {@code LifecyclePolicy} instance
	 */
	public LocalRedisServer(Execution execution, ConnectionProperties connectionProperties,
			LifecyclePolicy lifecyclePolicy) {
		this.execution = execution;
		this.connectionProperties = connectionProperties;
		this.lifecyclePolicy = lifecyclePolicy;
	}

	/**
	 * {@inheritDoc}
	 *
	 * There are three steps to server startup:
	 * <ol>
	 * <li>Execute the redis-server process</li>
	 * <li>Connect to the server. Makes multiple connection attempts up to the maximum specified by
	 * {@link ConnectionProperties#maximumConnectionAttempts}, waiting
	 * 100 milliseconds between each attempt.</li>
	 * <li>Wait for the server to become ready. Sends multiple PING commands up to the maximum specified by
	 * {@link ConnectionProperties#maximumReadinessAttempts}</li>, waiting
	 * 1000 milliseconds between each attempt.
	 * </ol>
	 *
	 * @throws ConnectException
	 *             if the server process was started but no connection to it could be made
	 * @throws ServerNotReadyException
	 *             if the server process was started but did not respond positively to redis PING commands
	 * @throws IOException
	 *             if the server could not be started because the process could not be started
	 * @throws InterruptedException
	 *             if interrupted while waiting to start
	 */
	@Override
	public void start() throws IOException, InterruptedException {
		if (started) {
			return;
		}
		executionResultHandler = execution.start(lifecyclePolicy.getProcessDestroyer());
		Socket socket = tryToConnect();
		started = true;
		try {
			waitForServerReadiness(socket);
		} finally {
			socket.close();
		}
	}

	private Socket tryToConnect() throws IOException, InterruptedException {
		for (int i = 0; i < connectionProperties.maximumConnectionAttempts; i++) {
			Socket socket = new Socket();
			try {
				socket.connect(new InetSocketAddress("localhost", DEFAULT_PORT));
				return socket;
			} catch (ConnectException e) {
				socket.close();
				TimeUnit.MILLISECONDS.sleep(DEFAULT_SLEEP_BETWEEN_CONNECT_RETRIES_MILLIS);
			} catch (IOException e) {
				socket.close();
			}
		}

		lifecyclePolicy.failedToStart(this);
		throw new ConnectException("Couldn't connect after " +
				(connectionProperties.maximumConnectionAttempts - 1) * DEFAULT_SLEEP_BETWEEN_CONNECT_RETRIES_MILLIS
				+ " milliseconds");
	}

	private void waitForServerReadiness(Socket socket) throws IOException, InterruptedException {
		OutputStream output = socket.getOutputStream();
		InputStream input = socket.getInputStream();

		for (int i = 0; i < connectionProperties.maximumReadinessAttempts; i++) {
			output.write(PING_COMMAND);
			output.flush();
			if (new Reply(input).parse().equals("+PONG")) {
				return;
			}
			TimeUnit.MILLISECONDS.sleep(DEFAULT_SLEEP_BETWEEN_READINESS_RETRIES_MILLIS);
		}

		lifecyclePolicy.failedToStart(this);
		throw new ServerNotReadyException("Server was not ready to accept requests after " +
				(connectionProperties.maximumReadinessAttempts - 1) * DEFAULT_SLEEP_BETWEEN_READINESS_RETRIES_MILLIS
				+ " milliseconds");
	}

	@Override
	public void stop() throws IOException, InterruptedException {
		if (!started) {
			return;
		}
		Socket socket = new Socket();
		try {
			socket.connect(new InetSocketAddress("localhost", DEFAULT_PORT));
			OutputStream os = socket.getOutputStream();
			os.write("*1\r\n$8\r\nSHUTDOWN\r\n".getBytes("UTF-8"));
			os.flush();
		} catch (IOException e) {
			// TODO: don't lose the exception
			lifecyclePolicy.failedToStop(this);
		} finally {
			socket.close();
		}
		waitForProcessShutdown();
		execution.destroy();
		started = false;
	}

	private void waitForProcessShutdown() throws IOException, InterruptedException {
		try {
			executionResultHandler.waitFor(connectionProperties.shutdownTimeoutMillis);
			if (!executionResultHandler.hasResult()) {
				lifecyclePolicy.failedToStop(this);
			}
		} catch (InterruptedException e) {
			lifecyclePolicy.failedToStop(this);
			throw e;
		}
	}

	@Override
	public void destroy() {
		execution.destroy();
	}
}
