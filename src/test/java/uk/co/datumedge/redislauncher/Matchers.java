package uk.co.datumedge.redislauncher;

import java.io.ByteArrayOutputStream;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

final class Matchers {
	static Matcher<ByteArrayOutputStream> containsBytes() {
		return new TypeSafeDiagnosingMatcher<ByteArrayOutputStream>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("A ByteArrayOutputStream that contains bytes");
			}

			@Override
			protected boolean matchesSafely(ByteArrayOutputStream item, Description description) {
				boolean matches = item.toByteArray().length > 0;
				if (!matches) description.appendText("An empty ByteArrayOutputStream");
				return matches;
			}
		};
	}
}
