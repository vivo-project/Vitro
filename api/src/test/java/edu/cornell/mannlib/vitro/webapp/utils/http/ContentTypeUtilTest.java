/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.http;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.utils.http.ContentTypeUtil.AcceptableType;
import edu.cornell.mannlib.vitro.webapp.utils.http.ContentTypeUtil.MatchCriteria;

/**
 * TODO
 */
public class ContentTypeUtilTest extends AbstractTestClass {

	// ----------------------------------------------------------------------
	// MatchCriteria tests
	// ----------------------------------------------------------------------

	@Test
	public void mcEmptyName() {
		checkMatchCriteriaConstructor("MC empty name", "", "*", "*");
	}

	@Test
	public void mcNullName() {
		checkMatchCriteriaConstructor("MC null name", null, "*", "*");
	}

	@Test
	public void mcTypeOnly() {
		checkMatchCriteriaConstructor("MC type only", "image", "image", "*");
	}

	@Test
	public void mcTypeAndSubtype() {
		checkMatchCriteriaConstructor("MC type and subtype", "image/png",
				"image", "png");
	}

	@Test
	public void mcTypeAndEmptySubtype() {
		checkMatchCriteriaConstructor("MC type and empty subtype", "image/",
				"image", "*");
	}

	@Test
	public void mcWildcardType() {
		checkMatchCriteriaConstructor("MC wild card type", "*", "*", "*");
	}

	@Test
	public void mcWildcardSubtype() {
		checkMatchCriteriaConstructor("MC wild card subtype", "image/*",
				"image", "*");
	}

	@Test
	public void mcMatchWildcardType() {
		checkMatchQuality("MC match wild card type 1", "*", "text", 1);
		checkMatchQuality("MC match wild card type 2", "text", "*", 1);
		checkMatchQuality("MC match wild card type 3", "*", "text/plain", 1);
		checkMatchQuality("MC match wild card type 4", "text/*", "*", 1);
	}

	@Test
	public void mcTypesDontMatch() {
		checkMatchQuality("MC types don't match 1", "this", "that", 0);
		checkMatchQuality("MC types don't match 2", "this/match", "that/match",
				0);
	}

	@Test
	public void mcMatchWildcardSubtype() {
		checkMatchQuality("MC match wild card subtype 1", "text", "text/xml", 2);
		checkMatchQuality("MC match wild card subtype 2", "image/jpeg",
				"image/*", 2);
	}

	@Test
	public void mcSubtypesDontMatch() {
		checkMatchQuality("MC match subtypes don't match", "text/xml",
				"text/plain", 0);
	}

	@Test
	public void mcFullMatch() {
		checkMatchQuality("MC full match", "text/plain", "text/plain", 3);
	}

	// ----------------------------------------------------------------------
	// AcceptableType tests
	// ----------------------------------------------------------------------

	@Test
	public void atNullQ() throws AcceptHeaderParsingException {
		checkAcceptableTypeConstructor("AT null Q", null, 1.0F);
	}

	@Test
	public void atEmptyQ() throws AcceptHeaderParsingException {
		checkAcceptableTypeConstructor("AT empty Q", "", 1.0F);
	}

	@Test
	public void atBlankQ() throws AcceptHeaderParsingException {
		checkAcceptableTypeConstructor("AT blank Q", "  \t", 1.0F);
	}

	@Test(expected = AcceptHeaderParsingException.class)
	public void atInvalidQ() throws AcceptHeaderParsingException {
		checkAcceptableTypeConstructor("AT invalid Q", "99XX", 0.0F);
	}

	@Test(expected = AcceptHeaderParsingException.class)
	public void atQTooHigh() throws AcceptHeaderParsingException {
		checkAcceptableTypeConstructor("AT Q too high", "1.1", 0.0F);
	}

	@Test(expected = AcceptHeaderParsingException.class)
	public void atQTooLow() throws AcceptHeaderParsingException {
		checkAcceptableTypeConstructor("AT Q too low", "0", 0.0F);
	}

	@Test
	public void atGoodQ() throws AcceptHeaderParsingException {
		checkAcceptableTypeConstructor("AT good Q", "0.4", 0.4F);
	}

	@Test
	public void atWildcardMatchWorks() throws AcceptHeaderParsingException {
		checkMatchQuality("AT wild card match", "*", 0.5F, "text/plain", 2.5F);
	}

