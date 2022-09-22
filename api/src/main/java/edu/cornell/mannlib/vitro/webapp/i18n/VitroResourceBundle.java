/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.*;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.Lock;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_UNION;

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
 * If a property has a value (after overriding) of "@@file &lt;filepath&gt;", the
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

	private static final String BUNDLE_DIRECTORY = "i18n/";

	private static final String FILE_FLAG = "@@file ";
	private static final String MESSAGE_FILE_NOT_FOUND = "File {1} not found for property {0}.";

	private static final List<String> appPrefixes = new ArrayList<>();

	static {
		addAppPrefix("vitro");
	}

	private static final String SPARQL_LANGUAGE_QUERY = " PREFIX : <http://vivoweb.org/ontology/core/properties#>\n" +
														"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
														"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
														"SELECT ?key ?translation\n" +
														"WHERE {\n" +
														"  ?uri :key ?key .\n" +
														"  ?uri rdfs:label ?translationWithLocale .\t\n" +
														"  OPTIONAL { \n" +
														"    ?uri :theme ?found_theme .\n" +
														"  }\n" +
														"  OPTIONAL { \n" +
														"    ?uri :application ?found_application .\n" +
														"  }\n" +
														"  BIND(COALESCE(?found_theme, \"none\") as ?theme ) .\n" +
														"  BIND(COALESCE(?found_application, \"none\") as ?application ) .\n" +
														"  BIND(IF(?current_theme = ?theme, 100, 0) AS ?priority1 ) .\n" +
														"  BIND(IF(\"local\" = ?application, xsd:integer(?priority1)+50, xsd:integer(?priority1)) AS ?priority2 ) .\n" +
														"  BIND(IF(\"VIVO\" = ?application, xsd:integer(?priority2)+10, xsd:integer(?priority2)) AS ?priority3 ) .\n" +
														"  BIND(IF(\"Vitro\" = ?application, xsd:integer(?priority3)+5, xsd:integer(?priority3)) AS ?priority4 ) .\n" +
														"  BIND (STR(?translationWithLocale)  AS ?translation) .\n" +
														"  FILTER ( lang(?translationWithLocale) = ?locale ) .\n" +
														"} \n" +
														"ORDER by ASC(?priority4) " ;

	public static void addAppPrefix(String prefix) {
		if (!prefix.endsWith("-") && !prefix.endsWith("_")) {
			prefix = prefix + "_";
		}

		if (!appPrefixes.contains(prefix)) {
			appPrefixes.add(prefix);
		}
	}

	// ----------------------------------------------------------------------
	// Factory method
	// ----------------------------------------------------------------------

	/**
	 * Returns the bundle for the for foo_ba_RR, providing that
	 * foo_ba_RR.properties exists in the I18n area of either the theme or the
	 * application.
	 *
	 * If the desired file doesn't exist in either location, return null.
	 * Usually, this does not indicate a problem but only that we were looking
	 * for too specific a bundle. For example, if the base name of the bundle is
	 * "all" and the locale is "en_US", we will likely return null on the search
	 * for all_en_US.properties, and all_en.properties, but will return a full
	 * bundle for all.properties.
	 *
	 * Of course, if all.properties doesn't exist either, then we have a
	 * problem, but that will be reported elsewhere.
	 *
	 * @return the populated bundle or null.
	 */
	public static VitroResourceBundle getBundle(String bundleName,
												ServletContext ctx, Locale locale, String themeDirectory,
												Control control) {
		try {
			return new VitroResourceBundle(bundleName, ctx, locale, themeDirectory, control);
		} catch (FileNotFoundException e) {
			log.debug(e.getMessage());
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
	private final Properties properties;
	private final String locale;
	private final String theme;

	private VitroResourceBundle(String bundleName, ServletContext ctx,
			Locale locale, String themeDirectory, Control control)
			throws IOException {

		String themeI18nPath = "/" + themeDirectory + BUNDLE_DIRECTORY;
		String appI18nPath = "/" + BUNDLE_DIRECTORY;

		log.debug("Paths are '" + themeI18nPath + "' and '" + appI18nPath
				+ "'");
		this.bundleName = bundleName;
		this.ctx = ctx;
		//TODO ask Veljko do we have some issue with the line below regarding Serbian two scripts
		this.locale = locale.toLanguageTag();
		this.theme = ApplicationBean.ThemeInfo.themeNameFromDir(themeDirectory);
		this.appI18nPath = appI18nPath;
		this.themeI18nPath = themeI18nPath;
		this.control = control;
		this.properties = loadProperties();
		loadReferencedFiles();
	}

	private Properties loadProperties() throws IOException {
		Properties translations = new Properties();
		translations = loadTranslationsFromTriplestore(translations);
		translations = loadTranslationsFromFiles(translations);
		return translations;
	}

	private Properties loadTranslationsFromTriplestore(Properties props) throws IOException {
		ParameterizedSparqlString pss = new ParameterizedSparqlString();
		pss.setCommandText(SPARQL_LANGUAGE_QUERY);
		pss.setLiteral("locale", locale);
		pss.setLiteral("current_theme", theme);
//		pss.setLiteral("current_application", "");
		ContextModelAccess modelAccess = ModelAccess.on(ctx);
		Model queryModel = modelAccess.getOntModel(DISPLAY);
		if (queryModel != null){
			queryModel.enterCriticalSection(Lock.READ);
			try {
				QueryExecution qexec = QueryExecutionFactory.create(pss.toString(), queryModel);
				try {
					ResultSet results = qexec.execSelect();

					while (results.hasNext()) {
						QuerySolution solution = results.nextSolution();
						props.put(solution.get("key").toString(), solution.get("translation").toString());
					}

				} catch (Exception e) {
					log.error(e.getLocalizedMessage());
					e.printStackTrace();
				} finally {
					qexec.close();
				}
			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
				e.printStackTrace();
			} finally {
				queryModel.leaveCriticalSection();
			}
		}
		return props;
	}

	private Properties loadTranslationsFromFiles(Properties props) throws IOException {
		String resourceName = control.toResourceName(bundleName, "properties");

		File defaultsPath = locateFile(joinPath(appI18nPath, resourceName));
		File propertiesPath = locateFile(joinPath(themeI18nPath, resourceName));

		props = loadProperties(props, defaultsPath);
		if (appPrefixes != null && appPrefixes.size() > 0) {
			for (String appPrefix : appPrefixes) {
				props = loadProperties(props, locateFile(joinPath(appI18nPath, (appPrefix + resourceName))));
			}
		}
		props = loadProperties(props, propertiesPath);
		if (props == null) {
			throw new FileNotFoundException("Property file not found at '" + defaultsPath + "' or '" + propertiesPath + "'");
		}
		props = loadProperties(props, locateFile(joinPath("/local/i18n/", resourceName)));

		return props;
	}

	private Properties loadProperties(Properties defProps, File file) throws IOException {
		if (file == null || !file.isFile()) {
			return defProps;
		}

		Properties props = null;
		if (defProps != null) {
			props = new Properties(defProps);
		} else {
			props = new Properties();
		}

		log.debug("Loading bundle '" + bundleName + "' defaults from '" + file + "'");
		FileInputStream stream = new FileInputStream(file);
		Reader reader = new InputStreamReader(stream, "UTF-8");
		try {
			props.load(reader);
		} finally {
			reader.close();
		}

		if (props.size() > 0) {
			return props;
		}

		return defProps;
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
