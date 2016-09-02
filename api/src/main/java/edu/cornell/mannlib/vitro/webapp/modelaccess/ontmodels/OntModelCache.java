/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels;

import java.util.SortedSet;

import org.apache.jena.ontology.OntModel;

/**
 * A source for OntModels from the triple-store. The contract is this:
 * 
 * If you ask by a name that is not in use, an OntModel will be created.
 * 
 * If you ask by the same name twice, you get the same OntModel. 
 */
public interface OntModelCache {
	/**
	 * Get the model with this name (URI). If such a model does not exist, it will be created.
	 */
	OntModel getOntModel(String name);

	/**
	 * Get the names of all existing models.
	 */
	SortedSet<String> getModelNames();
}
