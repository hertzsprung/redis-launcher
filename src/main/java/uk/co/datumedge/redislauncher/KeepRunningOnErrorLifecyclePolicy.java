package uk.co.datumedge.redislauncher;

/**
 * A lifecycle policy that keeps the server running when an exception occurs.  The server will not be destroyed.
 */
public class KeepRunningOnErrorLifecyclePolicy {
	private final int maximumReadinessAttempts;
	private final long shutdownTimeoutMillis;

	public KeepRunningOnErrorLifecyclePolicy(int maximumReadinessAttempts, long shutdownTimeoutMillis) {
		this.maximumReadinessAttempts = maximumReadinessAttempts;
		this.shutdownTimeoutMillis = shutdownTimeoutMillis;
	}

	public int getMaximumReadinessAttempts() {
		return maximumReadinessAttempts;
	}

	public long getShutdownTimeoutMillis() {
		return shutdownTimeoutMillis;
	}
}
