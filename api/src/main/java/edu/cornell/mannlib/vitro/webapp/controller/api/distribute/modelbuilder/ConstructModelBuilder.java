/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.distribute.modelbuilder;

import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createConstructQueryContext;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.mannlib.vitro.webapp.controller.api.distribute.DataDistributorContext;
import edu.cornell.mannlib.vitro.webapp.controller.api.distribute.VariableBinder;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.ConstructQueryContext;

/**
 * Run a construct query to build the model. Bind parameters from the request,
 * as needed.
 */
public class ConstructModelBuilder implements ModelBuilder {
	private static final Log log = LogFactory
			.getLog(ConstructModelBuilder.class);

	protected RequestModelAccess models;
	protected String rawConstructQuery;
	protected Set<String> uriBindingNames = new HashSet<>();
	protected Set<String> literalBindingNames = new HashSet<>();
	protected VariableBinder binder;

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#uriBinding")
	public void addUriBindingName(String uriBindingName) {
		this.uriBindingNames.add(uriBindingName);
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#literalBinding")
	public void addLiteralBindingName(String literalBindingName) {
		this.literalBindingNames.add(literalBindingName);
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#constructQuery", minOccurs = 1, maxOccurs = 1)
	public void setRawConstructQuery(String query) {
		rawConstructQuery = query;
	}

	@Override
	public void init(DataDistributorContext ddContext)
			throws DataDistributorException {
		this.models = ddContext.getRequestModels();
		this.binder = new VariableBinder(ddContext.getRequestParameters());
	}

	@Override
	public Model buildModel() throws DataDistributorException {
		ConstructQueryContext queryContext = createConstructQueryContext(
				models.getRDFService(), rawConstructQuery);
		queryContext = binder.bindUriParameters(uriBindingNames, queryContext);
		queryContext = binder.bindLiteralParameters(literalBindingNames,
				queryContext);
		log.debug("Query context is: " + queryContext);
		return queryContext.execute().toModel();
	}

	@Override
	public void close() {
		// Nothing to do.
	}

}
