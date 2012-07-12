/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.services.shortview;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.services.shortview.FakeApplicationOntologyService.ShortViewConfigException;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Set up the ShortViewService.
 */
public class ShortViewServiceSetup implements ServletContextListener {
	private static final String ATTRIBUTE_NAME = ShortViewService.class
			.getName();

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);

		FakeApplicationOntologyService faker;
		try {
			faker = new FakeApplicationOntologyService(ctx);
		} catch (ShortViewConfigException e) {
			ss.warning(this, "Failed to load the shortview_config.n3 file -- "
					+ e.getMessage(), e);
			faker = new FakeApplicationOntologyService();
		}

		ShortViewServiceImpl svs = new ShortViewServiceImpl(faker);
		ctx.setAttribute(ATTRIBUTE_NAME, svs);

		ss.info(this,
				"Started the Short View Service with a ShortViewServiceImpl");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		sce.getServletContext().removeAttribute(ATTRIBUTE_NAME);
	}

	public static ShortViewService getService(ServletContext ctx) {
		return (ShortViewService) ctx.getAttribute(ATTRIBUTE_NAME);
	}
}
