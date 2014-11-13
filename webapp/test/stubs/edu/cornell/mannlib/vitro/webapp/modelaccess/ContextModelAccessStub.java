/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.modelaccess;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ReasoningOption.ASSERTIONS_AND_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONTENT;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ReasoningOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

/**
 * A mock instance of ContextModelAccess for use in unit tests.
 * 
 * I have only implemented the methods that I needed for my tests. Feel free to
 * implement the rest, as needed.
 */
public class ContextModelAccessStub implements ContextModelAccess {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private final Map<ReasoningOption, WebappDaoFactory> wadfMap = new HashMap<>();
	private final Map<WhichService, RDFService> rdfServiceMap = new EnumMap<>(WhichService.class);

	public void setWebappDaoFactory(WebappDaoFactory wadf) {
		setWebappDaoFactory(wadf, ASSERTIONS_AND_INFERENCES);
	}

	public void setWebappDaoFactory(WebappDaoFactory wadf,
			ReasoningOption option) {
		wadfMap.put(option, wadf);
	}
	
	public void setRDFService(WhichService which, RDFService rdfService) {
		rdfServiceMap.put(which, rdfService);
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public WebappDaoFactory getWebappDaoFactory() {
		return wadfMap.get(ASSERTIONS_AND_INFERENCES);
	}

	@Override
	public RDFService getRDFService() {
		return getRDFService(CONTENT);
	}
	
	@Override
	public RDFService getRDFService(WhichService which) {
		return rdfServiceMap.get(which);
	}
	
	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public Dataset getDataset() {
		throw new RuntimeException(
				"ContextModelAccessStub.getDataset() not implemented.");
	}

	@Override
	public Dataset getDataset(WhichService which) {
		throw new RuntimeException(
				"ContextModelAccessStub.getDataset() not implemented.");
	}

	@Override
	public ModelMaker getModelMaker() {
		throw new RuntimeException(
				"ContextModelAccessStub.getModelMaker() not implemented.");
	}

	@Override
	public ModelMaker getModelMaker(WhichService which) {
		throw new RuntimeException(
				"ContextModelAccessStub.getModelMaker() not implemented.");
	}

	@Override
	public OntModel getOntModel() {
		throw new RuntimeException(
				"ContextModelAccessStub.getOntModel() not implemented.");
	}

	@Override
	public OntModel getOntModel(String name) {
		throw new RuntimeException(
				"ContextModelAccessStub.getOntModel() not implemented.");
	}

	@Override
	public OntModelSelector getOntModelSelector() {
		throw new RuntimeException(
				"ContextModelAccessStub.getOntModelSelector() not implemented.");
	}

	@Override
	public OntModelSelector getOntModelSelector(ReasoningOption option) {
		throw new RuntimeException(
				"ContextModelAccessStub.getOntModelSelector() not implemented.");
	}

	@Override
	public WebappDaoFactory getWebappDaoFactory(ReasoningOption option) {
		throw new RuntimeException(
				"ContextModelAccessStub.getWebappDaoFactory() not implemented.");
	}

}
