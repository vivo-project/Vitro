/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Works like a PropertyResourceBundle with two exceptions:
 * 
 * It looks for the file in both the i18n directory of the theme and in the i18n
 * directory of the application. Properties found in the theme override those
 * found in the application.
 * 
 * It allows a property to take its contents from a file. File paths are
 * relative to the i18n directory. Again, a file in the theme will override one
 * in the application.
 * 
 * If a property has a value (after overriding) of "@@file <filepath>", the
 * bundle looks for the file relative to the i18n directory of the theme, then
 * relative to the i18n directory of the application. If the file is not found
 * in either location, a warning is written to the log and the property will
 * contain an error message for displayed.
 * 
 * Note that the filename is not manipulated for Locale, so the author of the
 * properties files must do it explicitly. For example:
 * 
 * In all.properties: account_email_html = @@file accountEmail.html
 * 
 * In all_es.properties: account_email_html = @@file accountEmail_es.html
 */
public class VitroResourceBundle extends ResourceBundle {
	private static final Log log = LogFactory.getLog(VitroResourceBundle.class);

	private static final String FILE_FLAG = "@@file ";
	private static final String MESSAGE_FILE_NOT_FOUND = "File {1} not found for property {0}.";

	// ----------------------------------------------------------------------
	// Factory method
	// ----------------------------------------------------------------------

	public static VitroResourceBundle getBundle(String bundleName,
			ServletContext ctx, String appI18nPath, String themeI18nPath,
			Control control) {
		try {
			return new VitroResourceBundle(bundleName, ctx, appI18nPath,
					themeI18nPath, control);
		} catch (FileNotFoundException e) {
			log.debug(e);
			return null;
		} catch (Exception e) {
			log.warn(e, e);
			return null;
		}
	}

	// ----------------------------------------------------------------------
	// The instance
	// ----------------------------------------------------------------------

	private final String bundleName;
	private final ServletContext ctx;
	private final String appI18nPath;
	private final String themeI18nPath;
	private final Control control;
	private final Properties defaults;
	private final Properties properties;

	private VitroResourceBundle(String bundleName, ServletContext ctx,
			String appI18nPath, String themeI18nPath, Control control)
			throws IOException {
		this.bundleName = bundleName;
		this.ctx = ctx;
		this.appI18nPath = appI18nPath;
		this.themeI18nPath = themeI18nPath;
		this.control = control;

		this.defaults = new Properties();
		this.properties = new Properties(this.defaults);

		loadProperties();
		loadReferencedFiles();
	}

	private void loadProperties() throws IOException {
		String resourceName = control.toResourceName(bundleName, "properties");

		String defaultsPath = joinPath(appI18nPath, resourceName);
		String propertiesPath = joinPath(themeI18nPath, resourceName);
		File defaultsFile = locateFile(defaultsPath);
		File propertiesFile = locateFile(propertiesPath);

		if ((defaultsFile == null) && (propertiesFile == null)) {
			throw new FileNotFoundException("Property file not found at '"
					+ defaultsPath + "' or '" + propertiesPath + "'");
		}

		if (defaultsFile != null) {
			log.debug("Loading bundle '" + bundleName + "' defaults from '"
					+ defaultsPath + "'");
			FileInputStream stream = new FileInputStream(defaultsFile);
			try {
				this.defaults.load(stream);
			} finally {
				stream.close();
			}
		}
		if (propertiesFile != null) {
			log.debug("Loading bundle '" + bundleName + "' overrides from '"
					+ propertiesPath + "'");
			FileInputStream stream = new FileInputStream(propertiesFile);
			try {
				this.properties.load(stream);
			} finally {
				stream.close();
			}
		}
	}

	private void loadReferencedFiles() throws IOException {
		for (String key : this.properties.stringPropertyNames()) {
			String value = this.properties.getProperty(key);
			if (value.startsWith(FILE_FLAG)) {
				String filepath = value.substring(FILE_FLAG.length()).trim();
				loadReferencedFile(key, filepath);
			}
		}
	}

	private void loadReferencedFile(String key, String filepath)
			throws IOException {
		String appFilePath = joinPath(appI18nPath, filepath);
		String themeFilePath = joinPath(themeI18nPath, filepath);
		File appFile = locateFile(appFilePath);
		File themeFile = locateFile(themeFilePath);

		if (themeFile != null) {
			this.properties.setProperty(key,
					FileUtils.readFileToString(themeFile, "UTF-8"));
		} else if (appFile != null) {
			this.properties.setProperty(key,
					FileUtils.readFileToString(appFile, "UTF-8"));
		} else {
			String message = MessageFormat.format(MESSAGE_FILE_NOT_FOUND, key,
					themeFilePath, appFilePath);
			this.properties.setProperty(key, message);
			log.warn(message);
		}
	}

	private String joinPath(String root, String twig) {
		if ((root.charAt(root.length() - 1) == File.separatorChar)
				|| (twig.charAt(0) == File.separatorChar)) {
			return root + twig;
		} else {
			return root + File.separatorChar + twig;
		}
	}

	private File locateFile(String path) {
		String realPath = ctx.getRealPath(path);
		if (realPath == null) {
			log.debug("No real path for '" + path + "'");
			return null;
		}

		File f = new File(realPath);
		if (!f.isFile()) {
			log.debug("No file at '" + realPath + "'");
			return null;
		}
		if (!f.canRead()) {
			log.error("Can't read the file at '" + realPath + "'");
			return null;
		}
		log.debug("Located file '" + path + "' at '" + realPath + "'");
		return f;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<String> getKeys() {
		return (Enumeration<String>) this.properties.propertyNames();
	}

	@Override
	protected Object handleGetObject(String key) {
		String value = this.properties.getProperty(key);
		if (value == null) {
			log.debug(bundleName + " has no value for '" + key + "'");
		}
		return value;
	}

}
