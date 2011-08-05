package uk.co.datumedge.redislauncher;

import java.io.IOException;

/**
 * A lifecycle policy that keeps the server running when an exception occurs.  The server will not be destroyed.
 */
public final class KeepRunningOnErrorLifecyclePolicy implements LifecyclePolicy {
	public static final int DEFAULT_MAXIMUM_CONNECTION_ATTEMPTS = 5;
	public static final int DEFAULT_MAXIMUM_READINESS_ATTEMPTS = 5;
	public static final long DEFAULT_SHUTDOWN_TIMEOUT_MILLIS = 10000;
	private final int maximumReadinessAttempts;
	private final long shutdownTimeoutMillis;

	KeepRunningOnErrorLifecyclePolicy() {
		this(DEFAULT_MAXIMUM_READINESS_ATTEMPTS, DEFAULT_SHUTDOWN_TIMEOUT_MILLIS);
	}

	private KeepRunningOnErrorLifecyclePolicy(int maximumReadinessAttempts, long shutdownTimeoutMillis) {
		this.maximumReadinessAttempts = maximumReadinessAttempts;
		this.shutdownTimeoutMillis = shutdownTimeoutMillis;
	}

	@Override
	public void failedToStart(RedisServer redisServer) {
	}

	@Override
	public void failedToStop(RedisServer redisServer) throws IOException {
		throw new IOException("Failed to stop redis server");
	}

	@Override
	public int getMaximumReadinessAttempts() {
		return maximumReadinessAttempts;
	}

	@Override
	public long getShutdownTimeoutMillis() {
		return shutdownTimeoutMillis;
	}

	public static final class Builder {
		private int maximumReadinessAttempts = DEFAULT_MAXIMUM_READINESS_ATTEMPTS;
		private long shutdownTimeoutMillis = DEFAULT_SHUTDOWN_TIMEOUT_MILLIS;

		public Builder withMaximumReadinessAttempts(int maximumReadinessAttempts) {
			this.maximumReadinessAttempts = maximumReadinessAttempts;
			return this;
		}

		public Builder withShutdownTimeoutMillis(long shutdownTimeoutMillis) {
			this.shutdownTimeoutMillis = shutdownTimeoutMillis;
			return this;
		}

		public KeepRunningOnErrorLifecyclePolicy build() {
			return new KeepRunningOnErrorLifecyclePolicy(maximumReadinessAttempts, shutdownTimeoutMillis);
		}
	}
}
