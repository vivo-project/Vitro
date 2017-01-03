/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.config;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Test that gets run at servlet context startup to check for the existence and
 * validity of properties in the configuration.
 */
public class ConfigurationPropertiesSmokeTests implements
		ServletContextListener {
	private static final Log log = LogFactory
			.getLog(ConfigurationPropertiesSmokeTests.class);

	private static final String PROPERTY_DEFAULT_NAMESPACE = "Vitro.defaultNamespace";
	private static final String PROPERTY_LANGUAGE_BUILD = "languages.addToBuild";
	private static final String PROPERTY_LANGUAGE_SELECTABLE = "languages.selectableLocales";
	private static final String PROPERTY_LANGUAGE_FORCE = "languages.forceLocale";
	private static final String PROPERTY_LANGUAGE_FILTER = "RDFService.languageFilter";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		ConfigurationProperties props = ConfigurationProperties.getBean(ctx);
		StartupStatus ss = StartupStatus.getBean(ctx);

		checkDefaultNamespace(ctx, props, ss);
		checkLanguages(props, ss);
	}

	/**
	 * Confirm that the default namespace is specified and a syntactically valid
	 * URI. It should also end with "/individual/".
	 */
	private void checkDefaultNamespace(ServletContext ctx,
			ConfigurationProperties props, StartupStatus ss) {
		String ns = props.getProperty(PROPERTY_DEFAULT_NAMESPACE);
		if (ns == null || ns.isEmpty()) {
			ss.fatal(this, "runtime.properties does not contain a value for '"
					+ PROPERTY_DEFAULT_NAMESPACE + "'");
			return;
		}

		try {
			new URI(ns);
		} catch (URISyntaxException e) {
			ss.fatal(this,
					PROPERTY_DEFAULT_NAMESPACE + " '" + ns
							+ "' is not a valid URI. "
							+ (e.getMessage() != null ? e.getMessage() : ""));
			return;
		}

		String suffix = "/individual/";
		if (!ns.endsWith(suffix)) {
			ss.warning(this,
					"Default namespace does not match the expected form "
							+ "(does not end with '" + suffix + "'): '" + ns
							+ "'");
		}
	}

	/**
	 * Warn if we set up the languages incorrectly:
	 * 
	 * Must build with a language in order to select languages. Can't select
	 * languages and force language. Shouldn't build with language unless
	 * language filtering is enabled.
	 */
	private void checkLanguages(ConfigurationProperties props, StartupStatus ss) {
		String buildString = props.getProperty(PROPERTY_LANGUAGE_BUILD);
		boolean buildWithLanguages = StringUtils.isNotBlank(buildString);

		String selectString = props.getProperty(PROPERTY_LANGUAGE_SELECTABLE);
		boolean selectableLanguages = StringUtils.isNotBlank(selectString);

		String forceString = props.getProperty(PROPERTY_LANGUAGE_FORCE);
		boolean forceLanguage = StringUtils.isNotBlank(forceString);

		String filterString = props.getProperty(PROPERTY_LANGUAGE_FILTER,
				"true");
		boolean languageFilter = Boolean.valueOf(filterString);

		if (selectableLanguages && !buildWithLanguages) {
			ss.warning(this, String.format("Problem with Language setup - "
					+ "runtime.properties specifies a "
					+ "list of selectable languages (%s = %s), but "
					+ "build.properties did not include any languages with %s",
					PROPERTY_LANGUAGE_SELECTABLE, selectString,
					PROPERTY_LANGUAGE_BUILD));
		}

		if (selectableLanguages && forceLanguage) {
			ss.warning(this, String.format("Problem with Language setup - "
					+ "runtime.properties specifies a "
					+ "forced locale (%s = %s), and also a list of selectable "
					+ "languages (%s = %s). These options are incompatible.",
					PROPERTY_LANGUAGE_FORCE, forceString,
					PROPERTY_LANGUAGE_SELECTABLE, selectString));
		}

		if (buildWithLanguages && !languageFilter) {
			ss.warning(this, String.format("Problem with Language setup - "
					+ "build.properties includes one or more additional "
					+ "languages (%s = %s), but runtime.properties has "
					+ "disabled language filtering (%s = %s). This will "
					+ "likely result in a mix of languages in the "
					+ "application.", PROPERTY_LANGUAGE_BUILD, buildString,
					PROPERTY_LANGUAGE_FILTER, filterString));
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// nothing to do at shutdown
	}

}
