/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.modelaccess;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.DatasetOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.OntModelSelectorOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.RdfServiceOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WebappDaoFactoryOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.impl.keys.OntModelKey;
import edu.cornell.mannlib.vitro.webapp.modelaccess.impl.keys.RDFServiceKey;
import edu.cornell.mannlib.vitro.webapp.modelaccess.impl.keys.WebappDaoFactoryKey;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

/**
 * A mock instance of RequestModelAccess for use in unit tests.
 * 
 * I have only implemented the methods that I needed for my tests. Feel free to
 * implement the rest, as needed.
 */
public class RequestModelAccessStub implements RequestModelAccess {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private final Map<RDFServiceKey, RDFService> rdfServiceMap = new HashMap<>();

	public void setRDFService(RDFService rdfService,
			RdfServiceOption... options) {
		rdfServiceMap.put(new RDFServiceKey(options), rdfService);
	}

	private final Map<OntModelKey, OntModel> ontModelMap = new HashMap<>();

	public void setOntModel(OntModel model, String name) {
		ontModelMap.put(new OntModelKey(name), model);
	}

	private final Map<WebappDaoFactoryKey, WebappDaoFactory> wadfMap = new HashMap<>();

	public void setWebappDaoFactory(WebappDaoFactory wadf,
			WebappDaoFactoryOption... options) {
		wadfMap.put(new WebappDaoFactoryKey(options), wadf);
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public RDFService getRDFService(RdfServiceOption... options) {
		return rdfServiceMap.get(new RDFServiceKey(options));
	}

	@Override
	public OntModel getOntModel(LanguageOption... options) {
		return getOntModel(ModelNames.FULL_UNION, options);
	}

	@Override
	public OntModel getOntModel(String name, LanguageOption... options) {
		return ontModelMap.get(new OntModelKey(name, options));
	}

	@Override
	public WebappDaoFactory getWebappDaoFactory(
			WebappDaoFactoryOption... options) {
		return wadfMap.get(new WebappDaoFactoryKey(options));
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public Dataset getDataset(DatasetOption... options) {
		throw new RuntimeException(
				"RequestModelAccessStub.getDataset() not implemented.");
	}

	@Override
	public OntModelSelector getOntModelSelector(
			OntModelSelectorOption... options) {
		throw new RuntimeException(
				"RequestModelAccessStub.getOntModelSelector() not implemented.");
	}

	@Override
	public void close() {
		throw new RuntimeException(
				"RequestModelAccessStub.close() not implemented.");
	}

}
