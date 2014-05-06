/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeaderValueParser;

/**
 * A utility for selecting content types, in the context of the Accept header.
 * 
 * -------------------
 * 
 * This does not support matching against content types with extensions, like
 * "level=1", as illustrated in RFC-2616:
 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1
 * 
 * However, as long as we don't offer such extensions on our available types,
 * the use of extensions in the Accept header is moot.
 */
public class ContentTypeUtil {

	/**
	 * Given an Accept header value and a list of available type names, decide
	 * which type is the best fit.
	 * 
	 * If there is no fit, throw a NotAcceptableException
	 * 
	 * The only thing to do is to match all available against all acceptable,
	 * and pick the best match. Try to do as little work as possible inside the
	 * nested loop.
	 */
	public static String bestContentType(String acceptHeader,
			Collection<String> availableTypeNames)
			throws AcceptHeaderParsingException, NotAcceptableException {
		if (availableTypeNames == null) {
			throw new NotAcceptableException("availableTypeNames may not be null.");
		}
		
		Set<AcceptableType> acceptableTypes = parseAcceptHeader(acceptHeader);
		List<MatchCriteria> availableTypes = convertToMatchCriteria(availableTypeNames);

		float bestFitQuality = 0.0F;
		MatchCriteria bestMatch = null;

		for (AcceptableType acceptableType : acceptableTypes) {
			for (MatchCriteria availableType : availableTypes) {
				float fitQuality = acceptableType.fitQuality(availableType);
				if (fitQuality > bestFitQuality) {
					bestFitQuality = fitQuality;
					bestMatch = availableType;
				}
			}
		}

		if (bestMatch == null) {
			throw new NotAcceptableException(
					"No available type matches the Accept header: '"
							+ acceptHeader + "'; available types are "
							+ availableTypeNames);
		} else {
			return bestMatch.getName();
		}
	}

	/**
	 * The order of items in the Accept header is not important. We rely on the
	 * specificity of the match and the "q" factor, in that order.
	 * 
	 * Since q ranges between 1.0 and 0.001, we add a specificity offset of 2, 3
	 * or 4. That way, matches with equal specificity are decided by q factor.
	 */
	public static Set<AcceptableType> parseAcceptHeader(String acceptHeader)
			throws AcceptHeaderParsingException {
		if (acceptHeader == null || acceptHeader.trim().isEmpty()) {
			return Collections.singleton(new AcceptableType("*/*", "1.0"));
		}

		HeaderElement[] elements = BasicHeaderValueParser.parseElements(
				acceptHeader, null);

		Set<AcceptableType> acceptableTypes = new HashSet<>();
		for (HeaderElement he : elements) {
			String name = he.getName();

			NameValuePair qPair = he.getParameterByName("q");
			String qString = (qPair == null) ? "1.0" : qPair.getValue();

			acceptableTypes.add(new AcceptableType(name, qString));
		}

		return acceptableTypes;
	}

	private static List<MatchCriteria> convertToMatchCriteria(
			Collection<String> availableTypes) {
		List<MatchCriteria> availableMatches = new ArrayList<>();
		for (String availableType : availableTypes) {
			availableMatches.add(new MatchCriteria(availableType));
		}
		return availableMatches;
	}

	/**
	 * Parsing the Accept header returns a set of these.
	 * 
	 * Package access to permit unit testing.
	 */
	static class AcceptableType {
		private final MatchCriteria matchCriteria;
		private final float q;

		public AcceptableType(String name, String qString)
				throws AcceptHeaderParsingException {
			this.matchCriteria = new MatchCriteria(name);
			this.q = parseQValue(qString);
		}

		private float parseQValue(String qString)
				throws AcceptHeaderParsingException {
			float qValue = 0.0F;
			
			if (qString == null || qString.trim().isEmpty()) {
				qString = "1";
			}

			try {
				qValue = Float.parseFloat(qString);
			} catch (Exception e) {
				throw new AcceptHeaderParsingException("invalid q value: '"
						+ qString + "'");
			}

			if (qValue > 1.0F || qValue <= 0.0F) {
				throw new AcceptHeaderParsingException("q value out of range: "
						+ qString);
			}

			return qValue;
		}

		public float fitQuality(MatchCriteria availableType) {
			int matchQuality = matchCriteria.matchQuality(availableType);
			if (matchQuality == 0) {
				return 0;
			} else {
				return matchQuality + 1.0F + q;
			}
		}

		public String getName() {
			return matchCriteria.getName();
		}

		public float getQ() {
			return q;
		}
	}

	/**
	 * Parse the available type names into a list of these, so we only do the
	 * substring operations once.
	 * 
	 * Package access to permit unit testing.
	 */
	static class MatchCriteria {
		private final String name;
		private final String type;
		private final String subtype;

		MatchCriteria(String name) {
			if (name == null) {
				name = "";
			}

			this.name = name;
			int slashHere = name.indexOf('/');

			if (name.isEmpty()) {
				this.type = "*";
				this.subtype = "*";
			} else if (slashHere == -1) {
				this.type = name;
				this.subtype = "*";
			} else if (slashHere == name.length() - 1) {
				this.type = name.substring(0, slashHere);
				this.subtype = "*";
			} else {
				this.type = name.substring(0, slashHere);
				this.subtype = name.substring(slashHere + 1);
			}
		}

		/**
		 * If one of the types is a wild-card, it's a weak match.
		 * 
		 * Otherwise, if the types match and one of the subtypes is a wild-card,
		 * it's a medium match.
		 * 
		 * Otherwise, if the types match and the subtypes match, it's a strong
		 * match.
		 * 
		 * Otherwise, it is no match.
		 */
		public int matchQuality(MatchCriteria that) {
			boolean typeMatch = this.type.equals(that.type);
			boolean typeWild = this.type.equals("*") || that.type.equals("*");
			boolean subtypeMatch = this.subtype.equals(that.subtype);
			boolean subtypeWild = this.subtype.equals("*")
					|| that.subtype.equals("*");

			if (typeWild) {
				return 1;
			} else if (typeMatch && subtypeWild) {
				return 2;
			} else if (typeMatch && subtypeMatch) {
				return 3;
			} else {
				return 0;
			}
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

		public String getSubtype() {
			return subtype;
		}
	}
}
