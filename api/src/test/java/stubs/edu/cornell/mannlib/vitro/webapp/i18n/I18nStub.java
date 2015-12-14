/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.i18n;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.i18n.I18nBundle;

/**
 * An implementation of I18n for unit tests. Construct a new instance and it
 * replaces the instance of I18n.
 * 
 * Each bundle that you get from it is the same, returning the key itself as the
 * value of that key.
 */
public class I18nStub extends I18n {
	private static final Log log = LogFactory.getLog(I18nStub.class);

	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	public static void setup() {
		try {
			Field instanceField = I18n.class.getDeclaredField("instance");
			log.debug("Field is " + instanceField);
			instanceField.setAccessible(true);
			log.debug("Instance is " + instanceField.get(null));
			instanceField.set(null, new I18nStub());
			log.debug("Instance is " + instanceField.get(null));
			log.debug("Created and inserted.");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/** Make it private, so they will use the setup() method. */
	private I18nStub() {
		// Nothing to initialize.
	}
	
	@Override
	protected I18nBundle getBundle(String bundleName, HttpServletRequest req) {
		return new I18nBundleStub(bundleName);
	}

	private class I18nBundleStub extends I18nBundle {
		public I18nBundleStub(String bundleName) {
			super(bundleName, new DummyResourceBundle(), null);
		}

		@Override
		public String text(String key, Object... parameters) {
			return key;
		}
	}

	/**
	 * Not actually used, but the constructor of I18nBundle requires a non-null
	 * ResourceBundle.
	 */
	private class DummyResourceBundle extends ResourceBundle {
		@Override
		protected Object handleGetObject(String key) {
			return key;
		}

		@Override
		public Enumeration<String> getKeys() {
			return Collections.emptyEnumeration();
		}
	}
}
