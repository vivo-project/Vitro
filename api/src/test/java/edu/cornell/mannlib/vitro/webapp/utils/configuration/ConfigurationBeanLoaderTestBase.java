/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.model;

import org.apache.jena.rdf.model.Model;
import org.junit.Before;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ModelAccessFactory;
import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccessFactoryStub;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpSessionStub;

/**
 * TODO
 */
public class ConfigurationBeanLoaderTestBase extends AbstractTestClass {
	protected static final String GENERIC_INSTANCE_URI = "http://mytest.edu/some_instance";
	protected static final String GENERIC_PROPERTY_URI = "http://mytest.edu/some_property";

	protected static final String SIMPLE_SUCCESS_INSTANCE_URI = "http://mytest.edu/simple_success_instance";

	protected static final String FULL_SUCCESS_INSTANCE_URI = "http://mytest.edu/full_success_instance";
	protected static final String FULL_SUCCESS_BOOST_PROPERTY = "http://mydomain.edu/hasBoost";
	protected static final String FULL_SUCCESS_TEXT_PROPERTY = "http://mydomain.edu/hasText";
	protected static final String FULL_SUCCESS_HELPER_PROPERTY = "http://mydomain.edu/hasHelper";
	protected static final String FULL_SUCCESS_HELPER_INSTANCE_URI = "http://mytest.edu/full_success_helper_instance";

	private ServletContextStub ctx;
	private HttpSessionStub session;
	private HttpServletRequestStub req;

	protected Model model;

	protected ConfigurationBeanLoader loader;
	protected ConfigurationBeanLoader noRequestLoader;
	protected ConfigurationBeanLoader noContextLoader;

	@Before
	public void setup() {
		ctx = new ServletContextStub();

		session = new HttpSessionStub();
		session.setServletContext(ctx);

		req = new HttpServletRequestStub();
		req.setSession(session);

		@SuppressWarnings("unused")
		ModelAccessFactory maf = new ModelAccessFactoryStub();

		model = model();

		loader = new ConfigurationBeanLoader(model, req);
		noRequestLoader = new ConfigurationBeanLoader(model, ctx);
		noContextLoader = new ConfigurationBeanLoader(model);
	}

	// ----------------------------------------------------------------------
	// Helper methods for simple failure
	// ----------------------------------------------------------------------

	protected void expectSimpleFailure(Class<?> failureClass,
			ExpectedThrowable expected, ExpectedThrowable cause)
			throws ConfigurationBeanLoaderException {
		expectException(expected.getClazz(), expected.getMessageSubstring(),
				cause.getClazz(), cause.getMessageSubstring());

		@SuppressWarnings("unused")
		Object unused = loader.loadInstance(GENERIC_INSTANCE_URI, failureClass);
	}

	protected ExpectedThrowable throwable(Class<? extends Throwable> clazz,
			String messageSubstring) {
		return new ExpectedThrowable(clazz, messageSubstring);
	}

	private static class ExpectedThrowable {
		private final Class<? extends Throwable> clazz;
		private final String messageSubstring;

		public ExpectedThrowable(Class<? extends Throwable> clazz,
				String messageSubstring) {
			this.clazz = clazz;
			this.messageSubstring = messageSubstring;
		}

		public Class<? extends Throwable> getClazz() {
			return clazz;
		}

		public String getMessageSubstring() {
			return messageSubstring;
		}
	}

}
