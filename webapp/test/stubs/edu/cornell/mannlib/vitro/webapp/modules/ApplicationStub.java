/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.modules;

import java.lang.reflect.Field;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;

/**
 * TODO
 */
public class ApplicationStub implements Application {
	/**
	 * Create an ApplicationStub and set it as the instance in ApplicationUtils.
	 */
	public static void setup(ServletContext ctx, SearchEngine searchEngine) {
		ApplicationStub instance = new ApplicationStub(ctx, searchEngine);
		try {
			Field instanceField = ApplicationUtils.class
					.getDeclaredField("instance");
			instanceField.setAccessible(true);
			instanceField.set(null, instance);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private final ServletContext ctx;
	private final SearchEngine searchEngine;

	public ApplicationStub(ServletContext ctx, SearchEngine searchEngine) {
		this.ctx = ctx;
		this.searchEngine = searchEngine;
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public ServletContext getServletContext() {
		return ctx;
	}

	@Override
	public SearchEngine getSearchEngine() {
		return searchEngine;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------
}
