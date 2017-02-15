/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.i18n.selection;

import static edu.cornell.mannlib.vitro.webapp.i18n.selection.LocaleSelectionSetup.PROPERTY_FORCE_LOCALE;
import static edu.cornell.mannlib.vitro.webapp.i18n.selection.LocaleSelectionSetup.PROPERTY_SELECTABLE_LOCALES;
import static edu.cornell.mannlib.vitro.webapp.i18n.selection.SelectedLocale.ATTRIBUTE_NAME;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContextEvent;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import stubs.edu.cornell.mannlib.vitro.webapp.config.ConfigurationPropertiesStub;
import stubs.edu.cornell.mannlib.vitro.webapp.startup.StartupStatusStub;
import stubs.javax.servlet.ServletContextStub;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.i18n.selection.SelectedLocale.ContextSelectedLocale;

/**
 * TODO
 */
public class LocaleSelectionSetupTest extends AbstractTestClass {
	// ----------------------------------------------------------------------
	// Infrastructure
	// ----------------------------------------------------------------------

	private LocaleSelectionSetup lss;
	private ServletContextStub ctx;
	private ServletContextEvent sce;
	private ConfigurationPropertiesStub props;
	private StartupStatusStub ss;

	private int[] expectedMessageCounts;
	private Locale expectedForcedLocale;
	private List<Locale> expectedSelectableLocales;

	@Before
	public void setup() {
//		setLoggerLevel(LocaleSelectionSetup.class, Level.DEBUG);
//		setLoggerLevel(StartupStatusStub.class, Level.DEBUG);
		setLoggerLevel(ConfigurationProperties.class, Level.WARN);

		ctx = new ServletContextStub();
		sce = new ServletContextEvent(ctx);

		props = new ConfigurationPropertiesStub();
		props.setBean(ctx);

		ss = new StartupStatusStub(ctx);

		lss = new LocaleSelectionSetup();
	}

	@After
	public void checkExpectations() {
		if (expectedMessageCounts == null) {
			fail("expectedMessages() was not called");
		}

		String message = compareMessageCount("info", ss.getInfoCount(),
				expectedMessageCounts[0])
				+ compareMessageCount("warning", ss.getWarningCount(),
						expectedMessageCounts[1])
				+ compareMessageCount("fatal", ss.getFatalCount(),
						expectedMessageCounts[2])
				+ checkForced()
				+ checkSelectable();
		if (!message.isEmpty()) {
			fail(message);
		}
	}

	private String compareMessageCount(String label, int actual, int expected) {
		if (expected == actual) {
			return "";
		} else {
			return "expecting " + expected + " " + label
					+ " messages, but received " + actual + "; ";
		}
	}

	private String checkForced() {
		Locale actual = null;
		Object o = ctx.getAttribute(ATTRIBUTE_NAME);
		if (o instanceof ContextSelectedLocale) {
			actual = ((ContextSelectedLocale) o).getForcedLocale();
		}

		Locale expected = expectedForcedLocale;
		if (ObjectUtils.equals(expected, actual)) {
			return "";
		} else {
			return "expected forced locale of " + expectedForcedLocale
					+ ", but was " + actual + "; ";
		}
	}

	private String checkSelectable() {
		List<Locale> actual = Collections.emptyList();
		Object o = ctx.getAttribute(ATTRIBUTE_NAME);
		if (o instanceof ContextSelectedLocale) {
			actual = ((ContextSelectedLocale) o).getSelectableLocales();
		}

		List<Locale> expected = expectedSelectableLocales;
		if (expected == null) {
			expected = Collections.emptyList();
		}

		if (ObjectUtils.equals(expected, actual)) {
			return "";
		} else {
			return "expected selectable locales of " + expected + ", but was "
					+ actual + "; ";
		}
	}

	// ----------------------------------------------------------------------
	// The tests
	// ----------------------------------------------------------------------

	// General functionality

	@Test
	public void neitherPropertyIsSpecified() {
		lss.contextInitialized(sce);
		expectMessages(1, 0, 0);
	}

	@Test
	public void forceSuccessL() {
		props.setProperty(PROPERTY_FORCE_LOCALE, "es");
		lss.contextInitialized(sce);
		expectForced("es");
		expectMessages(1, 0, 0);
	}
	
	@Test
	public void forceSuccessL_C() {
		props.setProperty(PROPERTY_FORCE_LOCALE, "es_ES");
		lss.contextInitialized(sce);
		expectForced("es_ES");
		expectMessages(1, 0, 0);
	}
	
	@Test
	public void forceSuccessL_C_V() {
		props.setProperty(PROPERTY_FORCE_LOCALE, "no_NO_NY");
		lss.contextInitialized(sce);
		expectForced("no_NO_NY");
		expectMessages(1, 0, 0);
	}

