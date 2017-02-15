/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.i18n.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Check the ConfigurationProperties for a forced locale, or for a
 * comma-separate list of selectable locales.
 * 
 * Create the appropriate Locale objects and store them in the ServletContext.
 */
public class LocaleSelectionSetup implements ServletContextListener {
	/**
	 * If this is set, the locale is forced. No selection will be offered to the
	 * user, and browser locales will be ignored.
	 */
	public static final String PROPERTY_FORCE_LOCALE = "languages.forceLocale";

	/**
	 * This is the list of locales that the user may select. There should be a
	 * national flag or symbol available for each supported locale.
	 */
	public static final String PROPERTY_SELECTABLE_LOCALES = "languages.selectableLocales";

	private ServletContext ctx;
	private StartupStatus ss;
	private ConfigurationProperties props;

	private String forceString;
	private String selectableString;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ctx = sce.getServletContext();
		ss = StartupStatus.getBean(ctx);
		props = ConfigurationProperties.getBean(sce);

		readProperties();

		if (isForcing() && hasSelectables()) {
			warnAboutOverride();
		}

		if (isForcing()) {
			forceLocale();
		} else if (hasSelectables()) {
			setUpSelections();
		} else {
			reportNoLocales();
		}
	}

	private void readProperties() {
		forceString = props.getProperty(PROPERTY_FORCE_LOCALE, "");
		selectableString = props.getProperty(PROPERTY_SELECTABLE_LOCALES, "");
	}

	private boolean isForcing() {
		return StringUtils.isNotBlank(forceString);
	}

	private boolean hasSelectables() {
		return StringUtils.isNotBlank(selectableString);
	}

	private void warnAboutOverride() {
		ss.warning(this, "'" + PROPERTY_FORCE_LOCALE + "' will override '"
				+ PROPERTY_SELECTABLE_LOCALES + "'.");
	}

	private void forceLocale() {
		try {
			Locale forceLocale = buildLocale(forceString);
			SelectedLocale.setForcedLocale(ctx, forceLocale);
			ssInfo("Setting forced locale to '" + forceLocale + "'.");
		} catch (IllegalArgumentException e) {
			ssWarning("Problem in '" + PROPERTY_FORCE_LOCALE + "': "
					+ e.getMessage());
		}
	}

	private void setUpSelections() {
		List<Locale> locales = new ArrayList<Locale>();
		for (String string : splitSelectables()) {
			try {
				locales.add(buildLocale(string));
			} catch (IllegalArgumentException e) {
				ssWarning("Problem in '" + PROPERTY_SELECTABLE_LOCALES + "': "
						+ e.getMessage());
			}
		}

		SelectedLocale.setSelectableLocales(ctx, locales);
		ssInfo("Setting selectable locales to '" + locales + "'.");
	}

	private String[] splitSelectables() {
		return selectableString.split("\\s*,\\s*");
	}

	private void reportNoLocales() {
		ssInfo("There is no Locale information.");
	}

	private void ssInfo(String message) {
		ss.info(this, message + showPropertyValues());
	}

	private void ssWarning(String message) {
		ss.warning(this, message + showPropertyValues());
	}

	private String showPropertyValues() {
		return " In runtime.properties, '" + PROPERTY_FORCE_LOCALE
				+ "' is set to '" + forceString + "', '"
				+ PROPERTY_SELECTABLE_LOCALES + "' is set to '"
				+ selectableString + "'";
	}

	private Locale buildLocale(String localeString)
			throws IllegalArgumentException {

		// Replicate exception from lang2 with empty strings
		if ("".equals(localeString)) {
			throw new IllegalArgumentException("Invalid locale format");
		}

		Locale locale = LocaleUtils.toLocale(localeString);

		if (!"es_GO".equals(localeString) && // No complaint about bogus locale
				!LocaleUtils.isAvailableLocale(locale)) {
			ssWarning("'" + locale + "' is not a recognized locale.");
		}
		return locale;
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// Nothing to do at shutdown.
	}

}
