/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore.DumpNode.BadNodeException;

/**
 * TODO
 */
public class NQuadLineSplitterTest extends AbstractTestClass {
	private static List<String> testData;

	@BeforeClass
	public static void readTestData() throws IOException {
		InputStream stream = NQuadLineSplitterTest.class
				.getResourceAsStream("NQuadLineSplitterTest.nq");
		testData = IOUtils.readLines(stream);
	}

	@Test
	public void splitLine1() throws BadNodeException {
		List<String> strings = new NQuadLineSplitter(getLine(1)).split();
		assertEquals("count", 4, strings.size());
		assertEquals("subject", "<http://purl.org/ontology/bibo/degree>",
				strings.get(0));
		assertEquals("predicate",
				"<http://purl.obolibrary.org/obo/IAO_0000112>", strings.get(1));
		assertEquals(
				"object",
				"\"The source of the public description and this info is found here:  "
						+ "http://bibotools.googlecode.com/svn/bibo-ontology/trunk/doc/index.html.  "
						+ "Bibo considers this term \\\"unstable\\\".  "
						+ "The bibo editorial note is: \\\"We are not defining, using an enumeration, "
						+ "the range of the bibo:degree to the defined list of bibo:ThesisDegree. "
						+ "We won't do it because we want people to be able to define new degress "
						+ "if needed by some special usecases. "
						+ "Creating such an enumeration would restrict this to "
						+ "happen.\\\"\"^^<http://www.w3.org/2001/XMLSchema#string>",
				strings.get(2));
		assertEquals(
				"graph",
				"<http://vitro.mannlib.cornell.edu/filegraph/tbox/object-properties.owl>",
				strings.get(3));
	}

	private String getLine(int i) {
		return testData.get(i - 1);
	}

}
