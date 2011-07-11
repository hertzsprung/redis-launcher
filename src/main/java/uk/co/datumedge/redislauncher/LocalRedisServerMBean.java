package uk.co.datumedge.redislauncher;

import java.io.IOException;

/**
 * JMX MBean interface for a redis server. Instances should be registered with the {@code ObjectName}
 * <code>uk.co.datumedge.redislauncher:type=LocalRedisServer,name=<i>anyName</i></code>
 */
public interface LocalRedisServerMBean {
	void start() throws IOException, InterruptedException;
	void stop() throws IOException;
}
