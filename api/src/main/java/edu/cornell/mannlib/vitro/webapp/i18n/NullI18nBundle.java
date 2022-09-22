/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.i18n;

import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * A wrapper for a ResourceBundle that will not throw an exception, no matter
 * what string you request.
 *
 * If the ResourceBundle was not found, or if it doesn't contain the requested
 * key, an error message string is returned, to help the developer diagnose the
 * problem.
 */
public class NullI18nBundle extends I18nBundle{

	private static final String MESSAGE_BUNDLE_NOT_FOUND = "Text bundle ''{0}'' not found.";

	private static NullI18nBundle instance = null;

	public static NullI18nBundle getInstance(String bundleName, I18nLogger i18nLogger){
		if (instance == null)
			instance = new NullI18nBundle(bundleName, i18nLogger);
		return instance;
	}

	private NullI18nBundle(String bundleName, I18nLogger i18nLogger) {
		super(bundleName, new EmptyResourceBundle(), MESSAGE_BUNDLE_NOT_FOUND,
				i18nLogger);
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
