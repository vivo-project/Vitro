/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner;

import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.dataProperty;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.model;
import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createSelectQueryContext;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

public class StringResultsMappingTest extends AbstractTestClass {
	private static final String SUBJECT_URI_1 = "http://namespace/subject_uri_1";
	private static final String PREDICATE_URI_1 = "http://namespace/predicate_uri_1";
	private static final String OBJECT_VALUE_1 = "object_value_1";
	private static final String SUBJECT_URI_2 = "http://namespace/subject_uri_2";
	private static final String PREDICATE_URI_2 = "http://namespace/predicate_uri_2";
	private static final String OBJECT_VALUE_2 = "object_value_2";

	private static final String SELECT_QUERY = "SELECT ?s ?p ?o WHERE { ?s ?p ?o . }";

	private static final List<Map<String, String>> LIST_ALL_FIELDS = list(
			map(entry("s", SUBJECT_URI_1), entry("p", PREDICATE_URI_1),
					entry("o", OBJECT_VALUE_1)),
			map(entry("s", SUBJECT_URI_2), entry("p", PREDICATE_URI_2),
					entry("o", OBJECT_VALUE_2)));
	private static final List<Map<String, String>> LIST_ONE_FIELD = list(
			map(entry("o", OBJECT_VALUE_1)), map(entry("o", OBJECT_VALUE_2)));

	private static final List<String> FLATTEN_ALL_FIELDS = list(SUBJECT_URI_1,
			PREDICATE_URI_1, OBJECT_VALUE_1, SUBJECT_URI_2, PREDICATE_URI_2,
			OBJECT_VALUE_2);
	private static final List<String> FLATTEN_ONE_FIELD = list(OBJECT_VALUE_1,
			OBJECT_VALUE_2);

	private Model model;

	@Before
	public void setup() {
		model = model(
				dataProperty(SUBJECT_URI_1, PREDICATE_URI_1, OBJECT_VALUE_1),
				dataProperty(SUBJECT_URI_2, PREDICATE_URI_2, OBJECT_VALUE_2));
	}

	@Test
	public void checkMapsOfAllFields() {
		assertEquivalentUnorderedLists(LIST_ALL_FIELDS,
				createSelectQueryContext(model, SELECT_QUERY).execute()
						.toStringFields().getListOfMaps());
	}

	@Test
	public void checkMapsOfOneField() {
		assertEquivalentUnorderedLists(LIST_ONE_FIELD,
				createSelectQueryContext(model, SELECT_QUERY).execute()
						.toStringFields("o").getListOfMaps());
	}

	@Test
	public void checkFlattenAllFields() {
		assertEquivalentUnorderedLists(FLATTEN_ALL_FIELDS,
				createSelectQueryContext(model, SELECT_QUERY).execute()
						.toStringFields().flatten());
	}

	@Test
	public void checkFlattenOneField() {
		assertEquivalentUnorderedLists(FLATTEN_ONE_FIELD,
				createSelectQueryContext(model, SELECT_QUERY).execute()
						.toStringFields("o").flatten());
	}

	@Test
	public void checkFlattenToSet() {
		assertEquals(new HashSet<>(FLATTEN_ONE_FIELD),
				createSelectQueryContext(model, SELECT_QUERY).execute()
						.toStringFields("o").flattenToSet());
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private <T> void assertEquivalentUnorderedLists(List<T> list1, List<T> list2) {
		Collections.sort(list1, new ArbitraryOrder<>());
		Collections.sort(list2, new ArbitraryOrder<>());
		assertEquals(list1, list2);
	}

	private static class ArbitraryOrder<T> implements Comparator<T> {
		@Override
		public int compare(T t1, T t2) {
			return t1.hashCode() - t2.hashCode();
		}
	}

	@SafeVarargs
	private static <T> List<T> list(T... items) {
		List<T> l = new ArrayList<>();
		for (T item : items) {
			l.add(item);
		}
		return l;
	}

	private static Map<String, String> map(Entry... entries) {
		Map<String, String> m = new HashMap<>();
		for (Entry entry : entries) {
			m.put(entry.key, entry.value);
		}
		return m;
	}

	private static Entry entry(String key, String value) {
		return new Entry(key, value);
	}

	private static class Entry {
		final String key;
		final String value;

		Entry(String key, String value) {
			this.key = key;
			this.value = value;
		}
	}

}
