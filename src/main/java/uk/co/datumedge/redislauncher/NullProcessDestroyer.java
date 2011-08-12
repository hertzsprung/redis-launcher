package uk.co.datumedge.redislauncher;

import org.apache.commons.exec.ProcessDestroyer;

final class NullProcessDestroyer implements ProcessDestroyer {
	public static final ProcessDestroyer INSTANCE = new NullProcessDestroyer();

	@Override
	public boolean add(Process process) {
		return false;
	}

	@Override
	public boolean remove(Process process) {
		return false;
	}

	@Override
	public int size() {
		return -1;
	}
}
