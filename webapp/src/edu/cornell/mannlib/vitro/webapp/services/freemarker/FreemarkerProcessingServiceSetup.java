/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.services.freemarker;

import javax.servlet.ServletContext;

/**
 * This would be used to create and publish such a service, but for now we can
 * simply create a fresh copy every time someone asks for one.
 */
public class FreemarkerProcessingServiceSetup {
	public static FreemarkerProcessingService getService(ServletContext ctx) {
		return new FreemarkerProcessingServiceImpl();
	}
}
