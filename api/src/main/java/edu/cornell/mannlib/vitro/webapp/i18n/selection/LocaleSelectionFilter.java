/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.i18n.selection;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Check for a Locale in the ServletContext or the Session that should override
 * the Locale in the ServletRequest.
 * 
 * If there is such a Locale, wrap the ServletRequest so it behaves as if that
 * is the preferred Locale.
 * 
 * Otherwise, just process the request as usual.
 */
public class LocaleSelectionFilter implements Filter {
	private static final Log log = LogFactory
			.getLog(LocaleSelectionFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// Nothing to do at startup.
	}

	@Override
	public void destroy() {
		// Nothing to do at shutdown.
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			HttpServletRequest hreq = (HttpServletRequest) request;

			Locale overridingLocale = SelectedLocale.getOverridingLocale(hreq);
			log.debug("overriding Locale is " + overridingLocale);

			if (overridingLocale != null) {
				request = new LocaleSelectionRequestWrapper(hreq,
						overridingLocale);
			}
		} else {
			log.debug("Not an HttpServletRequest.");
		}
		chain.doFilter(request, response);
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	/**
	 * Uses the selected Locale as the only acceptable Locale of the request.
	 */
	private static class LocaleSelectionRequestWrapper extends
			HttpServletRequestWrapper {
		private final Locale selectedLocale;

		public LocaleSelectionRequestWrapper(HttpServletRequest request,
				Locale selectedLocale) {
			super(request);

			if (request == null) {
				throw new NullPointerException("request may not be null.");
			}
			if (selectedLocale == null) {
				throw new NullPointerException(
						"selectedLocale may not be null.");
			}
			
			this.selectedLocale = selectedLocale;
		}

		@Override
		public Locale getLocale() {
			return selectedLocale;
		}

		/**
		 * Get the modified list of locales.
		 */
		@SuppressWarnings("rawtypes")
		@Override
		public Enumeration getLocales() {
			return Collections.enumeration(Arrays.asList(selectedLocale));
		}
	}

}
