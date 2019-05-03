/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.json;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * quotes,\, \r, \n, \b, \f, \t and other control characters.
 *
 *
 */
public class JacksonUtilsTest extends AbstractTestClass {

	// ----------------------------------------------------------------------
	// Tests for quote()
	// quotes,\, \r, \n, \b, \f, \t and other control characters.
	// Originally written as direct comparisons to the net.sf.json version.
	// ----------------------------------------------------------------------

	@Test
	public void quoteNull() {
		assertJacksonQuoted(null, "");
		// assertNetSfJsonQuoted(null, "");
	}

	@Test
	public void quoteQuote() {
		assertJacksonQuoted("\"", "\\\"");
		// assertNetSfJsonQuoted("\"", "\\\"");
	}

	@Test
	public void quoteBackslash() {
		assertJacksonQuoted("\\", "\\\\");
		// assertNetSfJsonQuoted("\\", "\\\\");
	}

	@Test
	public void quoteReturn() {
		assertJacksonQuoted("\r", "\\r");
		// assertNetSfJsonQuoted("\r", "\\r");
	}

	@Test
	public void quoteUnicode() {
		assertJacksonQuoted("\u0007", "\\u0007");
		// assertNetSfJsonQuoted("\u0007", "\\u0007");
	}

	@Test
	public void quoteAssorted() {
		assertJacksonQuoted("\n\b\f\t", "\\n\\b\\f\\t");
		// assertNetSfJsonQuoted("\n\b\f\t", "\\n\\b\\f\\t");
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void assertJacksonQuoted(String raw, String expected) {
		String actual = JacksonUtils.quote(raw);
		assertEquals("\"" + expected + "\"", actual);
	}

	// private void assertNetSfJsonQuoted(String raw, String expected) {
	// String actual = net.sf.json.util.JSONUtils.quote(raw);
	// assertEquals("\"" + expected + "\"", actual);
	// }
}
