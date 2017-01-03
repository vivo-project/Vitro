/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.tboxreasoner;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;

import java.util.LinkedList;
import java.util.Set;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;

import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ReasonerConfiguration;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ReasonerStatementPattern;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.TBoxChanges;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.TBoxReasoner;

/**
 * The list of patterns for filtering the models will include:
 * 
 * All patterns specified by the ReasonerConfiguration,
 * 
 * One pattern for each deleted property, to match the use of that property as a
 * predicate.
 */
public class PatternListBuilder {
	private static final String OWL_NS = "http://www.w3.org/2002/07/owl#";

	private final ReasonerConfiguration reasonerConfiguration;
	private final TBoxReasoner reasoner;
	private final TBoxChanges changes;

	public PatternListBuilder(ReasonerConfiguration reasonerConfiguration,
			TBoxReasoner reasoner, TBoxChanges changes) {
		this.reasonerConfiguration = reasonerConfiguration;
		this.reasoner = reasoner;
		this.changes = changes;
	}

	public LinkedList<ReasonerStatementPattern> build() {
		LinkedList<ReasonerStatementPattern> patterns = new LinkedList<>();

		Set<ReasonerStatementPattern> allowSet = reasonerConfiguration
				.getInferenceReceivingPatternAllowSet();
		if (allowSet != null) {
			patterns.addAll(allowSet);
		} else {
			patterns.add(ReasonerStatementPattern.ANY_OBJECT_PROPERTY);
		}

		if (reasonerConfiguration.getQueryForAllObjectProperties()) {
			for (ObjectProperty objProp : reasoner.listObjectProperties()) {
				if (!(OWL_NS.equals(objProp.getNameSpace()))) {
					patterns.add(ReasonerStatementPattern
							.objectPattern(objProp));
				}
			}

			for (String uri : changes.getDeletedObjectPropertyUris()) {
				patterns.add(ReasonerStatementPattern
						.objectPattern(createProperty(uri)));
			}
		}

		if (reasonerConfiguration.getQueryForAllDatatypeProperties()) {
			for (DatatypeProperty dataProp : reasoner.listDatatypeProperties()) {
				if (!(OWL_NS.equals(dataProp.getNameSpace()))) {
					// TODO: THIS WILL WORK, BUT NEED TO GENERALIZE THE
					// PATTERN CLASSES
					patterns.add(ReasonerStatementPattern
							.objectPattern(dataProp));
				}
			}
			for (String uri : changes.getDeletedDataPropertyUris()) {
				patterns.add(ReasonerStatementPattern
						.objectPattern(createProperty(uri)));
			}
		}
		return patterns;
	}
}
