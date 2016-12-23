/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.tboxreasoner;

import java.util.List;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.rdf.model.Statement;

/**
 * The functionality of a TBox reasoner.
 * 
 * The reasoner will maintain its own TBox model. It will receive updates to
 * that model and perform reasoning on it. It will answer queries about the
 * contents of the model, when reasoning is complete.
 */
public interface TBoxReasoner {

	/**
	 * Add the additions and remove the removals.
	 */
	void updateReasonerModel(TBoxChanges changes);

	/**
	 * Chew on it and create the inferences. Report status.
	 */
	Status performReasoning();

	/**
	 * List all of the ObjectProperties from the reasoner model, after updating
	 * and reasoning.
	 */
	List<ObjectProperty> listObjectProperties();

	/**
	 * List all of the DatatypeProperties from the reasoner model, after
	 * updating and reasoning.
	 */
	List<DatatypeProperty> listDatatypeProperties();

	/**
	 * List all of the restrictions in the reasoner model, after updating and
	 * reasoning.
	 */
	List<Restriction> listRestrictions();
	
	/**
	 * List all of the statements that satisfy any of these patterns, after
	 * updating and reasoning.
	 */
	List<Statement> filterResults(List<ReasonerStatementPattern> patternList);
	
	public static class Status {
		public static final Status SUCCESS = new Status(true, false, "");
		public static final Status ERROR = new Status(true, true, "");
		
		public static final Status inconsistent(String explanation) {
			return new Status(false, false, explanation);
		}

		private final boolean consistent;
		private final boolean inErrorState;
		private final String explanation;

		private Status(boolean consistent, boolean inErrorState,
				String explanation) {
			this.consistent = consistent;
			this.inErrorState = inErrorState;
			this.explanation = explanation;
		}

		public boolean isConsistent() {
			return consistent;
		}

		public boolean isInErrorState() {
			return inErrorState;
		}

		public String getExplanation() {
			return explanation;
		}
	}

}
