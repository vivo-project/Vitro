/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.i18n;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.utils.developer.DeveloperSettings;
import edu.cornell.mannlib.vitro.webapp.utils.developer.Key;

/**
 * If enabled in developer mode, write a message to the log each time someone
 * asks for a language string.
 */
public class I18nLogger {
	private static final Log log = LogFactory.getLog(I18nLogger.class);
	private DeveloperSettings settings;

	public I18nLogger() {
		settings = DeveloperSettings.getInstance();
	}

	public void log(String key, Object[] parameters, String rawText, String formattedText) {
		if (isI18nLoggingTurnedOn()) {
			String message = String.format(
					"Retrieved from %s with %s: '%s'", key,
					Arrays.toString(parameters), rawText);

			if (!rawText.equals(formattedText)) {
				message += String.format(" --> '%s'", formattedText);
			}
			log.info(message);
		}
	}

	private boolean isI18nLoggingTurnedOn() {
		return settings.getBoolean(Key.I18N_LOG_STRINGS) && log.isInfoEnabled();
	}
}
