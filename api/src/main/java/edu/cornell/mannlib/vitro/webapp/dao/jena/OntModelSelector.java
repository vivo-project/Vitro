/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import org.apache.jena.ontology.OntModel;

/**
 * Interface for retrieving RDF (Ont)models containing certain types of data.
 * So named because there was already a ModelSelector in the n3editing package.
 * @author bjl23
 */
public interface OntModelSelector {

	/**
	 * @return OntModel containing all RDF statements available to the application
	 */
	public OntModel getFullModel();

	/**
	 * @return OntModel containing Portals, Tabs, etc. but not Users
	 */
	public OntModel getApplicationMetadataModel();

	/**
	 * @return OntModel containing Users
	 */
	public OntModel getUserAccountsModel();

	/**
	 * @return OntModel containing all ABox assertions
	 */
	public OntModel getABoxModel();

	/**
	 * @return OntModel containing all TBox axioms
	 */
	public OntModel getTBoxModel();

	/**
	 * @return OntModel containing all RDF statements in the Display model.
	 */
	public OntModel getDisplayModel();
}
