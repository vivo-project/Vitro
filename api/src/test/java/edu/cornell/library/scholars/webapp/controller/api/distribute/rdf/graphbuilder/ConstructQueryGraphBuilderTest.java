/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder;

import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.dataProperty;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.model;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.modelToStrings;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.objectProperty;
import static org.junit.Assert.assertEquals;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import stubs.edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContextStub;

/**
 * TODO
 */
public class ConstructQueryGraphBuilderTest extends AbstractTestClass {
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
            + "CONSTRUCT { \n " //
            + "  ?book ns:hasAuthor ?author . \n " //
            + "  ?book ns:hasTitle ?title . \n " //
            + "  ?author ns:hasName ?name . \n " //
            + "} WHERE { \n " //
            + "  ?book ns:hasAuthor ?author . \n " //
            + "  ?book ns:hasTitle ?title . \n " //
            + "  ?author ns:hasName ?name . \n " //
            + "}";
    private static final String MULTI_QUERY_1 = ""
            + "PREFIX ns: <http://this.name/space#> \n " //
            + "CONSTRUCT { \n " //
            + "  ?book ns:hasAuthor ?author . \n " //
            + "  ?book ns:hasTitle ?title . \n " //
            + "} WHERE { \n " //
            + "  ?book ns:hasAuthor ?author . \n " //
            + "  ?book ns:hasTitle ?title . \n " //
            + "}";
    private static final String MULTI_QUERY_2 = ""
            + "PREFIX ns: <http://this.name/space#> \n " //
            + "CONSTRUCT { \n " //
            + "  ?book ns:hasAuthor ?author . \n " //
            + "  ?author ns:hasName ?name . \n " //
            + "} WHERE { \n " //
            + "  ?book ns:hasAuthor ?author . \n " //
            + "  ?author ns:hasName ?name . \n " //
            + "}";

    private Model graph;
    private Model expectedResult;
    private Model actualResult;
    private ConstructQueryGraphBuilder builder;
    private DataDistributorContextStub ddContext;

    @Before
    public void setup() {
        graph = model(dataProperty(BOOK1, HAS_TITLE, TITLE1),
                dataProperty(BOOK2, HAS_TITLE, TITLE2),
                dataProperty(AUTHOR1, HAS_NAME, NAME1),
                dataProperty(AUTHOR2, HAS_NAME, NAME2),
                objectProperty(BOOK1, HAS_AUTHOR, AUTHOR1),
                objectProperty(BOOK1, HAS_AUTHOR, AUTHOR2),
                objectProperty(BOOK2, HAS_AUTHOR, AUTHOR1));

        ddContext = new DataDistributorContextStub(graph);

        builder = new ConstructQueryGraphBuilder();
    }

    // ----------------------------------------------------------------------
    // The tests
    // ----------------------------------------------------------------------

    @Test
    public void noBinding() throws DataDistributorException {
        setQueries(RAW_QUERY);
        runAndAssertResult(dataProperty(BOOK1, HAS_TITLE, TITLE1),
                dataProperty(BOOK2, HAS_TITLE, TITLE2),
                dataProperty(AUTHOR1, HAS_NAME, NAME1),
                dataProperty(AUTHOR2, HAS_NAME, NAME2),
                objectProperty(BOOK1, HAS_AUTHOR, AUTHOR1),
                objectProperty(BOOK1, HAS_AUTHOR, AUTHOR2),
                objectProperty(BOOK2, HAS_AUTHOR, AUTHOR1));
    }

    @Test
    public void bindAuthorUri() throws DataDistributorException {
        setQueries(RAW_QUERY);
        bindAsUri("author", AUTHOR1);
        runAndAssertResult(dataProperty(BOOK1, HAS_TITLE, TITLE1),
                dataProperty(BOOK2, HAS_TITLE, TITLE2),
                dataProperty(AUTHOR1, HAS_NAME, NAME1),
                objectProperty(BOOK1, HAS_AUTHOR, AUTHOR1),
                objectProperty(BOOK2, HAS_AUTHOR, AUTHOR1));
    }

    @Test
    public void bindBookUri() throws DataDistributorException {
        setQueries(RAW_QUERY);
        bindAsUri("book", BOOK2);
        runAndAssertResult(dataProperty(BOOK2, HAS_TITLE, TITLE2),
                dataProperty(AUTHOR1, HAS_NAME, NAME1),
                objectProperty(BOOK2, HAS_AUTHOR, AUTHOR1));
    }

    @Test
    public void bindAuthorAndBook() throws DataDistributorException {
        setQueries(RAW_QUERY);
        bindAsUri("author", AUTHOR1);
        bindAsUri("book", BOOK1);
        runAndAssertResult(dataProperty(BOOK1, HAS_TITLE, TITLE1),
                dataProperty(AUTHOR1, HAS_NAME, NAME1),
                objectProperty(BOOK1, HAS_AUTHOR, AUTHOR1));
    }

    @Test
    public void bindTitleLiteral() throws DataDistributorException {
        setQueries(RAW_QUERY);
        bindAsLiteral("title", TITLE2);
        runAndAssertResult(dataProperty(BOOK2, HAS_TITLE, TITLE2),
                dataProperty(AUTHOR1, HAS_NAME, NAME1),
                objectProperty(BOOK2, HAS_AUTHOR, AUTHOR1));
    }

    @Test
    public void bindNameLiteral() throws DataDistributorException {
        setQueries(RAW_QUERY);
        bindAsLiteral("name", NAME1);
        runAndAssertResult(dataProperty(BOOK1, HAS_TITLE, TITLE1),
                dataProperty(BOOK2, HAS_TITLE, TITLE2),
                dataProperty(AUTHOR1, HAS_NAME, NAME1),
                objectProperty(BOOK1, HAS_AUTHOR, AUTHOR1),
                objectProperty(BOOK2, HAS_AUTHOR, AUTHOR1));
    }

    @Test
    public void bindTitleAndName() throws DataDistributorException {
        setQueries(RAW_QUERY);
        bindAsLiteral("name", NAME1);
        bindAsLiteral("title", TITLE1);
        runAndAssertResult(dataProperty(BOOK1, HAS_TITLE, TITLE1),
                dataProperty(AUTHOR1, HAS_NAME, NAME1),
                objectProperty(BOOK1, HAS_AUTHOR, AUTHOR1));
    }
    
    @Test
    public void bindBookUriWithMultipleQueries() throws DataDistributorException {
        setQueries(MULTI_QUERY_1, MULTI_QUERY_2);
        bindAsUri("book", BOOK2);
        runAndAssertResult(dataProperty(BOOK2, HAS_TITLE, TITLE2),
                dataProperty(AUTHOR1, HAS_NAME, NAME1),
                objectProperty(BOOK2, HAS_AUTHOR, AUTHOR1));
    }

    // ----------------------------------------------------------------------
    // Helper methods and classes
    // ----------------------------------------------------------------------

    private void setQueries(String... queries) {
        for (String query : queries) {
            builder.addRawQuery(query);
        }
    }

    private void bindAsUri(String name, String uri) {
        builder.addUriBindingName(name);
        ddContext.setParameter(name, uri);
    }

    private void bindAsLiteral(String name, String value) {
        builder.addLiteralBindingName(name);
        ddContext.setParameter(name, value);
    }

    private void runAndAssertResult(Statement... statements)
            throws DataDistributorException {
        expectedResult = model(statements);
        actualResult = builder.buildGraph(ddContext);
        assertEquals(modelToStrings(expectedResult),
                modelToStrings(actualResult));
    }

}
