/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.distribute.modelbuilder;

import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.dataProperty;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.model;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.modelToStrings;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.objectProperty;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import stubs.edu.cornell.mannlib.vitro.webapp.controller.api.distribute.DataDistributorContextStub;

public class ConstructModelBuilderTest extends AbstractTestClass {
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

	private Model model;
	private Model expectedResult;
	private ConstructModelBuilder builder;
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

		builder = new ConstructModelBuilder();
		builder.setRawConstructQuery(RAW_QUERY);
	}

	@After
	public void cleanup() {
		builder.close();
	}

	// ----------------------------------------------------------------------
	// The tests
	// ----------------------------------------------------------------------

	@Test
	public void noBinding() throws DataDistributorException {
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
		bindAsUri("author", AUTHOR1);
		runAndAssertResult(dataProperty(BOOK1, HAS_TITLE, TITLE1),
				dataProperty(BOOK2, HAS_TITLE, TITLE2),
				dataProperty(AUTHOR1, HAS_NAME, NAME1),
				objectProperty(BOOK1, HAS_AUTHOR, AUTHOR1),
				objectProperty(BOOK2, HAS_AUTHOR, AUTHOR1));
	}

	@Test
	public void bindBookUri() throws DataDistributorException {
		bindAsUri("book", BOOK2);
		runAndAssertResult(dataProperty(BOOK2, HAS_TITLE, TITLE2),
				dataProperty(AUTHOR1, HAS_NAME, NAME1),
				objectProperty(BOOK2, HAS_AUTHOR, AUTHOR1));
	}

	@Test
	public void bindAuthorAndBook() throws DataDistributorException {
		bindAsUri("author", AUTHOR1);
		bindAsUri("book", BOOK1);
		runAndAssertResult(dataProperty(BOOK1, HAS_TITLE, TITLE1),
				dataProperty(AUTHOR1, HAS_NAME, NAME1),
				objectProperty(BOOK1, HAS_AUTHOR, AUTHOR1));
	}

	@Test
	public void bindTitleLiteral() throws DataDistributorException {
		bindAsLiteral("title", TITLE2);
		runAndAssertResult(dataProperty(BOOK2, HAS_TITLE, TITLE2),
				dataProperty(AUTHOR1, HAS_NAME, NAME1),
				objectProperty(BOOK2, HAS_AUTHOR, AUTHOR1));
	}

	@Test
	public void bindNameLiteral() throws DataDistributorException {
		bindAsLiteral("name", NAME1);
		runAndAssertResult(dataProperty(BOOK1, HAS_TITLE, TITLE1),
				dataProperty(BOOK2, HAS_TITLE, TITLE2),
				dataProperty(AUTHOR1, HAS_NAME, NAME1),
				objectProperty(BOOK1, HAS_AUTHOR, AUTHOR1),
				objectProperty(BOOK2, HAS_AUTHOR, AUTHOR1));
	}

	@Test
	public void bindTitleAndName() throws DataDistributorException {
		bindAsLiteral("name", NAME1);
		bindAsLiteral("title", TITLE1);
		runAndAssertResult(dataProperty(BOOK1, HAS_TITLE, TITLE1),
				dataProperty(AUTHOR1, HAS_NAME, NAME1),
				objectProperty(BOOK1, HAS_AUTHOR, AUTHOR1));
	}

	// ----------------------------------------------------------------------
	// Helper methods and classes
	// ----------------------------------------------------------------------

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
		builder.init(ddContext);
		expectedResult = model(statements);
		assertEquals(modelToStrings(expectedResult),
				modelToStrings(builder.buildModel()));
	}

}
