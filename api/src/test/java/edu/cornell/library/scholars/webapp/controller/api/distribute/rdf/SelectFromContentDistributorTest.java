/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf;

import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.dataProperty;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.model;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.objectProperty;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import stubs.edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContextStub;

/**
 * Show that this works with an assortment of variable bindings, or with none at
 * all.
 */
public class SelectFromContentDistributorTest extends AbstractTestClass {
    protected static final String NS = "http://this.name/space#";
    protected static final String BOOK1 = NS + "book1";
    protected static final String BOOK2 = NS + "book2";
    protected static final String AUTHOR1 = NS + "author1";
    protected static final String AUTHOR2 = NS + "author2";
    protected static final String TITLE1 = NS + "title1";
    protected static final String TITLE2 = NS + "title2";
    protected static final String NAME1 = NS + "name1";
    protected static final String NAME2 = NS + "name2";
    protected static final String HAS_AUTHOR = NS + "hasAuthor";
    protected static final String HAS_TITLE = NS + "hasTitle";
    protected static final String HAS_NAME = NS + "hasName";
    protected static final String RAW_QUERY = ""
            + "PREFIX ns: <http://this.name/space#> \n " //
            + "SELECT ?book ?author ?title ?name \n " //
            + "WHERE { \n " //
            + "  ?book ns:hasAuthor ?author . \n " //
            + "  ?book ns:hasTitle ?title . \n " //
            + "  ?author ns:hasName ?name . \n " //
            + "}";

    protected DataDistributor distributor;
    protected DataDistributorContextStub ddContext;

    @Before
    public void setup() {
        Model model = model(dataProperty(BOOK1, HAS_TITLE, TITLE1),
                dataProperty(BOOK2, HAS_TITLE, TITLE2),
                dataProperty(AUTHOR1, HAS_NAME, NAME1),
                dataProperty(AUTHOR2, HAS_NAME, NAME2),
                objectProperty(BOOK1, HAS_AUTHOR, AUTHOR1),
                objectProperty(BOOK1, HAS_AUTHOR, AUTHOR2),
                objectProperty(BOOK2, HAS_AUTHOR, AUTHOR1));

        ddContext = new DataDistributorContextStub(model);

        distributor = new SelectFromContentDistributor();
        ((SelectFromContentDistributor) distributor).setRawQuery(RAW_QUERY);
    }

    @After
    public void cleanup() throws DataDistributorException {
        distributor.close();
    }

    // ----------------------------------------------------------------------
    // The tests
    // ----------------------------------------------------------------------

    @Test
    public void noBinding() throws DataDistributorException {
        runAndAssertResult(
                row(uri("book", BOOK1), uri("author", AUTHOR1),
                        literal("title", TITLE1), literal("name", NAME1)),
                row(uri("book", BOOK2), uri("author", AUTHOR1),
                        literal("title", TITLE2), literal("name", NAME1)),
                row(uri("book", BOOK1), uri("author", AUTHOR2),
                        literal("title", TITLE1), literal("name", NAME2)));
    }

    @Test
    public void bindAuthorUri() throws DataDistributorException {
        bindAsUri("author", AUTHOR1);
        runAndAssertResult(
                row(uri("book", BOOK1), literal("title", TITLE1),
                        literal("name", NAME1)),
                row(uri("book", BOOK2), literal("title", TITLE2),
                        literal("name", NAME1)));
    }

    @Test
    public void bindBookUri() throws DataDistributorException {
        bindAsUri("book", BOOK2);
        runAndAssertResult(row(uri("author", AUTHOR1), literal("title", TITLE2),
                literal("name", NAME1)));
    }

    @Test
    public void bindAuthorAndBook() throws DataDistributorException {
        bindAsUri("author", AUTHOR1);
        bindAsUri("book", BOOK1);
        runAndAssertResult(
                row(literal("title", TITLE1), literal("name", NAME1)));
    }

