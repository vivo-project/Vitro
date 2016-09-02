/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ReasoningOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

/**
 * Data structures on the context have fewer options than those on a request.
 * 
 * There is no Preferred Language or Policy restrictions if there is no
 * "current user".
 */
public interface ContextModelAccess {
	/** Get the RDFService for the CONTENT. */
	public RDFService getRDFService();

	/** Get the RDFService for either CONTENT or CONFIGURATION models. */
	public RDFService getRDFService(WhichService which);

	/** Get the Dataset for the CONTENT models. */
	public Dataset getDataset();

	/** Get the Dataset for either CONTENT or CONFIGURATION models. */
	public Dataset getDataset(WhichService which);
	
	/** Get the ModelMaker for the CONTENT models. */
	public ModelMaker getModelMaker();

	/** Get the ModelMaker for either CONTENT or CONFIGURATION models. */
	public ModelMaker getModelMaker(WhichService which);
	
	/** Get the FULL_UNION OntModel. */
	public OntModel getOntModel();

	/** Get an OntModel by name. */
	public OntModel getOntModel(String name);
	
	/** Get the ASSERTIONS_AND_INFERENCES OntModelSelector. */
	public OntModelSelector getOntModelSelector();
	
	/** Get an OntModelSelector based on ASSERTIONS, INFERENCES, or both. */
	public OntModelSelector getOntModelSelector(ReasoningOption option);

	/** Get the ASSERTIONS_AND_INFERENCES WebappDaoFactory. */
	public WebappDaoFactory getWebappDaoFactory();

	/** Get a WebappDaoFactory, based on ASSERTIONS, INFERENCES, or both. */
	public WebappDaoFactory getWebappDaoFactory(ReasoningOption option);
}
