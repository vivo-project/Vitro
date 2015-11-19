/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.developer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * A singleton holder for the developer settings.
 * 
 * Start with an empty settings map.
 * 
 * The Setup class will read "developer.properties" from the "config"
 * sub-directory of the Vitro home directory, and load its settings. If the file
 * doesn't exist, or doesn't contain values for certain properties, those
 * properties will keep their default values.
 * 
 * An AJAX request can be used to update the properties. If the request has
 * multiple values for a property, the first value will be used. If the request
 * does not contain a value for a property, that property will keep its current
 * value.
 * 
 * The property names in "developer.properties" are not suitable as fields in
 * the HTML panel, because they contain periods. For the HTML panel, we
 * translate those periods to underscores.
 * 
 * If the ENABLED flag is not set, then getBinary() will return false for all
 * keys, and getString() will return the empty string. This simplifies the logic
 * in the client code. Use getRawSettingsMap() to display the actual values in
 * the developer panel.
 */
public class DeveloperSettings {
	private static final Log log = LogFactory.getLog(DeveloperSettings.class);

	// ----------------------------------------------------------------------
	// The factory
	// ----------------------------------------------------------------------

	private static final DeveloperSettings instance = new DeveloperSettings();

	public static DeveloperSettings getInstance() {
		return instance;
	}

	// ----------------------------------------------------------------------
	// The instance
	// ----------------------------------------------------------------------

	private final Map<Key, String> settings;

	private DeveloperSettings() {
		this.settings = new EnumMap<>(Key.class);
	}

	public void updateFromRequest(Map<String, String[]> parameterMap) {
		if (log.isDebugEnabled()) {
			dumpParameterMap(parameterMap);
		}

		Map<Key, String> fromRequest = new HashMap<>();
		for (String key : parameterMap.keySet()) {
			fromRequest.put(Key.fromElementId(key), parameterMap.get(key)[0]);
		}
		update(fromRequest);
	}

	public void updateFromProperties(Properties properties) {
		Map<Key, String> fromFile = new HashMap<>();
		for (String key : properties.stringPropertyNames()) {
			fromFile.put(Key.fromPropertyName(key), properties.getProperty(key));
		}
		update(fromFile);
	}

	/**
	 * Update by known keys, so we will ignore any irrelevant request
	 * parameters, or incorrect properties.
	 */
	private void update(Map<Key, String> changedSettings) {
		for (Key key : Key.values()) {
			String s = changedSettings.get(key);
			if (s != null) {
				s = s.trim();
				if (key.isBoolean()) {
					settings.put(key, Boolean.valueOf(s).toString());
				} else {
					settings.put(key, s);
				}
			}
		}
		log.debug("DeveloperSettings: " + this);
	}

	/**
	 * If developerMode is enabled, return the boolean value of the stored
	 * setting.
	 */
	public boolean getBoolean(Key key) {
		if (!key.isBoolean()) {
			log.warn("Key '" + key + "' does not take a boolean value.");
		}
		if (isDeveloperModeEnabled()) {
			return Boolean.valueOf(settings.get(key));
		} else {
			return false;
		}
	}

	/**
	 * If developerMode is enabled and the setting has a value, return that
	 * value. Otherwise, return the empty string.
	 */
	public String getString(Key key) {
		if (key.isBoolean()) {
			log.warn("Key '" + key + "' takes a boolean value.");
		}
		String value = settings.get(key);
		if (value != null && isDeveloperModeEnabled()) {
			return value;
		} else {
			return "";
		}
	}

	private boolean isDeveloperModeEnabled() {
		return Boolean.valueOf(settings.get(Key.ENABLED));
	}

	/**
	 * Get the values of all the settings, by element ID, regardless of whether
	 * developerMode is enabled or not. Boolean settings are represented as
	 * actual Booleans, so Freemarker can perform logical tests on them.
	 */
	public Map<String, Object> getRawSettingsMap() {
		Map<String, Object> map = new HashMap<>();
		for (Key key : Key.values()) {
			map.put(key.elementId(), getRawValue(key));
		}
		return map;
	}

	/**
	 * Get a String or Boolean value, as appropriate for the key. A boolean key
	 * with no value returns false. A non-boolean key with no value returns the
	 * empty string.
	 */
	private Object getRawValue(Key key) {
		String value = settings.get(key);
		if (key.isBoolean()) {
			return Boolean.valueOf(value);
		} else {
			return (value == null) ? "" : value;
		}
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

	// ----------------------------------------------------------------------
	// Setup class
	// ----------------------------------------------------------------------

	public static class Setup implements ServletContextListener {

		@Override
		public void contextInitialized(ServletContextEvent sce) {
			ServletContext ctx = sce.getServletContext();
			StartupStatus ss = StartupStatus.getBean(ctx);
			DeveloperSettings devSettings = DeveloperSettings.getInstance();

			Path homeDir = ApplicationUtils.instance().getHomeDirectory()
					.getPath();
			File dsFile = homeDir.resolve("config/developer.properties")
					.toFile();

			try (FileReader reader = new FileReader(dsFile)) {
				Properties dsProps = new Properties();
				dsProps.load(reader);
				devSettings.updateFromProperties(dsProps);
				log.info(devSettings);
				ss.info(this, "Loaded the 'developer.properties' file: "
						+ devSettings);
			} catch (FileNotFoundException e) {
				ss.info(this, "'developer.properties' file does not exist.");
			} catch (Exception e) {
				ss.warning(this,
						"Failed to load the 'developer.properties' file.", e);
			}
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce) {
			// Nothing to remove.
		}

	}

}
