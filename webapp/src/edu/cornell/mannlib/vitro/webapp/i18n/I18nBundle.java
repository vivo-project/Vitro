/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.i18n;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A wrapper for a ResourceBundle that will not throw an exception, no matter
 * what string you request.
 * 
 * If the ResourceBundle was not found, or if it doesn't contain the requested
 * key, an error message string is returned, to help the developer diagnose the
 * problem.
 */
public class I18nBundle {
	private static final Log log = LogFactory.getLog(I18nBundle.class);

	private static final String MESSAGE_BUNDLE_NOT_FOUND = "Text bundle ''{0}'' not found.";
	private static final String MESSAGE_KEY_NOT_FOUND = "Text bundle ''{0}'' has no text for ''{1}''";

	public static I18nBundle emptyBundle(String bundleName,
			I18nLogger i18nLogger) {
		return new I18nBundle(bundleName, i18nLogger);
	}

	private final String bundleName;
	private final ResourceBundle resources;
	private final String notFoundMessage;
	private final I18nLogger i18nLogger;

	private I18nBundle(String bundleName, I18nLogger i18nLogger) {
		this(bundleName, new EmptyResourceBundle(), MESSAGE_BUNDLE_NOT_FOUND,
				i18nLogger);
	}

	public I18nBundle(String bundleName, ResourceBundle resources,
			I18nLogger i18nLogger) {
		this(bundleName, resources, MESSAGE_KEY_NOT_FOUND, i18nLogger);
	}

	private I18nBundle(String bundleName, ResourceBundle resources,
			String notFoundMessage, I18nLogger i18nLogger) {
		if (bundleName == null) {
			throw new IllegalArgumentException("bundleName may not be null");
		}
		if (bundleName.isEmpty()) {
			throw new IllegalArgumentException("bundleName may not be empty");
		}
		if (resources == null) {
			throw new NullPointerException("resources may not be null.");
		}
		if (notFoundMessage == null) {
			throw new NullPointerException("notFoundMessage may not be null.");
		}
		this.bundleName = bundleName;
		this.resources = resources;
		this.notFoundMessage = notFoundMessage;
		this.i18nLogger = i18nLogger;
	}

	public String text(String key, Object... parameters) {
		String textString;
		if (resources.containsKey(key)) {
			textString = resources.getString(key);
			log.debug("In '" + bundleName + "', " + key + "='" + textString
					+ "')");
		} else {
			String message = MessageFormat.format(notFoundMessage, bundleName,
					key);
			log.warn(message);
			textString = "ERROR: " + message;
		}
		String result = formatString(textString, parameters);

		if (i18nLogger != null) {
			i18nLogger.log(bundleName, key, parameters, textString, result);
		}
		return result;
	}

	private static String formatString(String textString, Object... parameters) {
		if (parameters.length == 0) {
			return textString;
		} else {
			return MessageFormat.format(textString, parameters);
		}
	}

	/**
	 * A resource bundle that contains no strings.
	 */
	public static class EmptyResourceBundle extends ResourceBundle {
		@Override
		public Enumeration<String> getKeys() {
			return Collections.enumeration(Collections.<String> emptySet());
		}

		@Override
		protected Object handleGetObject(String key) {
			if (key == null) {
				throw new NullPointerException("key may not be null.");
			}
			return null;
		}

	}

}
