package uk.co.datumedge.redislauncher;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public final class AlwaysDestroyLifecyclePolicyTest {
	private final Mockery context = new JUnit4Mockery();
	private final RedisServer server = context.mock(RedisServer.class);
	private final LifecyclePolicy lifecyclePolicy = new AlwaysDestroyLifecyclePolicy();

	@Test
	public void destroysServerOnFailureToStart() {
		expectServerDestroy();
		lifecyclePolicy.failedToStart(server);
	}

	@Test
	public void destroysServerOnFailureToStop() throws IOException {
		expectServerDestroy();
		lifecyclePolicy.failedToStop(server);
	}

	@Test
	public void providesShutdownHookProcessDestroyer() {
		assertThat(lifecyclePolicy.getProcessDestroyer(), is(instanceOf(ShutdownHookProcessDestroyer.class)));
	}

	private void expectServerDestroy() {
		context.checking(new Expectations() {{
			oneOf(server).destroy();
		}});
	}
}
