package uk.co.datumedge.redislauncher;


public final class ConnectionProperties {
	public static final ConnectionProperties DEFAULT = new Builder().build();
	private final int maximumConnectionAttempts;

	private ConnectionProperties(int maximumConnectionAttempts) {
		this.maximumConnectionAttempts = maximumConnectionAttempts;
	}

	public int getMaximumConnectionAttempts() {
		return maximumConnectionAttempts;
	}

	public static final class Builder {
		private static final int DEFAULT_MAXIMUM_CONNECTION_ATTEMPTS = 5;
		private static final int DEFAULT_MAXIMUM_READINESS_ATTEMPTS = 5;
		private static final long DEFAULT_SHUTDOWN_TIMEOUT_MILLIS = 10000;

		private int maximumConnectionAttempts = DEFAULT_MAXIMUM_CONNECTION_ATTEMPTS;

		public Builder withMaximumConnectionAttempts(int maximumConnectionAttempts) {
			this.maximumConnectionAttempts = maximumConnectionAttempts;
			return this;
		}

		public ConnectionProperties build() {
			return new ConnectionProperties(maximumConnectionAttempts);
		}
	}
}
