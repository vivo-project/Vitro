/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.freemarker.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.SortedSet;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.freemarker.loader.FreemarkerTemplateLoader.PathPieces;
import edu.cornell.mannlib.vitro.webapp.freemarker.loader.FreemarkerTemplateLoader.PathPiecesFileVisitor;

/**
 * TODO
 */
public class FreemarkerTemplateLoaderTest {
	private PathPiecesFileVisitor visitor;
	private String[] paths;

	// ----------------------------------------------------------------------
	// PathPieces tests
	// ----------------------------------------------------------------------

	@Test
	public void ppLanguageRegionExtension() {
		assertPathPieces("this_en_US.ftl", "this", "_en", "_US", ".ftl");
	}

	@Test
	public void ppLanguageRegion() {
		assertPathPieces("this_en_US", "this", "_en", "_US", "");
	}

	@Test
	public void ppLanguageExtension() {
		assertPathPieces("this_en.ftl", "this", "_en", "", ".ftl");
	}

	@Test
	public void ppLanguage() {
		assertPathPieces("this_en", "this", "_en", "", "");
	}

	@Test
	public void ppDefaultExtension() {
		assertPathPieces("this.ftl", "this", "", "", ".ftl");
	}

	@Test
	public void ppDefault() {
		assertPathPieces("this", "this", "", "", "");
	}

	@Test
	public void ppExtraUnderscoreExtension() {
		assertPathPieces("woo_hoo_en_US.ftl", "woo_hoo", "_en", "_US", ".ftl");
	}

	@Test
	public void ppExtraUnderscore() {
		assertPathPieces("woo_hoo_en_US", "woo_hoo", "_en", "_US", "");
	}

	// ----------------------------------------------------------------------
	// Specific function tests
	// ----------------------------------------------------------------------

	@Test
	public void baseAndExtensionMatch() {
		paths("match-me.ftl");
		assertMatches("match-me.ftl", 1, "match-me.ftl");
	}

	@Test
	public void baseAndExtensionDontMatch() {
		paths("match-me.ftl");
		assertMatches("fail.ftl", 0, null);
		assertMatches("match-me", 0, null);
		assertMatches("match-me.FTL", 0, null);
	}

	@Test
	public void matchRegardlessOfDepth() {
		paths("short-path.ftl", "long/long-path.ftl");
		assertMatches("long/short-path.ftl", 1, "short-path.ftl");
		assertMatches("long-path.ftl", 1, "long/long-path.ftl");
	}

	@Test
	public void preferShorterPath() {
		paths("shorter-is-better", "long/shorter-is-better");
		assertMatches("shorter-is-better", 2, "shorter-is-better");
	}

	@Test
	public void preferShorterPathToExactPath() {
		paths("shorter-is-better", "long/shorter-is-better");
		assertMatches("long/shorter-is-better", 2, "shorter-is-better");
	}

	@Test
	public void languageAndRegionMustMatchExactly() {
		paths("this_es_MX.ftl", "this_es_ES.ftl", "this_es.ftl");
		assertMatches("this_es_ES.ftl", 1, "this_es_ES.ftl");
	}

	@Test
	public void languageAndRegionNoMatch() {
		paths("this_es_MX.ftl", "this_es_ES.ftl", "this_es.ftl");
		assertMatches("this_es_GO.ftl", 0, null);
	}

	@Test
	public void languagePrefersExactMatch() {
		paths("this_es_MX.ftl", "this_es.ftl", "this_es_ES.ftl");
		assertMatches("this_es.ftl", 3, "this_es.ftl");
	}

	@Test
	public void languageAcceptsApproximateMatch() {
		paths("this_es_MX.ftl");
		assertMatches("this_es.ftl", 1, "this_es_MX.ftl");
	}

	@Test
	public void languagePrefersApproximateAlphabetical() {
		paths("this_es_MX.ftl", "this_es_ES.ftl");
		assertMatches("this_es.ftl", 2, "this_es_ES.ftl");
	}

	@Test
	public void defaultPrefersExactMatch() {
		paths("this_fr.ftl", "this.ftl", "this_fr_BE.ftl");
		assertMatches("this.ftl", 3, "this.ftl");
	}

	@Test
	public void defaultPrefersDefaultRegion() {
		paths("this_fr_BE.ftl", "this_fr.ftl", "this_fr_CA.ftl");
		assertMatches("this.ftl", 3, "this_fr.ftl");
	}

	@Test
	public void defaultPrefersLanguageAlphabetical() {
		paths("this_es.ftl", "this_fr.ftl");
		assertMatches("this.ftl", 2, "this_es.ftl");
	}

	@Test
	public void defaultPrefersRegionAlphabetical() {
		paths("this_fr_BE.ftl", "this_fr_CA.ftl");
		assertMatches("this.ftl", 2, "this_fr_BE.ftl");
	}

	// ----------------------------------------------------------------------
	// Freemarker simulation tests
	// ----------------------------------------------------------------------

	public static final String[] FREEMARKER_TEST_PATHS = {
			"long/this_fr_BE.ftl", "language_fr.ftl", "default.ftl",
			"language-approx_en_US.ftl" };

	@Test
	public void freemarkerLangAndRegionExact() {
		paths = FREEMARKER_TEST_PATHS;
		assertFM("this_fr_BE.ftl", 1, "long/this_fr_BE.ftl");
	}

	@Test
	public void freemarkerLangAndRegionMatchLang() {
		paths = FREEMARKER_TEST_PATHS;
		assertFM("language_fr_CA.ftl", 2, "language_fr.ftl");
	}

