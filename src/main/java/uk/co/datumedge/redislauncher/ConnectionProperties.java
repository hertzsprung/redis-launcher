package uk.co.datumedge.redislauncher;


public final class ConnectionProperties {
	public static final ConnectionProperties DEFAULT = new Builder().build();
	private final int maximumConnectionAttempts;
	private final int maximumReadinessAttempts;
	private final long shutdownTimeoutMillis;

	private ConnectionProperties(int maximumConnectionAttempts, int maximumReadinessAttempts, long shutdownTimeoutMillis) {
		this.maximumConnectionAttempts = maximumConnectionAttempts;
		this.maximumReadinessAttempts = maximumReadinessAttempts;
		this.shutdownTimeoutMillis = shutdownTimeoutMillis;
	}

	public int getMaximumConnectionAttempts() {
		return maximumConnectionAttempts;
	}

	public int getMaximumReadinessAttempts() {
		return maximumReadinessAttempts;
	}

	public long getShutdownTimeoutMillis() {
		return shutdownTimeoutMillis;
	}

	public static final class Builder {
		private static final int DEFAULT_MAXIMUM_CONNECTION_ATTEMPTS = 5;
		private static final int DEFAULT_MAXIMUM_READINESS_ATTEMPTS = 5;
		private static final long DEFAULT_SHUTDOWN_TIMEOUT_MILLIS = 10000;

		private int maximumConnectionAttempts = DEFAULT_MAXIMUM_CONNECTION_ATTEMPTS;
		private int maximumReadinessAttempts = DEFAULT_MAXIMUM_READINESS_ATTEMPTS;
		private long shutdownTimeoutMillis = DEFAULT_SHUTDOWN_TIMEOUT_MILLIS;

		public Builder withMaximumConnectionAttempts(int maximumConnectionAttempts) {
			this.maximumConnectionAttempts = maximumConnectionAttempts;
			return this;
		}

		/**
		 * The maximum number of attempts at checking that the server is ready to accept requests.
		 */
		public Builder withMaximumReadinessAttempts(int maximumReadinessAttempts) {
			this.maximumReadinessAttempts  = maximumReadinessAttempts;
			return this;
		}

		/**
		 * The maximum time to wait for the server process to exit after requesting shutdown.
		 */
		public Builder withShutdownTimeoutMillis(long shutdownTimeoutMillis) {
			this.shutdownTimeoutMillis = shutdownTimeoutMillis;
			return this;
		}

		public ConnectionProperties build() {
			return new ConnectionProperties(maximumConnectionAttempts, maximumReadinessAttempts, shutdownTimeoutMillis);
		}
	}
}
