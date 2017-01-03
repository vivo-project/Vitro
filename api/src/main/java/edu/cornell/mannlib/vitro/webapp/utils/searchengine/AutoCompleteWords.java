/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.searchengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A helper class for use with an Auto-complete query.
 * 
 * Any word that is followed by a delimiter is considered to be complete, and
 * should be matched exactly in the query. If there is a word on the end that is
 * not followed by a delimiter, it is incomplete, and should act like a
 * "starts-with" query.
 */
public class AutoCompleteWords {
	private static final Log log = LogFactory.getLog(AutoCompleteWords.class);

	private final String searchTerm;
	private final String delimiterPattern;
	private final List<String> completeWords;
	private final String partialWord;

	/**
	 * Package-access. Use SearchQueryUtils.parseForAutoComplete() to create an
	 * instance.
	 */
	AutoCompleteWords(String searchTerm, String delimiterPattern) {
		this.searchTerm = (searchTerm == null) ? "" : searchTerm;
		this.delimiterPattern = delimiterPattern;

		List<String> termWords = figureTermWords();
		if (termWords.isEmpty()
				|| this.searchTerm.matches(".*" + delimiterPattern)) {
			this.completeWords = termWords;
			this.partialWord = null;
		} else {
			this.completeWords = termWords.subList(0, termWords.size() - 1);
			this.partialWord = termWords.get(termWords.size() - 1);
		}

	}

	private List<String> figureTermWords() {
		List<String> list = new ArrayList<String>();
		String[] array = this.searchTerm.split(this.delimiterPattern);
		for (String word : array) {
			String trimmed = word.trim();
			if (!trimmed.isEmpty()) {
				list.add(trimmed);
			}
		}
		return Collections.unmodifiableList(list);
	}

	public String assembleQuery(String fieldNameForCompleteWords,
			String fieldNameForPartialWord) {
		List<String> terms = new ArrayList<String>();
		for (String word : this.completeWords) {
			terms.add(buildTerm(fieldNameForCompleteWords, word));
		}
		if (partialWord != null) {
			terms.add(buildTerm(fieldNameForPartialWord, partialWord));
		}

		String q = StringUtils.join(terms, " AND ");
		log.debug("Query string is '" + q + "'");
		return q;
	}

	private String buildTerm(String fieldName, String word) {
		return fieldName + ":\"" + word + "\"";
	}

	@Override
	public String toString() {
		return "AutoCompleteWords[searchTerm='" + searchTerm
				+ "', delimiterPattern='" + delimiterPattern
				+ "', completeWords=" + completeWords + ", partialWord="
				+ partialWord + "]";
	}

}
