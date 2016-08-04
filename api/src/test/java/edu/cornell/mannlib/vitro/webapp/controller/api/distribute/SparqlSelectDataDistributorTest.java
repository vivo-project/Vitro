/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.distribute;

import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.dataProperty;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.model;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.objectProperty;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import stubs.edu.cornell.mannlib.vitro.webapp.controller.api.distribute.DataDistributorContextStub;

/**
 * TODO
 */
public class SparqlSelectDataDistributorTest extends AbstractTestClass {
	private static final String NS = "http://this.name/space#";
	private static final String BOOK1 = NS + "book1";
	private static final String BOOK2 = NS + "book2";
	private static final String AUTHOR1 = NS + "author1";
	private static final String AUTHOR2 = NS + "author2";
	private static final String TITLE1 = NS + "title1";
	private static final String TITLE2 = NS + "title2";
	private static final String NAME1 = NS + "name1";
	private static final String NAME2 = NS + "name2";
	private static final String HAS_AUTHOR = NS + "hasAuthor";
	private static final String HAS_TITLE = NS + "hasTitle";
	private static final String HAS_NAME = NS + "hasName";
	private static final String RAW_QUERY = ""
			+ "PREFIX ns: <http://this.name/space#> \n " //
			+ "SELECT ?book ?author ?title ?name \n " //
			+ "WHERE { \n " //
			+ "  ?book ns:hasAuthor ?author . \n " //
			+ "  ?book ns:hasTitle ?title . \n " //
			+ "  ?author ns:hasName ?name . \n " //
			+ "}";

	private Model model;
	private SparqlSelectDataDistributor distributor;
	private DataDistributorContextStub ddContext;

	@Before
	public void setup() {
		model = model(dataProperty(BOOK1, HAS_TITLE, TITLE1),
				dataProperty(BOOK2, HAS_TITLE, TITLE2),
				dataProperty(AUTHOR1, HAS_NAME, NAME1),
				dataProperty(AUTHOR2, HAS_NAME, NAME2),
				objectProperty(BOOK1, HAS_AUTHOR, AUTHOR1),
				objectProperty(BOOK1, HAS_AUTHOR, AUTHOR2),
				objectProperty(BOOK2, HAS_AUTHOR, AUTHOR1));

		ddContext = new DataDistributorContextStub(model);

		distributor = new SparqlSelectDataDistributor();
		distributor.setRawQuery(RAW_QUERY);
	}

	@After
	public void cleanup() throws DataDistributorException {
		distributor.close();
	}

	// ----------------------------------------------------------------------
	// The tests
	// ----------------------------------------------------------------------

	@Test
	public void noBinding() throws DataDistributorException, JSONException {
		runAndAssertResult(
				row(uri("author", AUTHOR1), uri("book", BOOK1),
						literal("name", NAME1), literal("title", TITLE1)),
				row(uri("author", AUTHOR1), uri("book", BOOK2),
						literal("name", NAME1), literal("title", TITLE2)),
				row(uri("author", AUTHOR2), uri("book", BOOK1),
						literal("name", NAME2), literal("title", TITLE1)));
	}

	@Test
	public void bindAuthorUri() throws DataDistributorException, JSONException {
		bindAsUri("author", AUTHOR1);
		runAndAssertResult(
				row(uri("book", BOOK1), literal("name", NAME1),
						literal("title", TITLE1)),
				row(uri("book", BOOK2), literal("name", NAME1),
						literal("title", TITLE2)));
	}

	@Test
	public void bindBookUri() throws DataDistributorException, JSONException {
		bindAsUri("book", BOOK2);
		runAndAssertResult(row(uri("author", AUTHOR1), literal("name", NAME1),
				literal("title", TITLE2)));
	}

	@Test
	public void bindAuthorAndBook()
			throws DataDistributorException, JSONException {
		bindAsUri("author", AUTHOR1);
		bindAsUri("book", BOOK1);
		runAndAssertResult(
				row(literal("name", NAME1), literal("title", TITLE1)));
	}

	@Test
	public void bindTitleLiteral()
			throws DataDistributorException, JSONException {
		bindAsLiteral("title", TITLE2);
		runAndAssertResult(row(uri("author", AUTHOR1), uri("book", BOOK2),
				literal("name", NAME1)));
	}

