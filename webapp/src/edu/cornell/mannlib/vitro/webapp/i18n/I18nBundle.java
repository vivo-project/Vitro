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

	public static I18nBundle emptyBundle(String bundleName) {
		return new I18nBundle(bundleName);
	}

	private final String bundleName;
	private final ResourceBundle resources;
	private final String notFoundMessage;

	private I18nBundle(String bundleName) {
		this(bundleName, new EmptyResourceBundle(), MESSAGE_BUNDLE_NOT_FOUND);
	}

	public I18nBundle(String bundleName, ResourceBundle resources) {
		this(bundleName, resources, MESSAGE_KEY_NOT_FOUND);
	}
	
	private I18nBundle(String bundleName, ResourceBundle resources, String notFoundMessage) {
		if (bundleName == null) {
			throw new IllegalArgumentException("bundleName may not be null");
		}
		if (bundleName.isEmpty()) {
			throw new IllegalArgumentException("bundleName may not be empty");
		}
		if (resources == null) {
			throw new NullPointerException("resources may not be null.");
		}if (notFoundMessage == null) {
			throw new NullPointerException("notFoundMessage may not be null.");
		}
		this.bundleName = bundleName;
		this.resources = resources;
		this.notFoundMessage = notFoundMessage;
	}
	
	public String text(String key, Object... parameters) {
		log.debug("Asking for '" + key + "' from bundle '" + bundleName + "'");

		String textString;
		if (resources.containsKey(key)) {
			textString = resources.getString(key);
			return formatString(textString, parameters);
		} else {
			String message = MessageFormat.format(notFoundMessage, bundleName,
					key);
			log.warn(message);
			return "ERROR: " + message;
		}
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
