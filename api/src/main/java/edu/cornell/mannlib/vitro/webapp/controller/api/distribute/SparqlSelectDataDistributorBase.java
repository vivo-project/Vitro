/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.distribute;

import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

/**
 * Some utility methods that come in handy.
 */
public abstract class SparqlSelectDataDistributorBase extends
		DataDistributorBase {

	/** The models on the current request. */
	protected RequestModelAccess models;

	protected Set<String> uriBindingNames = new HashSet<>();
	protected Set<String> literalBindingNames = new HashSet<>();
	protected VariableBinder binder;

	@SuppressWarnings("hiding")
	@Override
	public void init(DataDistributorContext ddContext) {
		super.init(ddContext);
		this.models = ddContext.getRequestModels();
		this.binder = new VariableBinder(ddContext.getRequestParameters());
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#uriBinding")
	public void addUriBindingName(String uriBindingName) {
		this.uriBindingNames.add(uriBindingName);
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#literalBinding")
	public void addLiteralBindingName(String literalBindingName) {
		this.literalBindingNames.add(literalBindingName);
	}

	@Override
	public String getContentType() throws DataDistributorException {
		return "application/sparql-results+json";
	}

}