	@Test
	public void bindNameLiteral()
			throws DataDistributorException, JSONException {
		bindAsLiteral("name", NAME1);
		runAndAssertResult(
				row(uri("author", AUTHOR1), uri("book", BOOK1),
						literal("title", TITLE1)),
				row(uri("author", AUTHOR1), uri("book", BOOK2),
						literal("title", TITLE2)));
	}

	@Test
	public void bindTitleAndName()
			throws DataDistributorException, JSONException {
		bindAsLiteral("name", NAME1);
		bindAsLiteral("title", TITLE1);
		runAndAssertResult(row(uri("author", AUTHOR1), uri("book", BOOK1)));
	}

	// ----------------------------------------------------------------------
	// Helper methods and classes
	// ----------------------------------------------------------------------

	private ResultRow row(Binding... bindings) {
		return new ResultRow(bindings);
	}

	private UriBinding uri(String key, String value) {
		return new UriBinding(key, value);
	}

	private LiteralBinding literal(String key, String value) {
		return new LiteralBinding(key, value);
	}

	private void bindAsUri(String name, String uri) {
		distributor.addUriBindingName(name);
		ddContext.setParameter(name, uri);
	}

	private void bindAsLiteral(String name, String value) {
		distributor.addLiteralBindingName(name);
		ddContext.setParameter(name, value);
	}

	private void runAndAssertResult(ResultRow... rows)
			throws DataDistributorException, JSONException {
		distributor.init(ddContext);
		assertEqualJsonLists(jsonify(rows), jsonifyOutput(distributor));
	}

	private List<JSONObject> jsonify(ResultRow... rows) throws JSONException {
		List<JSONObject> list = new ArrayList<>();
		for (ResultRow row : rows) {
			list.add(row.toJson());
		}
		Collections.sort(list, new JSONObjectComparator());
		return list;
	}
	private List<JSONObject> jsonifyOutput(DataDistributor dd)
			throws DataDistributorException, JSONException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dd.writeOutput(out);
		JSONObject response = new JSONObject(out.toString());
		JSONObject results = (JSONObject) response.get("results");
		JSONArray bindings = (JSONArray) results.get("bindings");
		List<JSONObject> list = new ArrayList<>();
		for (int i = 0; i < bindings.length(); i++) {
			list.add((JSONObject) bindings.get(i));
		}
		Collections.sort(list, new JSONObjectComparator());
		return list;
	}

	private void assertEqualJsonLists(List<JSONObject> expectedJson,
			List<JSONObject> actualJson) {
		List<String> expectedList = listToSortedStrings(expectedJson);
		List<String> actualList = listToSortedStrings(actualJson);
		assertEquals(expectedList, actualList);
	}

	private List<String> listToSortedStrings(List<JSONObject> jsons) {
		ArrayList<String> strings = new ArrayList<>();
		for (JSONObject json : jsons) {
			strings.add(json.toString());
		}
		return strings;
	}

	private static class ResultRow {
		private final List<Binding> bindings;

		public ResultRow(Binding[] bindings) {
			this.bindings = new ArrayList<>(Arrays.asList(bindings));
		}

		public JSONObject toJson() throws JSONException {
			JSONObject json = new JSONObject();
			for (Binding binding : bindings) {
				json.put(binding.key, binding.toJson());
			}
			return json;
		}
	}

	private static abstract class Binding {
		final String key;
		final String value;

		public Binding(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public abstract JSONObject toJson() throws JSONException;
	}

	private static class UriBinding extends Binding {
		public UriBinding(String key, String value) {
			super(key, value);
		}

		@Override
		public JSONObject toJson() throws JSONException {
			JSONObject json = new JSONObject();
			json.put("type", "uri");
			json.put("value", value);
			return json;
		}
	}

	private static class LiteralBinding extends Binding {
		public LiteralBinding(String key, String value) {
			super(key, value);
		}

		@Override
		public JSONObject toJson() throws JSONException {
			JSONObject json = new JSONObject();
			json.put("type", "literal");
			json.put("value", value);
			return json;
		}
	}

	private static class JSONObjectComparator
			implements
				Comparator<JSONObject> {
		@Override
		public int compare(JSONObject o1, JSONObject o2) {
			return o1.toString().compareTo(o2.toString());
		}

	}
}
