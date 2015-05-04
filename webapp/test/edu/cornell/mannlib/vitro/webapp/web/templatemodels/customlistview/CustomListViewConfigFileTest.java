/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.customlistview;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * Note: when testing, an "empty" element may be self-closing, or with
 * zero-length text content, or with only blank text content.
 */
public class CustomListViewConfigFileTest extends AbstractTestClass {
	/**
	 * Use this XML to test the methods that strip tags from the select clause.
	 * 
	 * If not collated, omit the "collated" tag. If editing, omit the
	 * "critical-data-required" tag.
	 */
	private static final String XML_WITH_RICH_SELECT_CLAUSE = "<list-view-config>"
			+ "<query-select>SELECT"
			+ "  <collated>collated1</collated>"
			+ "  <collated>collated2</collated>"
			+ "  <critical-data-required>critical</critical-data-required>"
			+ "</query-select>"
			+ "<template>template.ftl</template>"
			+ "</list-view-config>";

	/**
	 * In general, we expect no exception, but individual tests may override,
	 * like this:
	 * 
	 * <pre>
	 * thrown.expect(InvalidConfigurationException.class);
	 * thrown.expectMessage(&quot;Bozo&quot;);
	 * </pre>
	 */
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private CustomListViewConfigFile configFile;

	// ----------------------------------------------------------------------
	// Test failures
	// ----------------------------------------------------------------------

	@Test
	public void readerIsNull() throws InvalidConfigurationException {
		expectException("Config file reader is null.");
		configFile = new CustomListViewConfigFile(null);
	}

	@Test
	public void readerThrowsIOException() throws InvalidConfigurationException {
		expectException("Unable to read config file.");
		configFile = new CustomListViewConfigFile(new ExplodingReader());
	}

	@Test
	public void invalidXml() throws InvalidConfigurationException {
		suppressSyserr(); // catch the error report from the XML parser
		expectException(CoreMatchers
				.containsString("Config file is not valid XML:"));
		readConfigFile("<unbalancedTag>");
	}

	@Test
	public void selectQueryMissing() throws InvalidConfigurationException {
		expectException("Config file must contain a query-select element");
		readConfigFile("<list-view-config>"
				+ "<template>template.ftl</template>" + "</list-view-config>");
	}

	@Test
	public void selectQueryMultiple() throws InvalidConfigurationException {
		expectException("Config file may not contain more than one query-select element");
		readConfigFile("<list-view-config>"
				+ "<query-select>SELECT</query-select>"
				+ "<query-select>ANOTHER</query-select>"
				+ "<template>template.ftl</template>" + "</list-view-config>");
	}

	@Test
	public void selectQueryEmpty() throws InvalidConfigurationException {
		expectException("In a config file, the <query-select> element must not be empty.");
		readConfigFile("<list-view-config>" + "<query-select/>"
				+ "<template>template.ftl</template>" + "</list-view-config>");
	}

	@Test
	public void templateNameMissing() throws InvalidConfigurationException {
		expectException("Config file must contain a template element");
		readConfigFile("<list-view-config>"
				+ "<query-select>SELECT</query-select>" + "</list-view-config>");
	}

	@Test
	public void templateNameMultiple() throws InvalidConfigurationException {
		expectException("Config file may not contain more than one template element");
		readConfigFile("<list-view-config>"
				+ "<query-select>SELECT</query-select>"
				+ "<template>template.ftl</template>"
				+ "<template>another.ftl</template>" + "</list-view-config>");
	}

	@Test
	public void templateNameEmpty() throws InvalidConfigurationException {
		expectException("In a config file, the <template> element must not be empty.");
		readConfigFile("<list-view-config>"
				+ "<query-select>SELECT</query-select>"
				+ "<template> </template>" + "</list-view-config>");
	}

	@Test
	public void postprocessorNameMultiple() throws InvalidConfigurationException {
		expectException("Config file may not contain more than one postprocessor element");
		readConfigFile("<list-view-config>"
				+ "<query-select>SELECT</query-select>"
				+ "<template>template.ftl</template>"
				+ "<postprocessor>ONE</postprocessor>"
				+ "<postprocessor>TWO</postprocessor>" + "</list-view-config>");
	}

	// ----------------------------------------------------------------------
	// Test successes
	// ----------------------------------------------------------------------

