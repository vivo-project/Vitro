/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.smoketests;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;

import edu.cornell.mannlib.vitro.webapp.modules.interfaces.FileStorage;
import edu.cornell.mannlib.vitro.webapp.osgi.framework.OsgiFramework;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Confirm that the expected modules are available from the OSGi framework.
 * 
 * Check all of them, so more than one error message, if appropriate.
 */
public class OsgiModulesSmokeTest implements ServletContextListener {
	private static final List<Class<?>> EXPECTED_MODULE_CLASSES = Collections
			.unmodifiableList(Arrays.<Class<?>> asList(FileStorage.class));

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);

		List<String> classNames = OsgiFramework.getFramework(ctx)
				.listAvailableServiceClasses();

		reportAvailableServices(classNames, ss);

		try {
			for (Class<?> moduleClass : EXPECTED_MODULE_CLASSES) {
				checkIfModuleIsAvailable(moduleClass, classNames, ss);
			}
		} catch (Exception e) {
			ss.fatal(this, "Unexpected failure during smoke test", e);
		}
	}

	private void reportAvailableServices(List<String> classNames,
			StartupStatus ss) {
		String classesString = StringUtils.join(classNames, ", ");
		ss.info(this, "Found OSGi services for these classes: " + classesString);
	}

	private void checkIfModuleIsAvailable(Class<?> moduleClass,
			List<String> classNames, StartupStatus ss) {
		for (String className : classNames) {
			if (moduleClass.getName().equals(className)) {
				return;
			}
		}
		ss.fatal(this,
				"The OSGi framework does not provide this Vitro module: "
						+ moduleClass.getName());
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// nothing to do at shutdown
	}

}
