package uk.co.datumedge.redislauncher;

import java.io.IOException;

public interface RedisServer {
	/**
	 * Start a redis server and block until it is ready to accept requests. Calling this method does nothing if the
	 * server is already running.
	 *
	 * @throws IOException
	 *             if the server could not be started
	 * @throws InterruptedException
	 *             if interrupted while waiting to start
	 */
	void start() throws IOException, InterruptedException;

	/**
	 * Stop the redis server, blocking until it has terminated. Calling this method does nothing if the server is not
	 * running.
	 *
	 * @throws IOException
	 *             if an error occurs when connecting to the server
	 * @throws InterruptedException
	 *             if interrupted while waiting for the process to terminate
	 */
	void stop() throws IOException, InterruptedException;

	/**
	 * Forcibly terminate the redis server. Calling this method does nothing if the server is not running.
	 */
	void destroy();
}
