/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.developer;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;

/**
 * Hold the global developer settings. Render to JSON when requested.
 * 
 * On first request, the "developer.properties" file is loaded from the Vitro
 * home directory. If the file doesn't exist, or doesn't contain values for
 * certain properties, those propertiew will keep their default values.
 * 
 * An AJAX request can be used to update the properties. If the request has
 * multiple values for a property, the first value will be used. If the request
 * does not contain a value for a property, that property will keep its current
 * value.
 */
public class DeveloperSettings {
	private static final Log log = LogFactory.getLog(DeveloperSettings.class);

	public enum Keys {
		/**
		 * Developer mode and developer panel is enabled.
		 */
		ENABLED("developer.enabled", true),

		/**
		 * Users don't need authority to use the developer panel. But they still
		 * can't enable it without authority.
		 */
		PERMIT_ANONYMOUS_CONTROL("developer.permitAnonymousControl", true),

		/**
		 * Load Freemarker templates every time they are requested.
		 */
		DEFEAT_FREEMARKER_CACHE("developer.defeatFreemarkerCache", true),

		/**
		 * Show where each Freemarker template starts and stops.
		 */
		INSERT_FREEMARKER_DELIMITERS("developer.insertFreemarkerDelimiters",
				true),

		/**
		 * Load language property files every time they are requested.
		 */
		I18N_DEFEAT_CACHE("developer.i18n.defeatCache", true),

		/**
		 * Enable the I18nLogger to log each string request.
		 */
		I18N_LOG_STRINGS("developer.i18n.logStringRequests", true),

		/**
		 * Enable the LoggingRDFService
		 */
		LOGGING_RDF_ENABLE("developer.loggingRDFService.enable", true),

		/**
		 * When logging with the LoggingRDFService, include a stack trace
		 */
		LOGGING_RDF_STACK_TRACE("developer.loggingRDFService.stackTrace", true),

		/**
		 * Don't log with the LoggingRDFService unless the calling stack meets
		 * this restriction.
		 */
		LOGGING_RDF_RESTRICTION("developer.loggingRDFService.restriction",
				false);

		private final String propertyName;
		private final String elementId;
		private final boolean bool;

		private Keys(String propertyName, boolean bool) {
			this.propertyName = propertyName;
			this.elementId = produceElementId();
			this.bool = bool;
		}

		public String propertyName() {
			return propertyName;
		}

		public String elementId() {
			return elementId;
		}

		boolean isBoolean() {
			return bool;
		}

		/**
		 * The element ID is camel-case instead of period-delimited. So
		 * "developer.enabled" becomes "developerEnabled".
		 */
		String produceElementId() {
			StringBuilder id = new StringBuilder(propertyName.length());
			boolean capitalize = false;
			for (int i = 0; i < propertyName.length(); i++) {
				char c = propertyName.charAt(i);
				if (c == '.') {
					capitalize = true;
				} else if (capitalize) {
					id.append(Character.toUpperCase(c));
					capitalize = false;
				} else {
					id.append(c);
				}
			}
			return id.toString();
		}

		@Override
		public String toString() {
			return propertyName;
		}

		static Keys fromElementId(String id) {
			for (Keys k : Keys.values()) {
				if (k.elementId.equals(id)) {
					return k;
				}
			}
			log.error("Can't find key for element id: '" + id + "'");
			return null;
		}

		static Keys fromPropertyName(String name) {
			for (Keys k : Keys.values()) {
				if (k.propertyName.equals(name)) {
					return k;
				}
			}
			log.error("Can't find key for property name: '" + name + "'");
			return null;
		}

	}

	// ----------------------------------------------------------------------
	// The factory
	// ----------------------------------------------------------------------

	private static final String ATTRIBUTE_NAME = DeveloperSettings.class
			.getName();

	public static DeveloperSettings getBean(HttpServletRequest req) {
		return getBean(req.getSession().getServletContext());
	}

