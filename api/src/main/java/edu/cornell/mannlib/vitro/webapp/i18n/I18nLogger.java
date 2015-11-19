/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.i18n;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.utils.developer.DeveloperSettings;
import edu.cornell.mannlib.vitro.webapp.utils.developer.Key;

/**
 * If enabled in developer mode, write a message to the log each time someone
 * asks for a language string.
 * 
 * The I18nBundle has a life span of one HTTP request, and so does this.
 */
public class I18nLogger {
	private static final Log log = LogFactory.getLog(I18nLogger.class);

	private final boolean isLogging;

	public I18nLogger() {
		DeveloperSettings settings = DeveloperSettings.getInstance();
		this.isLogging = settings.getBoolean(Key.I18N_LOG_STRINGS)
				&& log.isInfoEnabled();
	}

	public void log(String bundleName, String key, Object[] parameters,
			String rawText, String formattedText) {
		if (isLogging) {
			String message = String.format(
					"Retrieved from %s.%s with %s: '%s'", bundleName, key,
					Arrays.toString(parameters), rawText);

			if (!rawText.equals(formattedText)) {
				message += String.format(" --> '%s'", formattedText);
			}

			log.info(message);
		}
	}
}