	@Test
	public void freemarkerLangAndRegionMatchDefault() {
		paths = FREEMARKER_TEST_PATHS;
		assertFM("default_es_ES.ftl", 3, "default.ftl");
	}

	@Test
	public void freemarkerLangAndRegionNoMatch() {
		paths = FREEMARKER_TEST_PATHS;
		assertFM("bogus_en_US.ftl", 3, null);
	}

	@Test
	public void freemarkerLangExact() {
		paths = FREEMARKER_TEST_PATHS;
		assertFM("language_fr.ftl", 1, "language_fr.ftl");
	}

	@Test
	public void freemarkerLangMatchLangAndRegion() {
		paths = FREEMARKER_TEST_PATHS;
		assertFM("language-approx_en.ftl", 1, "language-approx_en_US.ftl");
	}

	@Test
	public void freemarkerLangMatchDefault() {
		paths = FREEMARKER_TEST_PATHS;
		assertFM("default_en.ftl", 2, "default.ftl");
	}

	@Test
	public void freemarkerLangNoMatch() {
		paths = FREEMARKER_TEST_PATHS;
		assertFM("bogus_it.ftl", 2, null);
	}

	@Test
	public void freemarkerDefaultExact() {
		paths = FREEMARKER_TEST_PATHS;
		assertFM("default.ftl", 1, "default.ftl");
	}

	@Test
	public void freemarkerDefaultMatchLang() {
		paths = FREEMARKER_TEST_PATHS;
		assertFM("language.ftl", 1, "language_fr.ftl");
	}

	@Test
	public void freemarkerDefaultMatchLangAndRegion() {
		paths = FREEMARKER_TEST_PATHS;
		assertFM("this.ftl", 1, "long/this_fr_BE.ftl");
	}

	@Test
	public void freemarkerDefaultNoMatch() {
		paths = FREEMARKER_TEST_PATHS;
		assertFM("bogus.ftl", 1, null);
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void paths(String... p) {
		this.paths = p;
	}

	private void assertPathPieces(String path, String base, String language,
			String region, String extension) {
		PathPieces pp = new PathPieces(path);
		String[] expected = new String[] { base, language, region, extension };
		String[] actual = new String[] { pp.base, pp.language, pp.region,
				pp.extension };
		assertEquals("pieces", Arrays.asList(expected), Arrays.asList(actual));
	}

	/**
	 * @param searchTerm
	 *            template we are looking for
	 * @param expectedHowMany
	 *            How many matches do we expect?
	 * @param expectedBestFitString
	 *            What should the best match turn out to be?
	 */
	private void assertMatches(String searchTerm, int expectedHowMany,
			String expectedBestFitString) {
		Path expectedBestFit = (expectedBestFitString == null) ? null : Paths
				.get(expectedBestFitString);

		SortedSet<PathPieces> matches = runTheVisitor(searchTerm);
		int actualHowMany = matches.size();
		Path actualBestFit = matches.isEmpty() ? null : matches.last().path;

		if (expectedHowMany != actualHowMany) {
			fail("How many results: expected " + expectedHowMany
					+ ", but was  " + actualHowMany + ": " + matches);
		}
		assertEquals("Best result", expectedBestFit, actualBestFit);
	}

	/**
	 * Try for exact match, then pare down if needed, just like Freemarker
	 * would.
	 */
	private void assertFM(String searchTerm, int expectedNumberOfTries,
			String expectedBestString) {
		Path expectedBestFit = expectedBestString == null ? null : Paths
				.get(expectedBestString);
		PathPieces stPp = new PathPieces(searchTerm);

		int actualNumberOfTries = 0;
		Path actualBestFit = null;

		if (StringUtils.isNotBlank(stPp.region)) {
			actualNumberOfTries++;
			SortedSet<PathPieces> matches = runTheVisitor(stPp.base
					+ stPp.language + stPp.region + stPp.extension);
			if (!matches.isEmpty()) {
				actualBestFit = matches.last().path;
			}
		}
		if (actualBestFit == null && StringUtils.isNotBlank(stPp.language)) {
			actualNumberOfTries++;
			SortedSet<PathPieces> matches = runTheVisitor(stPp.base
					+ stPp.language + stPp.extension);
			if (!matches.isEmpty()) {
				actualBestFit = matches.last().path;
			}
		}
		if (actualBestFit == null) {
			actualNumberOfTries++;
			SortedSet<PathPieces> matches = runTheVisitor(stPp.base
					+ stPp.extension);
			if (!matches.isEmpty()) {
				actualBestFit = matches.last().path;
			}
		}

		assertEquals("How many tries", expectedNumberOfTries,
				actualNumberOfTries);
		assertEquals("best fit", expectedBestFit, actualBestFit);
	}

	private SortedSet<PathPieces> runTheVisitor(String searchTerm) {
		try {
			visitor = new PathPiecesFileVisitorStub(new PathPieces(searchTerm));
			for (String p : this.paths) {
				visitor.visitFile(Paths.get(p), null);
			}
		} catch (IOException e) {
			fail("Failed: " + e);
		}

		return visitor.getMatches();
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	/**
	 * We want to test the PathPiecesFileVisitor, but we can't have it checking
	 * to see whether the files actually exist.
	 */
	private static class PathPiecesFileVisitorStub extends
			PathPiecesFileVisitor {
		public PathPiecesFileVisitorStub(PathPieces searchTerm) {
			super(searchTerm);
		}

		@Override
		public boolean fileQualifies(Path path) {
			return true;
		}

	}
}
