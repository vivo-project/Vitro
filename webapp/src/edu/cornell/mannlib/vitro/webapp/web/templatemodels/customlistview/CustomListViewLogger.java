/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.customlistview;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.utils.developer.DeveloperSettings;
import edu.cornell.mannlib.vitro.webapp.utils.developer.DeveloperSettings.Keys;

/**
 * If enabled in the developer settings (and log levels), log every non-default
 * custom list view.
 */
public class CustomListViewLogger {
	private static final Log log = LogFactory
			.getLog(CustomListViewLogger.class);

	public static void log(VitroRequest vreq, ObjectProperty op,
			String configFileName) {
		if (isLogging(vreq)) {
			log.info("Using list view: '" + configFileName + "' for "
					+ op.getURI() + " (" + op.getLabel() + ")");

		}
	}

	private static boolean isLogging(VitroRequest vreq) {
		if (!log.isInfoEnabled()) {
			return false;
		}
		DeveloperSettings settings = DeveloperSettings.getBean(vreq);
		return settings.getBoolean(Keys.ENABLED)
				&& settings.getBoolean(Keys.PAGE_CONTENTS_LOG_CUSTOM_LIST_VIEW);
	}
}
