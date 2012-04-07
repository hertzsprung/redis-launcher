package uk.co.datumedge.redislauncher;

import java.io.ByteArrayOutputStream;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.TypeSafeMatcher;

import com.google.common.primitives.Bytes;

final class Matchers {
	/**
	 * Evaluates to true if the {@code ByteArrayOutputStream} is not empty.
	 *
	 * @return a {@code Matcher} instance
	 */
	public static Matcher<ByteArrayOutputStream> containsBytes() {
		return new TypeSafeDiagnosingMatcher<ByteArrayOutputStream>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("a ByteArrayOutputStream that contains bytes");
			}

			@Override
			protected boolean matchesSafely(ByteArrayOutputStream item, Description mismatchDescription) {
				boolean matches = item.toByteArray().length > 0;
				if (!matches) mismatchDescription.appendText("an empty ByteArrayOutputStream");
				return matches;
			}
		};
	}

	/**
	 * Evaluates to true if the expected byte array appears somewhere within the actual byte array.
	 *
	 * @param expected
	 *            the expected byte array instance
	 * @return a {@code Matcher} instance
	 */
	public static Matcher<byte[]> containsBytes(final byte[] expected) {
		return new TypeSafeMatcher<byte[]>() {
			@Override
			public void describeTo(Description description) {
				description.appendValue(expected);
			}

			@Override
			protected boolean matchesSafely(byte[] actual) {
				return Bytes.indexOf(actual, expected) != -1;
			}
		};
	}
}