	@Test
	public void oneSelectable() {
		props.setProperty(PROPERTY_SELECTABLE_LOCALES, "fr_FR");
		lss.contextInitialized(sce);
		expectSelectable("fr_FR");
		expectMessages(1, 0, 0);
	}

	@Test
	public void twoSelectables() {
		props.setProperty(PROPERTY_SELECTABLE_LOCALES, "fr_FR, es_PE");
		lss.contextInitialized(sce);
		expectSelectable("fr_FR", "es_PE");
		expectMessages(1, 0, 0);
	}

	@Test
	public void bothPropertiesAreSpecified() {
		props.setProperty(PROPERTY_FORCE_LOCALE, "es_ES");
		props.setProperty(PROPERTY_SELECTABLE_LOCALES, "fr_FR");
		lss.contextInitialized(sce);
		expectForced("es_ES");
		expectMessages(1, 1, 0);
	}

	// Locale string syntax (common to both force and selectable)

	@Test
	public void langaugeIsEmpty() {
		props.setProperty(PROPERTY_FORCE_LOCALE, "_ES");
		lss.contextInitialized(sce);
		expectForced("_ES");
		expectMessages(1, 1, 0);
	}

	@Test
	public void languageWrongLength() {
		props.setProperty(PROPERTY_FORCE_LOCALE, "e_ES");
		lss.contextInitialized(sce);
		expectMessages(0, 1, 0);
	}

	@Test
	public void languageNotAlphabetic() {
		props.setProperty(PROPERTY_FORCE_LOCALE, "e4_ES");
		lss.contextInitialized(sce);
		expectMessages(0, 1, 0);
	}

	@Test
	public void languageNotLowerCase() {
		props.setProperty(PROPERTY_FORCE_LOCALE, "eS_ES");
		lss.contextInitialized(sce);
		expectMessages(0, 1, 0);
	}

	@Test
	public void countryIsEmpty() {
		props.setProperty(PROPERTY_FORCE_LOCALE, "es_ _13");
		lss.contextInitialized(sce);
		expectMessages(0, 1, 0);
	}

	@Test
	public void countryWrongLength() {
		props.setProperty(PROPERTY_FORCE_LOCALE, "es_ESS");
		lss.contextInitialized(sce);
		expectMessages(0, 1, 0);
	}

	@Test
	public void countryNotAlphabetic() {
		props.setProperty(PROPERTY_FORCE_LOCALE, "es_E@");
		lss.contextInitialized(sce);
		expectMessages(0, 1, 0);
	}

	@Test
	public void countryNotUpperCase() {
		props.setProperty(PROPERTY_FORCE_LOCALE, "es_es");
		lss.contextInitialized(sce);
		expectMessages(0, 1, 0);
	}

	@Test
	public void variantIsEmpty() {
		props.setProperty(PROPERTY_FORCE_LOCALE, "es_ES_");
		lss.contextInitialized(sce);
		expectMessages(0, 1, 0);
	}

	// This shouldn't really be acceptable, so we won't test that it is
//	@Test
//	public void funkyVariantIsAcceptable() {
//		props.setProperty(PROPERTY_FORCE_LOCALE, "es_ES_123_aa");
//		lss.contextInitialized(sce);
//		expectForced("es_ES_123_aa");
//		expectMessages(1, 1, 0);
//	}

	@Test
	public void localeNotRecognizedProducesWarning() {
		props.setProperty(PROPERTY_FORCE_LOCALE, "es_FR");
		lss.contextInitialized(sce);
		expectForced("es_FR");
		expectMessages(1, 1, 0);
	}

	// Syntax of selectable property

	@Test
	public void emptySelectableLocaleProducesWarning() {
		props.setProperty(PROPERTY_SELECTABLE_LOCALES, "es_ES, , fr_FR");
		lss.contextInitialized(sce);
		expectSelectable("es_ES", "fr_FR");
		expectMessages(1, 1, 0);
	}

	@Test
	public void blanksAroundCommasAreIgnored() {
		props.setProperty(PROPERTY_SELECTABLE_LOCALES, "es_ES,en_US   \t ,      fr_FR");
		lss.contextInitialized(sce);
		expectSelectable("es_ES", "en_US", "fr_FR");
		expectMessages(1, 0, 0);
	}

	// ----------------------------------------------------------------------
	// helper methods
	// ----------------------------------------------------------------------

	private void expectMessages(int infoCount, int warningCount, int fatalCount) {
		this.expectedMessageCounts = new int[] { infoCount, warningCount,
				fatalCount };
	}

	private void expectForced(String localeString) {
		this.expectedForcedLocale = stringToLocale(localeString);
	}

	private void expectSelectable(String... strings) {
		List<Locale> list = new ArrayList<Locale>();
		for (String string : strings) {
			list.add(stringToLocale(string));
		}
		this.expectedSelectableLocales = list;
	}

	private Locale stringToLocale(String string) {
		return LocaleUtils.toLocale(string);
	}
}
