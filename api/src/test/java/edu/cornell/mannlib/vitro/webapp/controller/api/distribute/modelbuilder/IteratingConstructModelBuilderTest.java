/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.distribute.modelbuilder;

import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.model;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.modelToStrings;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.typeStatement;
import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;
import stubs.edu.cornell.mannlib.vitro.webapp.controller.api.distribute.DataDistributorContextStub;

/**
 * Show that we can iterate through one iterator, or through the cross-product
 * of two iterators. If no iterators, we do nothing.
 */
public class IteratingConstructModelBuilderTest extends AbstractTestClass {
	private static final String NS = "http://this.name/space#";
	private static final String I1 = NS + "instance1";
	private static final String I2 = NS + "instance2";
	private static final String I3 = NS + "instance3";
	private static final String I4 = NS + "instance4";
	private static final String T1 = NS + "type1";
	private static final String T2 = NS + "type2";
	private static final String T3 = NS + "type3";
	private static final String T4 = NS + "type4";
	private static final String RAW_QUERY = ""
			+ "PREFIX ns: <http://this.name/space#> \n " //
			+ "CONSTRUCT { \n " //
			+ "  ?instance a ?type . \n " //
			+ "} WHERE { \n " //
			+ "  ?instance a ?type . \n " //
			+ "}";

	private Model model;
	private Model expectedResult;
	private IteratingConstructModelBuilder builder;
	private DataDistributorContextStub ddContext;

	@Before
	public void setup() {
		model = model(typeStatement(I1, T1), typeStatement(I1, T2),
				typeStatement(I1, T3), typeStatement(I1, T4),
				typeStatement(I2, T1), typeStatement(I2, T2),
				typeStatement(I2, T3), typeStatement(I3, T1),
				typeStatement(I3, T2), typeStatement(I4, T1));

		ddContext = new DataDistributorContextStub(model);

		builder = new IteratingConstructModelBuilder();
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
	public void noIterators() throws DataDistributorException {
		runAndAssertResult();
	}

	@Test
	public void oneValue()
			throws DataDistributorException, ConfigurationBeanLoaderException {
		iterator("instance", I3);
		runAndAssertResult(typeStatement(I3, T1), typeStatement(I3, T2));
	}

	@Test
	public void twoValues()
			throws DataDistributorException, ConfigurationBeanLoaderException {
		iterator("instance", I3, I4);
		runAndAssertResult(typeStatement(I3, T1), typeStatement(I3, T2),
				typeStatement(I4, T1));
	}

	@Test
	public void oneValueAndOneValue()
			throws DataDistributorException, ConfigurationBeanLoaderException {
		iterator("instance", I1);
		iterator("type", T1);
		runAndAssertResult(typeStatement(I1, T1));
	}
	
	@Test
	public void twoValuesAndThreeValues()
			throws DataDistributorException, ConfigurationBeanLoaderException {
		iterator("instance", I1, I2);
		iterator("type", T1, T2, T3);
		runAndAssertResult(typeStatement(I1, T1), typeStatement(I1, T2),
				typeStatement(I1, T3), typeStatement(I2, T1),
				typeStatement(I2, T2), typeStatement(I2, T3));
	}

	// ----------------------------------------------------------------------
	// Helper methods and classes
	// ----------------------------------------------------------------------

	private void iterator(String varName, String... values)
			throws ConfigurationBeanLoaderException {
		builder.addIterator(varName + "=" + StringUtils.join(values, ","));
	}

	private void runAndAssertResult(Statement... statements)
			throws DataDistributorException {
		builder.init(ddContext);
		expectedResult = model(statements);
		assertEquals(modelToStrings(expectedResult),
				modelToStrings(builder.buildModel()));
	}

}