	@Test
	public void minimalSuccess() throws InvalidConfigurationException {
		readConfigFile("<list-view-config>"
				+ "<query-select>SELECT</query-select>"
				+ "<template>template.ftl</template>" + "</list-view-config>");
		assertConfigFile(true, false, "SELECT", constructs(), "template.ftl",
				"");
	}

	@Test
	public void maximalSuccess() throws InvalidConfigurationException {
		readConfigFile("<list-view-config>"
				+ "<query-select>SELECT</query-select>"
				+ "<query-construct>CONSTRUCT ONE</query-construct>"
				+ "<query-construct>CONSTRUCT TWO</query-construct>"
				+ "<template>template.ftl</template>"
				+ "<postprocessor>post.processor.name</postprocessor>"
				+ "</list-view-config>");
		assertConfigFile(true, false, "SELECT",
				constructs("CONSTRUCT ONE", "CONSTRUCT TWO"), "template.ftl",
				"post.processor.name");
	}

	@Test
	public void postprocessorEmptyIsOK() throws InvalidConfigurationException {
		readConfigFile("<list-view-config>"
				+ "<query-select>SELECT</query-select>"
				+ "<query-construct>CONSTRUCT</query-construct>"
				+ "<template>template.ftl</template>"
				+ "<postprocessor></postprocessor>" + "</list-view-config>");
		assertConfigFile(true, false, "SELECT", constructs("CONSTRUCT"),
				"template.ftl", "");
	}

	@Test
	public void selectCollatedEditing() throws InvalidConfigurationException {
		readConfigFile(XML_WITH_RICH_SELECT_CLAUSE);
		assertConfigFile(true, true, "SELECT  collated1  collated2  ", constructs(),
				"template.ftl", "");
	}

	@Test
	public void selectCollatedNotEditing() throws InvalidConfigurationException {
		readConfigFile(XML_WITH_RICH_SELECT_CLAUSE);
		assertConfigFile(true, false, "SELECT  collated1  collated2  critical",
				constructs(), "template.ftl", "");
	}

	@Test
	public void selectNotCollatedEditing() throws InvalidConfigurationException {
		readConfigFile(XML_WITH_RICH_SELECT_CLAUSE);
		assertConfigFile(false, true, "SELECT      ", constructs(),
				"template.ftl", "");
	}

	@Test
	public void selectNotCollatedNotEditing() throws InvalidConfigurationException {
		readConfigFile(XML_WITH_RICH_SELECT_CLAUSE);
		assertConfigFile(false, false, "SELECT      critical", constructs(),
				"template.ftl", "");
	}

	/**
	 * <pre>
	 * TODO Successes:
	 *   select query with all tags
	 *   	collated, editing, both, neither
	 * 
	 * </pre>
	 */

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void expectException(String message) {
		thrown.expect(InvalidConfigurationException.class);
		thrown.expectMessage(message);
	}

	private void expectException(Matcher<String> matcher) {
		thrown.expect(InvalidConfigurationException.class);
		thrown.expectMessage(matcher);
	}

	private void readConfigFile(String xmlString)
			throws InvalidConfigurationException {
		StringReader reader = new StringReader(xmlString);
		configFile = new CustomListViewConfigFile(reader);
	}

	private String[] constructs(String... constructQueries) {
		return constructQueries;
	}

	private void assertConfigFile(boolean collated, boolean editing,
			String selectQuery, String[] constructQueries, String templateName,
			String postprocessorName) {
		assertEquals("select query", selectQuery,
				configFile.getSelectQuery(collated, editing));
		assertEquals("construct queries",
				new HashSet<String>(Arrays.asList(constructQueries)),
				configFile.getConstructQueries());
		assertEquals("template name", templateName,
				configFile.getTemplateName());
		assertEquals("postprocessor name", postprocessorName,
				configFile.getPostprocessorName());
	}

	// ----------------------------------------------------------------------
	// Supporting classes
	// ----------------------------------------------------------------------

	private class ExplodingReader extends Reader {
		@Override
		public int read(char[] arg0, int arg1, int arg2) throws IOException {
			throw new IOException("ExplodingReader threw an exception.");
		}

		@Override
		public void close() throws IOException {
			// Nothing to close.
		}

	}

}
