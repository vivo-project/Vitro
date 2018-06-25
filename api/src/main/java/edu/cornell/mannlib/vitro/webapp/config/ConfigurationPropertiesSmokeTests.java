/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.config;

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
	private static final String PROPERTY_ARGON2_TIME = "argon2.time";
	private static final String PROPERTY_ARGON2_MEMORY = "argon2.memory";
	private static final String PROPERTY_ARGON2_PARALLELISM = "argon2.parallelism";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		ConfigurationProperties props = ConfigurationProperties.getBean(ctx);
		StartupStatus ss = StartupStatus.getBean(ctx);

		checkDefaultNamespace(ctx, props, ss);
		checkMultipleRPFs(ctx, props, ss);
		checkLanguages(props, ss);
		checkEncryptionParameters(props, ss);
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
	 * Warn if runtime.properties exists in multiple locations
	 * or is located vivo.home instead of vivo.home/config
	 */
	private void checkMultipleRPFs(ServletContext ctx,
			ConfigurationProperties props, StartupStatus ss) {
		String rpfStatus = props.getProperty(ConfigurationPropertiesSetup.RP_MULTIPLE);

		if (rpfStatus.equals("both")) {
			ss.warning(this,
					"Deprecation warning: Files matching the name 'runtime.properties' "
							+ "were found in both vivo.home and vivo.home/config. Using "
							+ "the file in vivo.home. Future releases may require "
							+ "runtime.properties be placed in vivo.home/config.");
		}

		if (rpfStatus.equals("home")) {
			ss.warning(this,
					"Deprecation warning: runtime.properties was found in the "
							+ "vivo.home directory. The recommended directory for "
							+ "runtime.properties is now vivo.home/config. Future releases "
							+ "may require runtime.properties be placed in "
							+ "vivo.home/config.");
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

	/**
	 * Fail if there are no config properties for the Argon2 encryption.
	 */
	private void checkEncryptionParameters(ConfigurationProperties props,
										   StartupStatus ss) {
		failIfNotPresent(props, ss, PROPERTY_ARGON2_TIME);
		failIfNotPresent(props, ss, PROPERTY_ARGON2_MEMORY);
		failIfNotPresent(props, ss, PROPERTY_ARGON2_PARALLELISM);
	}

	private void failIfNotPresent(ConfigurationProperties props,
								  StartupStatus ss, String name) {
		String value = props.getProperty(name);
		if (value == null || value.isEmpty()) {
			ss.fatal(this, "runtime.properties does not contain a value for '"
					+ name + "'");
			return;
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// nothing to do at shutdown
	}

}
