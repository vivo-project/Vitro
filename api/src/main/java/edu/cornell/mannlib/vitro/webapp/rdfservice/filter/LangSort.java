package edu.cornell.mannlib.vitro.webapp.rdfservice.filter;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A class for sorting language strings by acceptability according to a 
 * supplied list of language preferences.  
 * Refactored from LanguageFilteringRDFService for reuse by classes performing
 * similar functions.
 */
public class LangSort {
     
    private static final Log log = LogFactory.getLog(LangSort.class);
    
    protected List<String> langs;
    private int inexactMatchPenalty;
    private int noLanguage;
    private int noMatch;

    /**
     * Construct a language string sorter with a supplied list of preferred 
     * language strings
     * @param preferredLanguageStrings list of preferred languages of form
     *                                 'en-US', 'es', 'fr-CA'.  May not be null.
     */
    public LangSort(List<String> preferredLanguageStrings) {
        this.langs = preferredLanguageStrings;
        this.inexactMatchPenalty = langs.size();
        // no language is worse than any inexact match (if the preferred list does not include "").
        this.noLanguage = 2 * inexactMatchPenalty;
        // no match is worse than no language.
        this.noMatch = noLanguage + 1;
    }
    
    protected int compareLangs(String t1lang, String t2lang) {
        int index1 = languageIndex(t1lang);
        int index2 = languageIndex(t2lang);
        if(index1 == index2) {
            return t1lang.compareTo(t2lang);
        } else {
            return languageIndex(t1lang) - languageIndex(t2lang);
        }
    }

    /**
     * Return index of exact match, or index of partial match, or
     * language-free, or no match.
     */
    private int languageIndex(String lang) {
        if (lang == null) {
            lang = "";
        }

        int index = langs.indexOf(lang);
        if (index >= 0) {
            log.debug("languageIndex for '" + lang + "' is " + index);
            return index;
        }

        if (lang.length() > 2) {
            index = langs.indexOf(lang.substring(0, 2));
            if (index >= 0) {
                log.debug("languageIndex for '" + lang + "' is " + index + inexactMatchPenalty);
                return index + inexactMatchPenalty;
            }
        }

        if (lang.isEmpty()) {
            log.debug("languageIndex for '" + lang + "' is " + noLanguage);
            return noLanguage;
        }

        return noMatch;
    }
}