    @Test
    public void bindTitleLiteral() throws DataDistributorException {
        bindAsLiteral("title", TITLE2);
        runAndAssertResult(row(uri("book", BOOK2), uri("author", AUTHOR1),
                literal("name", NAME1)));
    }

    @Test
    public void bindNameLiteral() throws DataDistributorException {
        bindAsLiteral("name", NAME1);
        runAndAssertResult(
                row(uri("book", BOOK1), uri("author", AUTHOR1),
                        literal("title", TITLE1)),
                row(uri("book", BOOK2), uri("author", AUTHOR1),
                        literal("title", TITLE2)));
    }

    @Test
    public void bindTitleAndName() throws DataDistributorException {
        bindAsLiteral("name", NAME1);
        bindAsLiteral("title", TITLE1);
        runAndAssertResult(row(uri("book", BOOK1), uri("author", AUTHOR1)));
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
        ((AbstractSparqlBindingDistributor) distributor)
                .addUriBindingName(name);
        ddContext.setParameter(name, uri);
    }

    private void bindAsLiteral(String name, String value) {
        ((AbstractSparqlBindingDistributor) distributor)
                .addLiteralBindingName(name);
        ddContext.setParameter(name, value);
    }

    private void runAndAssertResult(ResultRow... rows)
            throws DataDistributorException {
        distributor.init(ddContext);
        assertEqualJsonLists(jsonify(rows), jsonifyOutput(distributor));
    }

    private List<ObjectNode> jsonify(ResultRow... rows) {
        List<ObjectNode> list = new ArrayList<>();
        for (ResultRow row : rows) {
            list.add(row.toJson());
        }
        Collections.sort(list, new ObjectNodeComparator());
        return list;
    }

    private List<ObjectNode> jsonifyOutput(DataDistributor dd)
            throws DataDistributorException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            dd.writeOutput(out);
            ObjectNode response = (ObjectNode) new ObjectMapper()
                    .readTree(out.toString());
            ObjectNode results = (ObjectNode) response.get("results");
            ArrayNode bindings = (ArrayNode) results.get("bindings");
            List<ObjectNode> list = new ArrayList<>();
            for (int i = 0; i < bindings.size(); i++) {
                list.add((ObjectNode) bindings.get(i));
            }
            Collections.sort(list, new ObjectNodeComparator());
            return list;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertEqualJsonLists(List<ObjectNode> expectedJson,
            List<ObjectNode> actualJson) {
        List<String> expectedList = listToSortedStrings(expectedJson);
        List<String> actualList = listToSortedStrings(actualJson);
        assertEquals(expectedList, actualList);
    }

    private List<String> listToSortedStrings(List<ObjectNode> jsons) {
        ArrayList<String> strings = new ArrayList<>();
        for (ObjectNode json : jsons) {
            strings.add(json.toString());
        }
        return strings;
    }

    private static class ResultRow {
        private final List<Binding> bindings;

        public ResultRow(Binding[] bindings) {
            this.bindings = new ArrayList<>(Arrays.asList(bindings));
        }

        public ObjectNode toJson() {
            ObjectNode json = JsonNodeFactory.instance.objectNode();
            for (Binding binding : bindings) {
                json.set(binding.key, binding.toJson());
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

        public abstract ObjectNode toJson();
    }

    private static class UriBinding extends Binding {
        public UriBinding(String key, String value) {
            super(key, value);
        }

        @Override
        public ObjectNode toJson() {
            ObjectNode json = JsonNodeFactory.instance.objectNode();
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
        public ObjectNode toJson() {
            ObjectNode json = JsonNodeFactory.instance.objectNode();
            json.put("type", "literal");
            json.put("value", value);
            return json;
        }
    }

    private static class ObjectNodeComparator
            implements Comparator<ObjectNode> {
        @Override
        public int compare(ObjectNode o1, ObjectNode o2) {
            return o1.toString().compareTo(o2.toString());
        }

    }
}
