/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.tboxreasoner;

import java.util.List;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.tboxreasoner.TBoxReasonerDriver.Status;

/**
 * TODO
 */
public interface TBoxReasonerWrapper {

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
	 * List all of the statements that satisfy any of these patterns, after
	 * updating and reasoning.
	 */
	List<Statement> filterResults(List<ReasonerStatementPattern> patternList);

	/**
	 * List all of the restrictions in the reasoner model, after updating and
	 * reasoning.
	 */
	List<Restriction> listRestrictions();

}
