/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.i18n.selection;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DisplayMessage;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;

/**
 * {@code
 * Call this at /selectLocale&selection=[locale_string]
 * 
 * For example: /selectLocale&selection=en_US or /selectLocale&selection=es
 * }
 * Write an error to the log (and to DisplayMessage) if the selection is not
 * syntactically valid.
 * 
 * Write a warning to the log if the selection code is not one of the selectable
 * Locales from runtime.properties, or if the selection code is not recognized
 * by the system.
 * 
 * Set the new Locale in the Session using SelectedLocale and return to the
 * referrer.
 */
public class LocaleSelectionController extends HttpServlet {
	private static final Log log = LogFactory
			.getLog(LocaleSelectionController.class);

	public static final String PARAMETER_SELECTION = "selection";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String referrer = req.getHeader("referer");

		String selectedLocale = req.getParameter(PARAMETER_SELECTION);

		try {
			processSelectedLocale(req, selectedLocale);
		} catch (Exception e) {
			log.error("Failed to process the user's Locale selection", e);
		}

		if (StringUtils.isEmpty(referrer)) {
			resp.sendRedirect(UrlBuilder.getHomeUrl());
		} else {
			resp.sendRedirect(referrer);
		}
	}

	private void processSelectedLocale(HttpServletRequest req,
			String selectedLocale) {
		if (StringUtils.isBlank(selectedLocale)) {
			log.debug("No '" + PARAMETER_SELECTION + "' parameter");
			return;
		}

		Locale locale = null;

		try {
			locale = LocaleUtils.toLocale(selectedLocale.trim());
			log.debug("Locale selection is " + locale);
		} catch (IllegalArgumentException e) {
			log.error("Failed to convert the selection to a Locale", e);
			DisplayMessage.setMessage(req,
					I18n.bundle(req).text("language_selection_failed"));
			return;
		}

		List<Locale> selectables = SelectedLocale.getSelectableLocales(req);
		if (!selectables.contains(locale)) {
			log.warn("User selected a locale '" + locale
					+ "' that was not in the list: " + selectables);
		} else if (!LocaleUtils.isAvailableLocale(locale)) {
			log.warn("User selected an unrecognized locale: '" + locale + "'");
		}

		SelectedLocale.setSelectedLocale(req, locale);
		log.debug("Setting selected locale to " + locale);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}
