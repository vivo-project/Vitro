/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.searchengine;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

public class AutoCompleteWordsTest extends AbstractTestClass {
	private static final String WORD_DELIMITER = "[, ]+";
	private static final String FIELD_NAME_COMPLETE = "complete";
	private static final String FIELD_NAME_PARTIAL = "partial";

	@Test
	public void nullSearchTerm() {
		assertQueryString(null, "");
	}

	@Test
	public void emptySearchTerm() {
		assertQueryString("", "");
	}

	@Test
	public void blankSearchTerm() {
		assertQueryString(" ", "");
	}

	@Test
	public void searchTermContainsOnlyCommas() {
		assertQueryString(",,", "");
	}

	@Test
	public void oneWord() {
		assertQueryString("first", "partial:\"first\"");
	}

	@Test
	public void twoWords() {
		assertQueryString("first, second",
				"complete:\"first\" AND partial:\"second\"");
	}

	@Test
	public void threeWords() {
		assertQueryString("first, second, third",
				"complete:\"first\" AND complete:\"second\" AND partial:\"third\"");
	}

	@Test
	public void oneWordAndComma() {
		assertQueryString("first,", "complete:\"first\"");
	}

	@Test
	public void oneWordAndCommaAndSpace() {
		assertQueryString("first, ", "complete:\"first\"");
	}

	@Test
	public void emptyCompleteWord() {
		assertQueryString(", second", "partial:\"second\"");
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void assertQueryString(String searchTerm, String expected) {
		AutoCompleteWords acw = new AutoCompleteWords(searchTerm,
				WORD_DELIMITER);
		String actual = acw.assembleQuery(FIELD_NAME_COMPLETE,
				FIELD_NAME_PARTIAL);
		assertEquals(expected, actual);
	}

}
