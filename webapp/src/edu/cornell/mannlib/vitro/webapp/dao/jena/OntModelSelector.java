/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import com.hp.hpl.jena.ontology.OntModel;

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
	 * @param ontologyURI
	 * @return OntModel containing TBox axioms for the specified ontology
	 */
	public OntModel getTBoxModel(String ontologyURI);
	
	public OntModel getDisplayModel();
	
}
