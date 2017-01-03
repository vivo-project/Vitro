/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.DatasetOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.OntModelSelectorOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.RdfServiceOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WebappDaoFactoryOption;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

/**
 * Data structure on a request have additional options, depending on the
 * identity and preferences of the user.
 */
public interface RequestModelAccess {
	/**
	 * Get an RDFService: CONTENT or CONFIGURATION, Language-aware or not.
	 */
	RDFService getRDFService(RdfServiceOption... options);

	/**
	 * Get a Dataset: CONTENT or CONFIGURATION, Language-aware or not.
	 */
	Dataset getDataset(DatasetOption... options);

	/**
	 * Get the FULL_UNION OntModel: Language-aware or not.
	 */
	OntModel getOntModel(LanguageOption... options);
	
	/**
	 * Get an OntModel: Language-aware or not.
	 */
	OntModel getOntModel(String name, LanguageOption... options);

	/**
	 * Get an OntModelSelector: Language-aware or not, ASSERTIONS or INFERENCES or both.
	 */
	OntModelSelector getOntModelSelector(OntModelSelectorOption... options);

	/**
	 * Get a WebappDaoFactory: Filtered or not, Language-aware or not,
	 * ASSERTIONS or INFERENCES or both.
	 */
	WebappDaoFactory getWebappDaoFactory(WebappDaoFactoryOption... options);
	
	/**
	 * When finished, release any resources.
	 */
	void close();
}
