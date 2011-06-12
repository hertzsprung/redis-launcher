package uk.co.datumedge.redislauncher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public final class RedisServer {
	private static final int DEFAULT_PORT = 6379;

	/**
	 * A system property key to specify the path to a {@code redis-server} executable.
	 */
	public static final String COMMAND_PROPERTY = "redislauncher.command";
	private static final int MAX_CONNECT_ATTEMPTS = 5;
	private static final long SLEEP_BETWEEN_CONNECT_RETRIES_MILLIS = 100;
	private static final int DEFAULT_MAX_READINESS_ATTEMPTS = 30;
	private static final long DEFAULT_SLEEP_BETWEEN_READINESS_RETRIES_MILLIS = 1000;

	private final int maximumReadinessAttempts;
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
	public static RedisServer newInstance() {
		String command = System.getProperty(RedisServer.COMMAND_PROPERTY);
		if (command == null) {
			throw new NullPointerException(RedisServer.COMMAND_PROPERTY +
					" system property must be a path to a redis-server executable");
		}
		return new RedisServer(new ProcessBuilder(command));
	}

	public RedisServer(ProcessBuilder processBuilder) {
		this(processBuilder, DEFAULT_MAX_READINESS_ATTEMPTS);
	}

	public RedisServer(ProcessBuilder processBuilder, int maximumReadinessAttempts) {
		this.processBuilder = processBuilder;
		this.maximumReadinessAttempts = maximumReadinessAttempts;
	}

	/**
	 *
	 * @throws IOException
	 *             if the server could not be started
	 * @throws InterruptedException
	 *             if interrupted while waiting to start
	 */
	public void start() throws IOException, InterruptedException {
		if (started) throw new IllegalStateException("Server has already been started");
		process = processBuilder.start();
		Socket socket = tryToConnect();
		started = true;
		try {
			waitForServerReadiness(socket);
		} finally {
			socket.close();
		}
	}

	private Socket tryToConnect() throws IOException, InterruptedException {
		for (int i = 0; i < MAX_CONNECT_ATTEMPTS; i++) {
			Socket socket = new Socket();
			try {
				socket.connect(new InetSocketAddress("localhost", DEFAULT_PORT));
				return socket;
			} catch (ConnectException e) {
				socket.close();
				TimeUnit.MILLISECONDS.sleep(SLEEP_BETWEEN_CONNECT_RETRIES_MILLIS);
			} catch (IOException e) {
				socket.close();
			}
		}
		throw new ConnectException("Couldn't connect after " + MAX_CONNECT_ATTEMPTS + " attempts");
	}

	private void waitForServerReadiness(Socket socket) throws IOException, InterruptedException {
		OutputStream os = socket.getOutputStream();
		InputStream is = socket.getInputStream();
		for (int i=0; i< maximumReadinessAttempts; i++) {
			os.write("*1\r\n$4\r\nPING\r\n".getBytes("UTF-8"));
			os.flush();
			String reply = readReply(is);
			if (reply.equals("+PONG")) return;
			TimeUnit.MILLISECONDS.sleep(DEFAULT_SLEEP_BETWEEN_READINESS_RETRIES_MILLIS);
		}
		throw new ServerNotReadyException("Server was not ready to accept requests after " + maximumReadinessAttempts + " attempts");
	}

	private String readReply(InputStream is) throws IOException {
		StringBuilder builder = new StringBuilder();
		boolean seenCarriageReturn = false, seenNewline = false;
		while (!seenCarriageReturn || !seenNewline) { // TODO: should we check for ordering of \r and \n?
			int i = is.read();
			if (i == -1) {
				return builder.toString();
			}

			char c = (char) i;
			switch (c) {
			case '\r':
				seenCarriageReturn = true;
				break;
			case '\n':
				seenNewline = true;
				break;
			default:
				builder.append(c);
			}
		}
		return builder.toString();
	}

	/**
	 *
	 * @throws IOException
	 *             if an error occurs when connecting to the server
	 * @throws InterruptedException
	 *             if interrupted while waiting to stop
	 */
	public void stop() throws IOException, InterruptedException {
		if (!started) return;
		Socket socket = new Socket();
		try {
			socket.connect(new InetSocketAddress("localhost", DEFAULT_PORT));
			OutputStream os = socket.getOutputStream();
			os.write("*1\r\n$8\r\nSHUTDOWN\r\n".getBytes("UTF-8"));
			os.flush();
		} finally {
			socket.close();
		}
		process.waitFor();
		started = false;
	}

	// TODO: need an option to process.destroy() if we can't start connect to the server, or timeout waiting for it to be ready
}
