package uk.co.datumedge.redislauncher;

/**
 * Properties that govern how a {@code LocalRedisServer} connects to a {@code redis-server} process.
 *
 * @see LocalRedisServer
 */
public final class ConnectionProperties {
	/**
	 * Default connection properties.  The defaults are:
	 * <ul>
	 * <li>5 maximum connection attempts</li>
	 * <li>5 maximum readiness attempts</li>
	 * <li>a shutdown timeout of 10000 milliseconds</li>
	 * </ul>
	 */
	public static final ConnectionProperties DEFAULT = new Builder().build();

	/**
	 * The maximum number of attempts at connecting to the server.
	 */
	public final int maximumConnectionAttempts;

	/**
	 * The maximum number of attempts at checking that the server is ready to accept requests.
	 */
	public final int maximumReadinessAttempts;

	/**
	 * The maximum time to wait for the server process to exit after requesting shutdown.
	 */
	public final long shutdownTimeoutMillis;

	private ConnectionProperties(int maximumConnectionAttempts, int maximumReadinessAttempts, long shutdownTimeoutMillis) {
		this.maximumConnectionAttempts = maximumConnectionAttempts;
		this.maximumReadinessAttempts = maximumReadinessAttempts;
		this.shutdownTimeoutMillis = shutdownTimeoutMillis;
	}

	/**
	 * A builder of {@code ConnectionProperties}.
	 */
	public static final class Builder {
		private static final int DEFAULT_MAXIMUM_CONNECTION_ATTEMPTS = 5;
		private static final int DEFAULT_MAXIMUM_READINESS_ATTEMPTS = 5;
		private static final long DEFAULT_SHUTDOWN_TIMEOUT_MILLIS = 10000;

		private int maximumConnectionAttempts = DEFAULT_MAXIMUM_CONNECTION_ATTEMPTS;
		private int maximumReadinessAttempts = DEFAULT_MAXIMUM_READINESS_ATTEMPTS;
		private long shutdownTimeoutMillis = DEFAULT_SHUTDOWN_TIMEOUT_MILLIS;

		/**
		 * Sets the maximum number of connection attempts on the builder.
		 *
		 * @return the updated builder
		 */
		public Builder withMaximumConnectionAttempts(int maximumConnectionAttempts) {
			this.maximumConnectionAttempts = maximumConnectionAttempts;
			return this;
		}

		/**
		 * Sets the maximum number readiness attempts on the builder.
		 *
		 * @return the updated builder
		 */
		public Builder withMaximumReadinessAttempts(int maximumReadinessAttempts) {
			this.maximumReadinessAttempts  = maximumReadinessAttempts;
			return this;
		}

		/**
		 * Sets the maximum time to wait after requesting shutdown on the builder.
		 *
		 * @return the updated builder
		 */
		public Builder withShutdownTimeoutMillis(long shutdownTimeoutMillis) {
			this.shutdownTimeoutMillis = shutdownTimeoutMillis;
			return this;
		}

		/**
		 * Creates a {@code ConnectionProperties} instance from the current builder.
		 *
		 * @return a {@code ConnectionProperties} instance
		 */
		public ConnectionProperties build() {
			return new ConnectionProperties(maximumConnectionAttempts, maximumReadinessAttempts, shutdownTimeoutMillis);
		}
	}
}
