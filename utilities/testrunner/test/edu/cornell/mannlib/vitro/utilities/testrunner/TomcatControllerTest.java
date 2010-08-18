/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

/**
 * TODO
 */
public class TomcatControllerTest {

	// ----------------------------------------------------------------------
	// Tests for parseCommandLine()
	// ----------------------------------------------------------------------

	@Test
	public void oneArgument() {
		assertExpectedParsing("oneArgument", "oneArgument");
	}

	@Test
	public void multipleArguments() {
		assertExpectedParsing("more than one", "more", "than", "one");
	}

	@Test
	public void quotedArgument() {
		assertExpectedParsing("contains \"quoted blank\" string", "contains",
				"quoted blank", "string");
	}

	@Test(expected = IllegalArgumentException.class)
	public void mismatchedQuotes() {
		assertExpectedParsing("contains mismatched \"quote");
	}

	@Test
	public void emptyLine() {
		assertExpectedParsing("");
	}

	private void assertExpectedParsing(String commandLine,
			String... expectedPieces) {
		assertEquals("parse", Arrays.asList(expectedPieces),
				TomcatController.parseCommandLine(commandLine));
	}
}