	public static DeveloperSettings getBean(ServletContext ctx) {
		Object o = ctx.getAttribute(ATTRIBUTE_NAME);
		if (o instanceof DeveloperSettings) {
			return (DeveloperSettings) o;
		} else {
			DeveloperSettings ds = new DeveloperSettings(ctx);
			ctx.setAttribute(ATTRIBUTE_NAME, ds);
			return ds;
		}
	}

	// ----------------------------------------------------------------------
	// The instance
	// ----------------------------------------------------------------------

	private final Map<Keys, Object> settings = new EnumMap<>(Keys.class);

	private DeveloperSettings(ServletContext ctx) {
		updateFromFile(ctx);
	}

	/**
	 * Read the initial settings from "developer.properties" in the Vitro home
	 * directory.
	 * 
	 * This method is "protected" so we can override it for unit tests.
	 */
	protected void updateFromFile(ServletContext ctx) {
		Map<Keys, String> fromFile = new HashMap<>();

		ConfigurationProperties props = ConfigurationProperties.getBean(ctx);
		String home = props.getProperty("vitro.home");
		File dsFile = Paths.get(home, "developer.properties").toFile();

		if (dsFile.isFile()) {
			try (FileReader reader = new FileReader(dsFile)) {
				Properties dsProps = new Properties();
				dsProps.load(reader);
				for (String key : dsProps.stringPropertyNames()) {
					fromFile.put(Keys.fromPropertyName(key),
							dsProps.getProperty(key));
				}
			} catch (Exception e) {
				log.warn("Failed to load 'developer.properties' file.", e);
			}
		} else {
			log.debug("No developer.properties file.");
		}

		log.debug("Properties from file: " + fromFile);
		update(fromFile);
	}

	/** Provide the parameter map from the HttpServletRequest */
	public void updateFromRequest(Map<String, String[]> parameterMap) {
		if (log.isDebugEnabled()) {
			dumpParameterMap(parameterMap);
		}

		Map<Keys, String> fromRequest = new HashMap<>();
		for (String key : parameterMap.keySet()) {
			fromRequest.put(Keys.fromElementId(key), parameterMap.get(key)[0]);
		}
		update(fromRequest);
	}

	private void update(Map<Keys, String> changedSettings) {
		for (Keys key : Keys.values()) {
			String s = changedSettings.get(key);
			if (s != null) {
				if (key.isBoolean()) {
					settings.put(key, Boolean.valueOf(s));
				} else {
					settings.put(key, s);
				}
			}
		}
		log.debug("DeveloperSettings: " + this);
	}

	public Object get(Keys key) {
		if (key.isBoolean()) {
			return getBoolean(key);
		} else {
			return getString(key);
		}
	}

	public boolean getBoolean(Keys key) {
		if (!key.isBoolean()) {
			throw new IllegalArgumentException("Key '" + key
					+ "' does not take a boolean value.");
		}
		if (settings.containsKey(key)) {
			if (Boolean.TRUE.equals(settings.get(Keys.ENABLED))) {
				return (Boolean) settings.get(key);
			}
		}
		return false;
	}

	public String getString(Keys key) {
		if (key.isBoolean()) {
			throw new IllegalArgumentException("Key '" + key
					+ "' takes a boolean value.");
		}
		if (settings.containsKey(key)) {
			if (Boolean.TRUE.equals(settings.get(Keys.ENABLED))) {
				return (String) settings.get(key);
			}
		}
		return "";
	}

	public Map<String, Object> getSettingsMap() {
		Map<String, Object> map = new HashMap<>();
		for (Keys key : Keys.values()) {
			map.put(key.elementId(), get(key));
		}
		return map;
	}

	@Override
	public String toString() {
		return "DeveloperSettings" + settings;
	}

	/* For debugging. */
	private void dumpParameterMap(Map<String, String[]> parameterMap) {
		Map<String, List<String>> map = new HashMap<>();
		for (String key : parameterMap.keySet()) {
			map.put(key, Arrays.asList(parameterMap.get(key)));
		}
		log.debug("Parameter map: " + map);
	}

}