	@Test
	public void atPartialMatchIsBetter() throws AcceptHeaderParsingException {
		checkMatchQuality("AT partial match", "text", 0.5F, "text/plain", 3.5F);
	}

	@Test
	public void atFullMatchIsBest() throws AcceptHeaderParsingException {
		checkMatchQuality("AT full match", "text/plain", 0.5F, "text/plain",
				4.5F);
	}

	@Test
	public void atNoMatchTotallyBites() throws AcceptHeaderParsingException {
		checkMatchQuality("AT full match", "text/xml", 0.5F, "text/plain", 0.0F);
	}

	// ----------------------------------------------------------------------
	// Best content type tests
	// ----------------------------------------------------------------------
	@Test
	public void ctNullHeaderMatchesAnything() throws Exception {
		findBestMatch("CT null header matches anything", null,
				available("anything"), "anything");
	}

	@Test
	public void ctEmptyHeaderMatchesAnything() throws Exception {
		findBestMatch("CT empty header matches anything", "",
				available("anything"), "anything");
	}

	@Test
	public void ctBlankHeaderMatchesAnything() throws Exception {
		findBestMatch("CT blank header matches anything", "   \t  ",
				available("anything"), "anything");
	}

	@Test(expected = NotAcceptableException.class)
	public void ctNullCollectionMatchesNothing() throws Exception {
		findBestMatch("CT null collection matches nothing", "*/*", null,
				"nothing");
	}

	@Test(expected = NotAcceptableException.class)
	public void ctEmptyCollectionMatchesNothing() throws Exception {
		findBestMatch("CT empty collection matches nothing", "*/*",
				available(), "nothing");
	}

	@Test
	public void ctWildcardIsOK() throws Exception {
		findBestMatch("CT wild card is OK",
				"text/*;q=0.3, text/html;q=0.1, */*;q=0.5",
				available("image/png"), "image/png");
	}

	@Test
	public void ctPartialMatchIsBetter() throws Exception {
		findBestMatch("CT partial match is better",
				"text/*;q=0.3, text/html;q=0.1, */*;q=0.5",
				available("image/png", "text/xml"), "text/xml");
	}

	@Test
	public void ctFullMatchIsBest() throws Exception {
		findBestMatch("CT full match is best",
				"text/*;q=0.3, text/html;q=0.1, */*;q=0.5",
				available("image/png", "text/xml", "text/html"), "text/html");
	}

	@Test(expected = NotAcceptableException.class)
	public void ctNoMatchTotalBites() throws Exception {
		findBestMatch("CT no match totally bites",
				"text/*;q=0.3, text/html;q=0.1", available("no/match"),
				"nothing");
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void checkMatchCriteriaConstructor(String message, String name,
			String expectedType, String expectedSubtype) {
		MatchCriteria mc = new MatchCriteria(name);
		assertEquals(message + " - type", expectedType, mc.getType());
		assertEquals(message + " - subtype", expectedSubtype, mc.getSubtype());
	}

	private void checkMatchQuality(String message, String name1, String name2,
			int expected) {
		MatchCriteria mc1 = new MatchCriteria(name1);
		MatchCriteria mc2 = new MatchCriteria(name2);
		int actual = mc1.matchQuality(mc2);
		assertEquals(message, expected, actual);
	}

	private void checkAcceptableTypeConstructor(String message, String qString,
			float expected) throws AcceptHeaderParsingException {
		AcceptableType at = new AcceptableType("irrelevant", qString);
		assertEquals(message, expected, at.getQ(), 0.0001F);
	}

	private void checkMatchQuality(String message, String name1, float qValue,
			String name2, float expected) throws AcceptHeaderParsingException {
		AcceptableType at = new AcceptableType(name1, Float.toString(qValue));
		MatchCriteria mc = new MatchCriteria(name2);
		float actual = at.fitQuality(mc);
		assertEquals(message, expected, actual, 0.0001F);
	}

	private List<String> available(String... names) {
		return Arrays.asList(names);
	}

	private void findBestMatch(String message, String acceptHeader,
			List<String> availableTypeNames, String expected)
			throws AcceptHeaderParsingException, NotAcceptableException {
		String actual = ContentTypeUtil.bestContentType(acceptHeader,
				availableTypeNames);
		assertEquals(message, expected, actual);
	}
}
