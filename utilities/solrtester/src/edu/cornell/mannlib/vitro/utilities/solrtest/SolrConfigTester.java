/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.solrtest;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * TODO
 */
@RunWith(JUnit4.class)
public class SolrConfigTester {
	private static CoreContainer container;
	private static EmbeddedSolrServer server;

	@Before
	public void setup() throws Exception {
		String solrHomeString = System.getProperty("test.solr.home");
		container = new CoreContainer(solrHomeString);

		try (LogLeveler l = new LogLeveler(SolrCore.class, Level.ERROR)) {
			container.load();
		}

		server = new EmbeddedSolrServer(container, "collection1");
		server.deleteByQuery("*:*");
		server.commit();
	}

	@After
	public void tearDown() throws Exception {
		server.shutdown();
	}

	// ----------------------------------------------------------------------
	// The tests
	// ----------------------------------------------------------------------

	@Test
	public void serverResponds() throws Exception {
		server.ping();
	}

	@Test(expected = SolrException.class)
	public void docIdIsRequred() throws Exception {
		try (LogLeveler l = new LogLeveler(SolrCore.class, Level.OFF)) {
			addDocuments(inputDoc(null, field("nameRaw", "No ID")));
		}
	}

	@Test
	public void simpleName() throws Exception {
		addDocuments(inputDoc("idSimple", field("nameRaw", "SimpleName")));
		assertQueryResults("simple name", "SimpleName", "idSimple");
	}

	@Test
	public void upperLowerName() throws Exception {
		addDocuments(inputDoc("idUpperLower", field("nameRaw", "Lower, Upper")));
		assertQueryResults("upper lower name", "Lower", "idUpperLower");
		assertQueryResults("upper lower name", "lower", "idUpperLower");
		assertQueryResults("upper lower name", "UPPER", "idUpperLower");
	}

	/**
	 * Why does it behave as if name is stemmed? Is that the behavior that we
	 * want?
	 */
	@Ignore
	@Test
	public void nameIsNotStemmed() throws Exception {
		addDocuments(inputDoc("nameStemming",
				field("nameRaw", "Swimming, Bills"),
				field("nameLowercaseSingleValued", "Lower, Upper")));
		assertQueryResults("name not stemmed", "Swimming", "nameStemming");
		assertQueryResults("name not stemmed", "Bills", "nameStemming");
		assertQueryResults("name not stemmed", "Swim");
		assertQueryResults("name not stemmed", "Bill");
	}

	/**
	 * A name with an umlaut over an o is found exactly, or with the umlaut
	 * missing, upper case or lower case.
	 */
	@Test
	public void nameWithUmlaut() throws Exception {
		addDocuments(inputDoc("idUmlaut",
				field("nameRaw", "P\u00F6ysen B\u00F6ysen")));
		assertQueryResults("name with umlaut", "Boysen", "idUmlaut");
		assertQueryResults("name with umlaut", "B\u00F6ysen", "idUmlaut");
		assertQueryResults("name with umlaut", "BOYSEN", "idUmlaut");
		assertQueryResults("name with umlaut", "B\u00D6YSEN", "idUmlaut");
	}

	/**
	 * ALLTEXT is searched, but to make the 3rd case work, we need
	 * ALLTEXTUNSTEMMED also. Why is that?
	 */
	@Test
	public void allTextIsSearched() throws Exception {
		addDocuments(inputDoc("allTextSearch", field("ALLTEXT", "Wonderful"),
				field("ALLTEXTUNSTEMMED", "Wonderful")));
		assertQueryResults("all text", "Wonderful", "allTextSearch");
		assertQueryResults("all text", "wonderful", "allTextSearch");
		assertQueryResults("all text", "WoNdErFuL", "allTextSearch");
	}

	@Test
	public void allTextIsStemmed() throws Exception {
		addDocuments(inputDoc("allTextStem", field("ALLTEXT", "Swimming"),
				field("ALLTEXTUNSTEMMED", "Swimming")));
		assertQueryResults("all text stem", "Swim", "allTextStem");
		assertQueryResults("all text stem", "swim", "allTextStem");
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	public InputField field(String name, String... values) {
		return new InputField(name, null, values);
	}

	public InputField field(String name, Float boost, String... values) {
		return new InputField(name, boost, values);
	}

	public SolrInputDocument inputDoc(String docId, InputField... fields) {
		SolrInputDocument doc = new SolrInputDocument();
		if (docId != null) {
			doc.addField("DocId", docId);
		}
		for (InputField f : fields) {
			for (String value : f.values) {
				if (f.boost == null) {
					doc.addField(f.name, value);
				} else {
					doc.addField(f.name, value, f.boost);
				}
			}
		}
		return doc;
	}

	public void addDocuments(SolrInputDocument... documents)
			throws SolrServerException, IOException {
		for (SolrInputDocument doc : documents) {
			server.add(doc);
		}
		server.commit();
	}

	private void assertQueryResults(String message, String query,
			String... expectedDocIds) throws SolrServerException {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", query);

		QueryResponse qResp = server.query(params);

		SolrDocumentList docList = qResp.getResults();
		List<String> expected = Arrays.asList(expectedDocIds);
		List<String> actual = new ArrayList<>();
		for (int i = 0; i < docList.getNumFound(); i++) {
			actual.add(String.valueOf(docList.get(i).getFirstValue("DocId")));
		}
		assertEquals(message + " : '" + query + "'", expected, actual);
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	public static class InputField {
		final String name;
		final Float boost;
		final String[] values;

		public InputField(String name, Float boost, String[] values) {
			this.name = name;
			this.boost = boost;
			this.values = values;
		}
	}

	public static class LogLeveler implements AutoCloseable {
		private final Logger logger;
		private final Level initialLevel;

		public LogLeveler(Class<?> clazz, Level desiredLevel) {
			this.logger = Logger.getLogger(clazz);
			this.initialLevel = this.logger.getLevel();
			this.logger.setLevel(desiredLevel);
		}

		@Override
		public void close() {
			this.logger.setLevel(this.initialLevel);
		}

	}
}

/**
 * TODO
 * 
 * <pre>
 * // ** Let's index a document into our embedded server
 * 
 * SolrInputDocument newDoc = new SolrInputDocument();
 * newDoc.addField(&quot;title&quot;, &quot;Test Document 1&quot;);
 * newDoc.addField(&quot;id&quot;, &quot;doc-1&quot;);
 * newDoc.addField(&quot;text&quot;, &quot;Hello world!&quot;);
 * server.add(newDoc);
 * server.commit();
 * 
 * // ** And now let's query for it
 * 
 * params.set(&quot;q&quot;, &quot;title:test&quot;);
 * QueryResponse qResp = server.query(params);
 * 
 * SolrDocumentList docList = qResp.getResults();
 * System.out.println(&quot;Num docs: &quot; + docList.getNumFound());
 * SolrDocument doc = docList.get(0);
 * System.out.println(&quot;Title: &quot; + doc.getFirstValue(&quot;title&quot;).toString());
 * </pre>
 */
