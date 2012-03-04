package uk.co.datumedge.redislauncher;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public final class ExecutionProcessDestroyerTest {
	private final Mockery context = new JUnit4Mockery() {{
		setImposteriser(ClassImposteriser.INSTANCE);
	}};
	private final Process process = context.mock(Process.class);
	private final ExecutionProcessDestroyer executionProcessDestroyer = new ExecutionProcessDestroyer();

	@Test
	public void destroysProcessAddedToDestroyer() {
		context.checking(new Expectations() {{
			oneOf(process).destroy();
		}});

		executionProcessDestroyer.add(process);
		executionProcessDestroyer.destroy();
	}

	@Test
	public void doesNotDestroyProcessAddedThenRemovedFromDestroyer() {
		executionProcessDestroyer.add(process);
		executionProcessDestroyer.remove(process);
		executionProcessDestroyer.destroy();
	}

	@Test
	public void hasSizeOfOneWhenProcessAddedToDestroyer() {
		executionProcessDestroyer.add(process);
		assertThat(executionProcessDestroyer.size(), is(equalTo(1)));
	}

	@Test
	public void returnsTrueIfProcessAddedToDestroyer() {
		assertThat(executionProcessDestroyer.add(process), is(true));
	}

	@Test
	public void returnsFalseIfProcessWasAlreadyAddedToDestroyer() {
		executionProcessDestroyer.add(process);
		assertThat(executionProcessDestroyer.add(process), is(false));
	}

	@Test
	public void returnsTrueIfProcessRemovedFromDestroyer() {
		executionProcessDestroyer.add(process);
		assertThat(executionProcessDestroyer.remove(process), is(true));
	}

	@Test
	public void returnsFalseIfTryingToRemoveProcessNotAddedToDestroyer() {
		assertThat(executionProcessDestroyer.remove(process), is(false));
	}
}
