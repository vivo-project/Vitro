/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.i18n;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.i18n.selection.SelectedLocale;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.developer.DeveloperSettings;
import edu.cornell.mannlib.vitro.webapp.utils.developer.Key;

/**
 * Provides access to a bundle of text strings, based on the name of the bundle,
 * the Locale of the requesting browser, and the current theme directory.
 *
 * If the bundle name is not specified, the default name of "all" is used.
 *
 * If a requested bundle is not found, no error is thrown. Instead, an empty
 * bundle is returned that produces error message strings when asked for text.
 */
public class I18n {
	private static final Log log = LogFactory.getLog(I18n.class);

	/**
	 * If this attribute is present on the request, then the cache has already
	 * been cleared.
	 */
	private static final String ATTRIBUTE_CACHE_CLEARED = I18n.class.getName()
			+ "-cacheCleared";

	/**
	 * This is where the work gets done. Not declared final, so it can be
	 * modified in unit tests.
	 */
	private static I18n instance;

	private final ServletContext ctx;

	protected I18n(ServletContext ctx) {
		this.ctx = ctx;
	}

	// ----------------------------------------------------------------------
	// Static methods
	// ----------------------------------------------------------------------

	/**
	 * This setup method must be called before I18n static methods can be used.
	 * It is currently called from LocaleSelectionSetup.contextInitialized, which
	 * should ensure it is called when the context is initialized.
	 */
	public static void setup(ServletContext ctx) {
		I18n.instance = new I18n(ctx);
	}

	/**
	 * A convenience method to get the default bundle and format the text.
	 */
	public static String text(HttpServletRequest req, String key,
			Object... parameters) {
		return bundle(req).text(key, parameters);
	}

	/**
	 * Get the default request I18nBundle.
	 */
	public static I18nBundle bundle(HttpServletRequest req) {
		return instance.getBundle(req);
	}

	/**
	 * Get the default context I18nBundle for preferred locales.
	 */
	public static I18nBundle bundle(List<Locale> preferredLocales) {
		return instance.getBundle(preferredLocales);
	}

	// ----------------------------------------------------------------------
	// The instance
	// ----------------------------------------------------------------------

	/** Holds the current theme directory, as far as we know. */
	private AtomicReference<String> themeDirectory = new AtomicReference<String>(
			"");

	/**
	 * Get an I18nBundle by this name. The request provides the preferred
	 * Locale, the application directory, the theme directory and the
	 * development mode flag.
	 *
	 * If the request indicates that the system is in development mode, then the
	 * cache is cleared on each request.
	 *
	 * If the theme directory has changed, the cache is cleared.
	 *
	 * Declared 'protected' so it can be overridden in unit tests.
	 */
	protected I18nBundle getBundle(HttpServletRequest req) {
		checkDevelopmentMode(req);
		checkForChangeInThemeDirectory(req);
		Locale locale = req.getLocale();
		return new I18nSemanticBundle(Collections.singletonList(locale));
	}

	/**
	 * Get an I18nBundle by this name. The context provides the selectable
	 * Locale, the application directory, the theme directory and the
	 * development mode flag. Choosing matching locale from context by
	 * provided preferred locales.
	 *
	 * If the context indicates that the system is in development mode, then the
	 * cache is cleared on each request.
	 *
	 * If the theme directory has changed, the cache is cleared.
	 *
	 * Declared 'protected' so it can be overridden in unit tests.
	 */
	protected I18nBundle getBundle( List<Locale> preferredLocales) {
		checkDevelopmentMode();
		checkForChangeInThemeDirectory(ctx);
		Locale locale = SelectedLocale.getPreferredLocale(ctx, preferredLocales);
		return new I18nSemanticBundle(Collections.singletonList(locale));
	}

	/**
	 * If we are in development mode, clear the cache on each request.
	 */
	private void checkDevelopmentMode(HttpServletRequest req) {
		if (DeveloperSettings.getInstance().getBoolean(Key.I18N_DEFEAT_CACHE)) {
			log.debug("In development mode - clearing the cache.");
			clearCacheOnRequest(req);
		}
	}

	/**
	 * If we are in development mode, clear the cache.
	 */
	private void checkDevelopmentMode() {
		if (DeveloperSettings.getInstance().getBoolean(Key.I18N_DEFEAT_CACHE)) {
			log.debug("In development mode - clearing the cache.");
			clearCache();
		}
	}

	/**
	 * If the theme directory has changed from before, clear the cache of all
	 * ResourceBundles.
	 */
	private void checkForChangeInThemeDirectory(HttpServletRequest req) {
		String currentDir = new VitroRequest(req).getAppBean().getThemeDir();
		String previousDir = themeDirectory.getAndSet(currentDir);
		if (!currentDir.equals(previousDir)) {
			log.debug("Theme directory changed from '" + previousDir + "' to '"
					+ currentDir + "' - clearing the cache.");
			clearCacheOnRequest(req);
		}
	}

	/**
	 * If we have a complete model access and the theme directory has changed
	 * from before, clear the cache of all ResourceBundles.
	 */
	private void checkForChangeInThemeDirectory(ServletContext ctx) {
		WebappDaoFactory wdf = ModelAccess.on(ctx)
			.getWebappDaoFactory();
		// Only applicable if context has a complete model access
		if (Objects.nonNull(wdf)
				&& Objects.nonNull(wdf.getApplicationDao())
				&& Objects.nonNull(wdf.getApplicationDao().getApplicationBean())) {
			String currentDir = wdf
				.getApplicationDao()
				.getApplicationBean()
				.getThemeDir();
			String previousDir = themeDirectory.getAndSet(currentDir);
			if (!currentDir.equals(previousDir)) {
				log.debug("Theme directory changed from '" + previousDir + "' to '"
						+ currentDir + "' - clearing the cache.");
				clearCache();
			}
		}
	}

	private void clearCache() {
		TranslationProvider.getInstance().clearCache();
	}

	/** Only clear the cache one time per request. */
	private void clearCacheOnRequest(HttpServletRequest req) {
		if (req.getAttribute(ATTRIBUTE_CACHE_CLEARED) != null) {
			log.debug("Cache was already cleared on this request.");
		} else {
			clearCache();
			log.debug("Cache cleared.");
			req.setAttribute(ATTRIBUTE_CACHE_CLEARED, Boolean.TRUE);
		}
	}
	
}
