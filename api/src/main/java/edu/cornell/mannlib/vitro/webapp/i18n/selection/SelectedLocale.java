/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.i18n.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A utility class for storing and retrieving Locale information.
 * 
 * The static methods create beans and store them in the ServletContext or the
 * session, where the information can be found later.
 */
public abstract class SelectedLocale {
	private static final Log log = LogFactory.getLog(SelectedLocale.class);

	/** Use this attribute on both the ServletContext and the Session. */
	protected static final String ATTRIBUTE_NAME = "SELECTED_LOCALE";

	/**
	 * Store the forced locale in the servlet context. Clear any selectable
	 * Locales.
	 */
	public static void setForcedLocale(ServletContext ctx, Locale forcedLocale) {
		log.debug("Set forced locale: " + forcedLocale);
		ctx.setAttribute(ATTRIBUTE_NAME,
				new ContextSelectedLocale(forcedLocale));
	}

	/**
	 * Store the selected locale in the current session.
	 */
	public static void setSelectedLocale(HttpServletRequest req,
			Locale selectedLocale) {
		log.debug("Set selected locale: " + selectedLocale);
		req.getSession().setAttribute(ATTRIBUTE_NAME,
				new SessionSelectedLocale(selectedLocale));
	}

	/**
	 * Do we need to override the Locale in the current request? return the
	 * first of these to be found:
	 * <ul>
	 * <li>The forced Locale in the servlet context</li>
	 * <li>The selected Locale in the session</li>
	 * <li>The first of the selectable Locales</li>
	 * <li>null</li>
	 * </ul>
	 */
	public static Locale getOverridingLocale(HttpServletRequest req) {
		HttpSession session = req.getSession();
		ServletContext ctx = session.getServletContext();

		Object ctxInfo = ctx.getAttribute(ATTRIBUTE_NAME);
		if (ctxInfo instanceof ContextSelectedLocale) {
			Locale forcedLocale = ((ContextSelectedLocale) ctxInfo)
					.getForcedLocale();
			if (forcedLocale != null) {
				log.debug("Found forced locale in the context: " + forcedLocale);
				return forcedLocale;
			}
		}

		Object sessionInfo = session.getAttribute(ATTRIBUTE_NAME);
		if (sessionInfo instanceof SessionSelectedLocale) {
			Locale selectedLocale = ((SessionSelectedLocale) sessionInfo)
					.getSelectedLocale();
			if (selectedLocale != null) {
				log.debug("Found selected locale in the session: "
						+ selectedLocale);
				return selectedLocale;
			}
		}

		if (ctxInfo instanceof ContextSelectedLocale) {
			List<Locale> selectableLocales = ((ContextSelectedLocale) ctxInfo)
					.getSelectableLocales();
			if (selectableLocales != null && !selectableLocales.isEmpty()) {
				Locale defaultLocale = selectableLocales.get(0);
				log.debug("Using first selectable locale as default: "
						+ defaultLocale);
				return defaultLocale;
			}
		}

		return null;
	}

	/**
	 * Get the current Locale to use, which is the first of these to be found:
	 * <ul>
	 * <li>The forced Locale in the servlet context</li>
	 * <li>The selected Locale in the session</li>
	 * <li>The Locale from the request</li>
	 * <li>The default Locale for the JVM</li>
	 * </ul>
	 */
	public static Locale getCurrentLocale(HttpServletRequest req) {
		Locale overridingLocale = getOverridingLocale(req);

		if (overridingLocale != null) {
			return overridingLocale;
		}

		Locale requestLocale = req.getLocale();
		if (requestLocale != null) {
			log.debug("Found locale in the request: " + requestLocale);
			return requestLocale;
		}

		log.debug("Using default locale: " + Locale.getDefault());
		return Locale.getDefault();
	}

	/**
	 * Store a list of selectable Locales in the servlet context, so we can
	 * easily build the selection panel in the GUI. Clears any forced locale.
	 */
	public static void setSelectableLocales(ServletContext ctx,
			List<Locale> selectableLocales) {
		log.debug("Setting selectable locales: " + selectableLocales);
		ctx.setAttribute(ATTRIBUTE_NAME, new ContextSelectedLocale(
				selectableLocales));
	}

	/**
	 * Get the list of selectable Locales from the servlet context. May return
	 * an empty list, but never returns null.
	 */
	public static List<Locale> getSelectableLocales(HttpServletRequest req) {
		return getSelectableLocales(req.getSession().getServletContext());
	}

	/**
	 * Get the list of selectable Locales from the servlet context. May return
	 * an empty list, but never returns null.
	 */
	public static List<Locale> getSelectableLocales(ServletContext ctx) {
		Object ctxInfo = ctx.getAttribute(ATTRIBUTE_NAME);
		if (ctxInfo instanceof ContextSelectedLocale) {
			List<Locale> selectableLocales = ((ContextSelectedLocale) ctxInfo)
					.getSelectableLocales();
			if (selectableLocales != null) {
				log.debug("Returning selectable locales: " + selectableLocales);
				return selectableLocales;
			}
		}

		log.debug("No selectable locales were found. Returning an empty list.");
		return Collections.emptyList();
	}

	// ----------------------------------------------------------------------
	// Bean classes
	// ----------------------------------------------------------------------

	/** Holds Locale information in the ServletContext. */
	protected static class ContextSelectedLocale {
		// Only one of these is populated.
		private final Locale forcedLocale;
		private final List<Locale> selectableLocales;

		public ContextSelectedLocale(Locale forcedLocale) {
			if (forcedLocale == null) {
				throw new NullPointerException("forcedLocale may not be null.");
			}

			this.forcedLocale = forcedLocale;
			this.selectableLocales = Collections.emptyList();
		}

		public ContextSelectedLocale(List<Locale> selectableLocales) {
			if (selectableLocales == null) {
				selectableLocales = Collections.emptyList();
			}

			this.forcedLocale = null;
			this.selectableLocales = Collections
					.unmodifiableList(new ArrayList<>(selectableLocales));
		}

		public Locale getForcedLocale() {
			return forcedLocale;
		}

		public List<Locale> getSelectableLocales() {
			return selectableLocales;
		}

		@Override
		public String toString() {
			return "ContextSelectedLocale[forced=" + forcedLocale
					+ ", selectable=" + selectableLocales + "]";
		}

	}

	/** Holds Locale information in the Session. */
	protected static class SessionSelectedLocale {
		private final Locale selectedLocale;

		public SessionSelectedLocale(Locale selectedLocale) {
			this.selectedLocale = selectedLocale;
		}

		public Locale getSelectedLocale() {
			return selectedLocale;
		}

		@Override
		public String toString() {
			return "SessionSelectedLocale[" + selectedLocale + "]";
		}

	}
}
