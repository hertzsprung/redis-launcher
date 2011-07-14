package uk.co.datumedge.redislauncher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
	private final ProcessBuilder processBuilder;
	private Process process;
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
		return new LocalRedisServer(new ProcessBuilder(command));
	}

	public LocalRedisServer(ProcessBuilder processBuilder) {
		this(processBuilder, DEFAULT_LIFECYCLE_POLICY);
	}

	public LocalRedisServer(ProcessBuilder processBuilder, LifecyclePolicy lifecyclePolicy) {
		this.processBuilder = processBuilder;
		this.lifecyclePolicy = lifecyclePolicy;
	}


	/**
	 * Start a redis server and block until it is ready to accept requests.
	 *
	 * @throws ConnectException
	 *             if the server process was started but no connection to it could be made
	 * @throws ServerNotReadyException
	 *             if the server process was started but did not respond positively to redis PING commands after a
	 *             certain time
	 * @throws IOException
	 *             if the server could not be started because the process could not be started
	 * @throws InterruptedException
	 *             if interrupted while waiting to start
	 */
	@Override
	public void start() throws IOException, InterruptedException {
		if (started) return;
		process = processBuilder.start();
		new InputStreamGobbler(process.getInputStream()).start();
		new InputStreamGobbler(process.getErrorStream()).start();
		Socket socket = tryToConnect();
		started = true;
		try {
			waitForServerReadiness(socket);
		} finally {
			socket.close();
		}
	}

	private Socket tryToConnect() throws IOException, InterruptedException {
		for (int i = 0; i < lifecyclePolicy.getMaximumConnectionAttempts(); i++) {
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

		lifecyclePolicy.failedToConnect(this);
		throw new ConnectException("Couldn't connect after " +
				lifecyclePolicy.getMaximumConnectionAttempts() + " attempts");
	}

	private void waitForServerReadiness(Socket socket) throws IOException, InterruptedException {
		OutputStream output = socket.getOutputStream();
		InputStream input = socket.getInputStream();

		for (int i = 0; i < lifecyclePolicy.getMaximumReadinessAttempts(); i++) {
			output.write(PING_COMMAND);
			output.flush();
			if (new Reply(input).parse().equals("+PONG")) return;
			TimeUnit.MILLISECONDS.sleep(DEFAULT_SLEEP_BETWEEN_READINESS_RETRIES_MILLIS);
		}

		lifecyclePolicy.serverNotReady(this);
		throw new ServerNotReadyException("Server was not ready to accept requests after " +
				lifecyclePolicy.getMaximumReadinessAttempts() + " attempts");
	}

	/**
	 *
	 * @throws IOException
	 *             if an error occurs when connecting to the server
	 */
	@Override
	public void stop() throws IOException {
		if (!started) return;
		Socket socket = new Socket();
		try {
			socket.connect(new InetSocketAddress("localhost", DEFAULT_PORT));
			OutputStream os = socket.getOutputStream();
			os.write("*1\r\n$8\r\nSHUTDOWN\r\n".getBytes("UTF-8"));
			os.flush();
		} catch (IOException e) {
			lifecyclePolicy.failedToShutdown(this, e);
		} finally {
			socket.close();
		}
		waitForProcessShutdown();
		process.destroy();
		started = false;
	}

	private void waitForProcessShutdown() throws IOException {
		ScheduledExecutorService timerPool = Executors.newScheduledThreadPool(1);
		try {
			ScheduledFuture<?> interrupter = timerPool.schedule(
					createInterrupterFor(Thread.currentThread()),
					lifecyclePolicy.getShutdownTimeoutMillis(),
					TimeUnit.MILLISECONDS);

			try {
				process.waitFor();
			} catch (InterruptedException e) {
				lifecyclePolicy.failedToShutdown(this, e);
			} finally {
				interrupter.cancel(false);
				Thread.interrupted();
			}
		} finally {
			timerPool.shutdown();
		}
	}

	private Runnable createInterrupterFor(final Thread threadToInterrupt) {
		return new Runnable() {
			@Override
			public void run() {
				threadToInterrupt.interrupt();
			}
		};
	}

	/**
	 * Forcibly terminate the redis server.
	 */
	public void destroy() {
		if (process != null) process.destroy();
	}
}
