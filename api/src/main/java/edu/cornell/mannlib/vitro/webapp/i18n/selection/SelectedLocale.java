/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.i18n.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

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

		Optional<Locale> forcedLocale = getForcedLocale(ctxInfo);
		if (forcedLocale.isPresent()) {
			return forcedLocale.get();
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

		Locale fallbackLocale = getFallbackLocale();
        log.debug("Using fallback locale as default: " + fallbackLocale);
        return fallbackLocale;
	}

	/**
	 * Get the overriding Locale to use, which is the first of these to be found:
	 * <ul>
	 * <li>The forced Locale in the servlet context</li>
	 * <li>The first selectable locale matching a preferred locale</li>
	 * <li>The first of the preferred locale</li>
	 * <li>null</li>
	 * </ul>
	 */
	public static Locale getOverridingLocale(ServletContext ctx, List<Locale> preferredLocales) {
		Object ctxInfo = ctx.getAttribute(ATTRIBUTE_NAME);
		Optional<Locale> forcedLocale = getForcedLocale(ctxInfo);
		if (forcedLocale.isPresent()) {
			return forcedLocale.get();
		}

		if (ctxInfo instanceof ContextSelectedLocale) {
			List<Locale> selectableLocales = ((ContextSelectedLocale) ctxInfo)
					.getSelectableLocales();

			if (Objects.nonNull(selectableLocales) && Objects.nonNull(preferredLocales)) {
				for (Locale preferredLocal : preferredLocales) {
					for (Locale selectableLocale : selectableLocales) {
						if (selectableLocale.equals(preferredLocal)) {
							log.debug("Using first matching selectable locale from context: "
									+ selectableLocale);
							return selectableLocale;
						}
					}
				}
			}
		}

		if (Objects.nonNull(preferredLocales) && !preferredLocales.isEmpty()) {
			Locale preferredLocal = preferredLocales.get(0);
			log.debug("Using first preferred locale as default: "
					+ preferredLocal);
			return preferredLocal;
		}

		Locale fallbackLocale = getFallbackLocale();
		log.debug("Using fallback locale as default: " + fallbackLocale);
		return fallbackLocale;
	}
	
	/**
	 * @return a default locale to use if no other criteria for selecting a 
	 * different one exist.
	 */
	public static Locale getFallbackLocale() {
	    return new Locale("en", "US");
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
	 * Get the preferred Locale to use, which is the first of these to be found:
	 * <ul>
	 * <li>The forced Locale in the servlet context</li>
	 * <li>The first selectable locale matching a preferred locale</li>
	 * <li>The first of the preferred locale</li>
	 * <li>The default Locale for the JVM</li>
	 * </ul>
	 */
	public static Locale getPreferredLocale(ServletContext ctx, List<Locale> preferredLocales) {
		Locale overridingLocale = getOverridingLocale(ctx, preferredLocales);

		if (overridingLocale != null) {
			return overridingLocale;
		}

		log.debug("Using default locale: " + Locale.getDefault());
		return Locale.getDefault();
	}

	/**
	 * Check for forced locale on the context.
	 */
	private static Optional<Locale> getForcedLocale(Object ctxInfo) {
		if (ctxInfo instanceof ContextSelectedLocale) {
			Locale forcedLocale = ((ContextSelectedLocale) ctxInfo)
					.getForcedLocale();
			if (forcedLocale != null) {
				log.debug("Found forced locale in the context: " + forcedLocale);
				return Optional.of(forcedLocale);
			}
		}

		return Optional.empty();
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
