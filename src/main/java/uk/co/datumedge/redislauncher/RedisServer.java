package uk.co.datumedge.redislauncher;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class RedisServer {
	public static final String COMMAND_PROPERTY = "redislauncher.command";
	private static final int MAX_CONNECT_ATTEMPTS = 5;
	private static final long SLEEP_BETWEEN_RETRIES_MILLIS = 100;
	private final ProcessBuilder processBuilder;
	private Process process;
	private boolean started;
	
	public RedisServer(ProcessBuilder processBuilder) {
		this.processBuilder = processBuilder;
	}

	/**
	 * 
	 * @throws ConnectException if the server could not be started
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void start() throws IOException, InterruptedException {
		if (started) throw new IllegalStateException("Server has already been started");
		process = processBuilder.start();
		tryToConnect();
		started = true;
	}

	private void tryToConnect() throws IOException, InterruptedException {
		for (int i = 0; i < MAX_CONNECT_ATTEMPTS; i++) {
			Socket socket = new Socket();
			try {
				socket.connect(new InetSocketAddress("localhost", 6379));
				OutputStream os = socket.getOutputStream();
				os.write("*1\r\n$4\r\nPING\r\n".getBytes("UTF-8"));
				os.flush();
				// TODO: should receive +PONG
				return;
			} catch (ConnectException e) {
				TimeUnit.MILLISECONDS.sleep(SLEEP_BETWEEN_RETRIES_MILLIS);
			} finally {
				socket.close();
			}
		}
		throw new ConnectException("Couldn't connect after " + MAX_CONNECT_ATTEMPTS + " attempts");
	}

	/**
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws IllegalStateException if the server has not been started, or has already been stopped
	 */
	public void stop() throws IOException, InterruptedException {
		if (!started) throw new IllegalStateException("Server has not been started, or has already been stopped");
		Socket socket = new Socket();
		try {
			socket.connect(new InetSocketAddress("localhost", 6379));
			OutputStream os = socket.getOutputStream();
			os.write("*1\r\n$8\r\nSHUTDOWN\r\n".getBytes("UTF-8"));
			os.flush();
		} finally {
			socket.close();
		}
		process.waitFor();
	}
}
