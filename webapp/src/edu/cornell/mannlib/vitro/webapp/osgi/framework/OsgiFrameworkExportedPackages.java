/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * The packages that must be exported for OSGi bundles to use. If a package is
 * needed in the base of the application as well as in the bundles, it must be
 * included here.
 * 
 * Perhaps these packages could be made into bundles themselves, but then they
 * would not be usable by the base of the application (the non-OSGi part), since
 * they would be loaded by a different class loader.
 * 
 * This results from the fact that we are straddling the line between OSGi and
 * non-OSGi.
 */
public class OsgiFrameworkExportedPackages {
	/**
	 * Combine all of the shared package specifications into a String,
	 * compatible withthe OSGi framework property:
	 * "org.osgi.framework.system.packages.extra"
	 */
	public static String getPackageList() {
		List<String> packageSpecs = new ArrayList<String>();
		packageSpecs.addAll(packageSpecsForVitro());
		packageSpecs.addAll(packageSpecsForApacheCommonsLogging());
		packageSpecs.addAll(packageSpecsForBndAnnotations());
		packageSpecs.addAll(packageSpecsForServletApiJar());
		packageSpecs.addAll(packageSpecsForOsgiCompServices());
		return StringUtils.join(packageSpecs, ",");
	}

	/**
	 * These are the Vitro interfaces that will be implemented by the
	 * application bundles, and the classes they may build from.
	 */
	private static Collection<? extends String> packageSpecsForVitro() {
		return Arrays
				.asList("edu.cornell.mannlib.vitro.webapp.modules.interfaces");
	}

	/**
	 * Interface package from commons-logging-1.1.1.jar
	 * 
	 * We want the application bundles to have just as much logging flexibility
	 * as the application base has.
	 */
	private static Collection<? extends String> packageSpecsForApacheCommonsLogging() {
		return Arrays.asList("org.apache.commons.logging");
	}

	/**
	 * Packages from biz.aQute.bnd.jar
	 * 
	 * Would it be better if we published biz.aQute.bndlib.jar as a bundle? But
	 * it has all sorts of scary "uses" clauses that we would need to satisfy.
	 * 
	 * TODO figure this out.
	 */
	private static Collection<? extends String> packageSpecsForBndAnnotations() {
		return Arrays.asList("aQute.bnd.annotation.component");
	}

	/**
	 * Do we really need to export the packages from servlet-api.jar?
	 * 
	 * TODO figure this out.
	 */
	private static Collection<? extends String> packageSpecsForServletApiJar() {
		return Arrays.asList("javax.servlet;version=2.4",
				"javax.servlet.http;version=2.4");
	}

	/**
	 * Packages from osgi.cmpn.jar
	 * 
	 * We can't simply include this as a bundle because we use it in the base of
	 * the application. Specifically, in OsgiFrameworkLogger.
	 * 
	 * TODO If the OsgiFrameworkLogger did not straddle the line, then perhaps
	 * we would not need to export these packages. TODO But the HttpService
	 * implementation that uses osg.osgi.service.http
	 */
	private static List<String> packageSpecsForOsgiCompServices() {
		return Arrays.asList(
				"info.dmtree;specification-version=1.0",
				"info.dmtree.notification;version=1.0",
				"info.dmtree.notification.spi;version=1.0",
				"info.dmtree.registry;version=1.0",
				"info.dmtree.security;version=1.0",
				"info.dmtree.spi;version=1.0",
				"org.osgi.application;version=1.0",
				"org.osgi.service.application;version=1.0",
				// exported by ConfigurationAdmin bundle
				// "org.osgi.service.cm;version=1.2",
				// exported by Declarative Services bundle
				// "org.osgi.service.component;version=1.0",
				"org.osgi.service.deploymentadmin;version=1.0",
				"org.osgi.service.deploymentadmin.spi;version=1.0",
				"org.osgi.service.device;version=1.1",
				"org.osgi.service.event;version=1.1",
				"org.osgi.service.http;version=1.2",
				"org.osgi.service.io;version=1.0",
				"org.osgi.service.log;version=1.3",
				"org.osgi.service.metatype;version=1.1",
				"org.osgi.service.monitor;version=1.0",
				"org.osgi.service.prefs;version=1.1",
				"org.osgi.service.provisioning;version=1.1",
				"org.osgi.service.upnp;version=1.1",
				"org.osgi.service.useradmin;version=1.1",
				"org.osgi.service.wireadmin;version=1.0",
				"org.osgi.util.gsm;version=1.0",
				"org.osgi.util.measurement;version=1.0",
				"org.osgi.util.mobile;version=1.0",
				"org.osgi.util.position;version=1.0",
				"org.osgi.util.tracker;version=1.3.3",
				"org.osgi.util.xml;version=1.0");
	}
}
