/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.services.shortview;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.services.shortview.FakeApplicationOntologyService.TemplateAndDataGetters;
import edu.cornell.mannlib.vitro.webapp.utils.developer.DeveloperSettings;
import edu.cornell.mannlib.vitro.webapp.utils.developer.Key;

/**
 * When we use a short view other than the default, log it.
 */
public class ShortViewLogger {
	private static final Log log = LogFactory.getLog(ShortViewLogger.class);

	public static void log(String contextName, Individual individual,
			String classUri, TemplateAndDataGetters tdg) {
		if (isLogging()) {
			log.info("Using custom short view in " + contextName + " because '"
					+ individual.getURI() + "' (" + individual.getLabel()
					+ ") has type '" + classUri + "': " + tdg);
		}
	}

	public static void log(String contextName, Individual individual) {
		if (isLogging()) {
			log.info("Using default short view in " + contextName + " for '"
					+ individual.getURI() + "' (" + individual.getLabel() + ")");
		}
	}

	private static boolean isLogging() {
		return log.isInfoEnabled()
				&& DeveloperSettings.getInstance().getBoolean(
						Key.PAGE_CONTENTS_LOG_CUSTOM_SHORT_VIEW);
	}

}
