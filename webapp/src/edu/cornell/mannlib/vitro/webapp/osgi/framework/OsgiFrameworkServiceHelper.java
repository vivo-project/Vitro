/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.framework;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.framework.Felix;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

/**
 * Just some simple utilities the help report on the status of the framework.
 */
public class OsgiFrameworkServiceHelper {
	private final Felix framework;
	private static final Log log = LogFactory
			.getLog(OsgiFrameworkServiceHelper.class);

	public OsgiFrameworkServiceHelper(Felix framework) {
		if (framework == null) {
			log.warn("Felix framework is null.");
		}
		this.framework = framework;
	}

	/**
	 * Get a list of all of the class names of all of the services available
	 * from the framework. (Note that a service may be available as more than
	 * one class.)
	 */
	public List<String> listAvailableServiceClasses() {
		List<String> list = new ArrayList<String>();
		if (framework == null) {
			return list;
		}

		BundleContext bc = framework.getBundleContext();
		if (bc == null) {
			log.warn("BundleContext for the system bundle is null.");
			return list;
		}

		try {
			ServiceReference<?>[] all = bc.getAllServiceReferences(null, null);
			for (ServiceReference<?> sr : all) {
				Object serviceClasses = sr.getProperty(Constants.OBJECTCLASS);
				if (serviceClasses instanceof String[]) {
					for (String sc : (String[]) serviceClasses) {
						list.add(sc);
					}
				}
			}
			return list;
		} catch (Exception e) {
			log.warn("asking about available services, "
					+ "but threw an exception: " + e);
			return list;
		}
	}
}
