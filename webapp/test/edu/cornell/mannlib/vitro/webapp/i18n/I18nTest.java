package edu.cornell.mannlib.vitro.webapp.i18n;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import stubs.javax.servlet.ServletContextStub;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.i18n.selection.SelectedLocale;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

/**
 * Test the I18N functionality.
 * 
 * Start by checking the logic that finds approximate matches for
 * language-specific property files.
 */
public class I18nTest extends AbstractTestClass {
	private static final List<Locale> SELECTABLE_LOCALES = locales("es_MX",
			"en_US");

	ServletContextStub ctx;

	@Before
	public void setup() {
		ctx = new ServletContextStub();
	}

	@Test
	public void noMatchOnLanguageRegion() {
		assertLocales("fr_CA", SELECTABLE_LOCALES, "fr_CA", "fr", "");
	}

	@Test
	public void noMatchOnLanguage() {
		assertLocales("fr", SELECTABLE_LOCALES, "fr", "");
	}

	@Test
	public void noMatchOnRoot() {
		assertLocales("", SELECTABLE_LOCALES, "");
	}

	@Test
	public void matchOnLanguageRegion() {
		assertLocales("es_ES", SELECTABLE_LOCALES, "es_ES", "es", "es_MX", "");
	}

	@Test
	public void matchOnLanguage() {
		assertLocales("es", SELECTABLE_LOCALES, "es", "es_MX", "");
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void assertLocales(String requested, List<Locale> selectable,
			String... expected) {
		SelectedLocale.setSelectableLocales(ctx, selectable);
		List<Locale> expectedLocales = locales(expected);

		I18n.ThemeBasedControl control = new I18n.ThemeBasedControl(ctx,
				"bogusThemeDirectory");
		List<Locale> actualLocales = control.getCandidateLocales(
				"bogusBaseName", locale(requested));

		assertEquals("Expected locales", expectedLocales, actualLocales);
	}

	private static List<Locale> locales(String... strings) {
		List<Locale> locales = new ArrayList<>();
		for (String s : strings) {
			locales.add(locale(s));
		}
		return locales;
	}

	private static Locale locale(String s) {
		String[] parts = s.split("_");
		String language = (parts.length > 0) ? parts[0] : "";
		String country = (parts.length > 1) ? parts[1] : "";
		String variant = (parts.length > 2) ? parts[2] : "";
		return new Locale(language, country, variant);
	}

}
