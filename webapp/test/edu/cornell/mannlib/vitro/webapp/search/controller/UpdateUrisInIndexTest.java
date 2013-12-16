/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

/**
 * Accepts requests to update a set of URIs in the search index.
 */
public class UpdateUrisInIndexTest {

	@Test(expected = NullPointerException.class)
	public void nullString() {
		scan(null, 0);
	}

	@Test
	public void emptyString() {
		scan("", 0);
	}

	@Test
	public void nothingButDelimiters() {
		scan(" , ", 0);
		scan("\n", 0);
		scan("\n\n\n", 0);
		scan("\n, \t\r   ,\n\n", 0);
	}

	@Test
	public void oneTokenNoDelimiters() {
		scan("http://bogus.com/n234", 1);
	}

	@Test
	public void oneTokenAssortedDelimiters() {
		scan("http://bogus.com/n234\n", 1);
		scan("\nhttp://bogus.com/n234", 1);
		scan("\nhttp://bogus.com/n234\n", 1);
	}

	@Test
	public void twoTokensAssortedDelimiters() {
		scan("http://bogus.com/n234\nhttp://bogus.com/n442", 2);
		scan("http://bogus.com/n234, http://bogus.com/n442", 2);
		scan("http://bogus.com/n234,\nhttp://bogus.com/n442\n", 2);
	}

	@Test
	public void nonBreakingSpace() {
		scan("non\u00A0breaking\u00A0space", 1);
	}

	@Test
	public void omnibus() {
		scan("  a  ,  b,c d\t,\re", 5);
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	public void scan(String input, int expectedUris) {
		Reader reader = (input == null) ? null : new StringReader(input);
		Iterator<String> it = new UpdateUrisInIndex().createScanner(reader);
		int count = 0;
		while (it.hasNext()) {
			String uri = it.next();
			if (uri == null) {
				Assert.fail("Scanner should not return null strings \n "
						+ "Null string for uri #" + count + " for input '"
						+ input + "'");
			} else if (uri.isEmpty()) {
				Assert.fail("Scanner should not return empty strings \n "
						+ "Empty string for uri #" + count + " for input '"
						+ input + "'");
			}
			count++;
		}
		Assert.assertEquals("Incorrect number of URIs from input '" + input
				+ "'", expectedUris, count);
	}

}
