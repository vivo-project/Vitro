/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.modelaccess;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ReasoningOption.ASSERTIONS_AND_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONTENT;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_UNION;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ReasoningOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
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
	//
	// Warning: ontModelMap and rdfServiceMap are not connected, so it's up to
	// the user to insure that they are consistent with each other.
	// ----------------------------------------------------------------------

	private final Map<ReasoningOption, WebappDaoFactory> wadfMap = new HashMap<>();
	private final Map<WhichService, RDFService> rdfServiceMap = new EnumMap<>(WhichService.class);
	private final Map<String, OntModel> ontModelMap = new HashMap<>();

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
	
	public void setOntModel(String name, OntModel model) {
		ontModelMap.put(name, model);
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
	
	@Override
	public OntModel getOntModel() {
		return getOntModel(FULL_UNION);
	}

	@Override
	public OntModel getOntModel(String name) {
		return ontModelMap.get(name);
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
