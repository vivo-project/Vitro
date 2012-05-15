/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.bundles.experiment.bundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

/**
 * Just mess around a bit.
 */
public class ExperimentActivator implements BundleActivator {
	private static final Log log = LogFactory.getLog(ExperimentActivator.class);

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		String prop = bundleContext
				.getProperty(Constants.FRAMEWORK_STORAGE_CLEAN);
		log.warn("Framework storage clean property is: " + prop);
	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		// nothing to do
	}

}
