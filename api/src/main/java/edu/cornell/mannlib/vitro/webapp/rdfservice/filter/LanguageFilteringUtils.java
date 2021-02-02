/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.filter;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletRequest;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * Some methods that will come in handy when dealing with Language Filtering
 */
public class LanguageFilteringUtils {

	private static final String UNDERSCORE = "_";
	private static final String HYPHEN = "-";
	private static final String DEFAULT_LANG_STRING = "en";

    /**
     * Take a Locale object, such as we might get from a
     * request, and convert to a language string used in RDF.
     *
     * While converting, change all underscores (as in Locale names) to hyphens
     * (as in RDF language specifiers).
     */
    public static String localeToLanguage(Locale locale) {
        return locale.toString().replace(UNDERSCORE, HYPHEN);
    }
	
    /**
     * Take a language string and convert to a Locale.
     *
     * While converting, change all hyphens (as in RDF language specifiers) to
     * underscores (as in Locale names). Ensure language string is lowercase
     * and country abbreviation is uppercase.
     */
    public static Locale languageToLocale(String langStr) {
        String[] langParts = langStr.split(HYPHEN);
        if (langParts.length > 2) {
            langStr = String.join(UNDERSCORE, langParts[0].toLowerCase(),
                    langParts[1].toUpperCase(), langParts[2]);
        } else if (langParts.length > 1) {
            langStr = String.join(UNDERSCORE, langParts[0].toLowerCase(),
                    langParts[1].toUpperCase());
        } else {
            langStr = langParts[0].toLowerCase();
        }
        return LocaleUtils.toLocale(langStr);
    }
	
	/**
	 * Take an Enumeration of Locale objects, such as we might get from a
	 * request, and convert to a List of language strings, such as are needed
	 * by the LanguageFilteringRDFService.
	 *
	 * While converting, change all underscores (as in Locale names) to hyphens
	 * (as in RDF language specifiers).
	 */
	public static List<String> localesToLanguages(Enumeration<?> locales) {
		List<String> langs = new ArrayList<>();
		while (locales.hasMoreElements()) {
			Locale locale = (Locale) locales.nextElement();
			langs.add(locale.toString().replace(UNDERSCORE, HYPHEN));
		}
		if (langs.isEmpty()) {
			langs.add(DEFAULT_LANG_STRING);
		}

		return langs;
	}

	/**
	 * Take a List of language strings and convert to a List of Locale.
	 *
	 * While converting, change all hyphens (as in RDF language specifiers) to
	 * under scores (as in Locale names). Ensure language string is lowercase
	 * and country abbreviation is uppercase.
	 */
	public static List<Locale> languagesToLocales(List<String> langs) {
		Set<Locale> locales = new HashSet<>();
		langs.forEach(langStr -> {			
			locales.add(languageToLocale(langStr));
		});
		if (locales.isEmpty()) {
			locales.add(LocaleUtils.toLocale(DEFAULT_LANG_STRING));
		}

		return new ArrayList<>(locales);
	}

	/**
	 * Add a Language Filtering layer to an OntModel
	 */
	public static OntModel wrapOntModelInALanguageFilter(OntModel rawModel,
			ServletRequest req) {
		List<String> languages = new AcceptableLanguages(localesToLanguages(req.getLocales()));
		return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,
		        ModelFactory.createModelForGraph(new LanguageFilteringGraph(
		                rawModel.getGraph(), languages)));
	}

	private LanguageFilteringUtils() {
		// Nothing to instantiate
	}

}
