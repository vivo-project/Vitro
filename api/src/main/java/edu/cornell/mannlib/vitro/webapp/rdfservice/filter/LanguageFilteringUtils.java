/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.filter;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletRequest;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceGraph;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;

/**
 * Some methods that will come in handy when dealing with Language Filtering
 */
public class LanguageFilteringUtils {

	/**
	 * Take an Enumeration of Locale objects, such as we might get from a
	 * request, and convert to a List of langauage strings, such as are needed
	 * by the LanguageFilteringRDFService.
	 * 
	 * While converting, change all underscores (as in Locale names) to hyphens
	 * (as in RDF language specifiers).
	 */
	public static List<String> localesToLanguages(Enumeration<?> locales) {
		List<String> langs = new ArrayList<>();
		while (locales.hasMoreElements()) {
			Locale locale = (Locale) locales.nextElement();
			langs.add(locale.toString().replace("_", "-"));
		}
		if (langs.isEmpty()) {
			langs.add("en");
		}
		return langs;

	}

	/**
	 * Add a Language Filtering layer to an OntModel by treating it as an RDFService.
	 */
	public static OntModel wrapOntModelInALanguageFilter(OntModel rawModel,
			ServletRequest req) {
		/** This is some nasty layering. Could we do this more easily? */
		List<String> languages = localesToLanguages(req.getLocales());
		return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,
				RDFServiceGraph.createRDFServiceModel(
						new RDFServiceGraph(
								new LanguageFilteringRDFService(
										new RDFServiceModel(rawModel), languages))));
	}

	private LanguageFilteringUtils() {
		// Nothing to instantiate
	}

}
