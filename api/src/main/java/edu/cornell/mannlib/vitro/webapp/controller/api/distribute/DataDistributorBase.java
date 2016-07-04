/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.distribute;

import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

/**
 * Build your DataDistributors on top of this.
 */
public abstract class DataDistributorBase implements DataDistributor {
	/** The name of the action request that we are responding to. */
	protected String actionName;
	protected DataDistributorContext ddContext;
	protected Map<String, String[]> parameters;

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#actionName", minOccurs = 1, maxOccurs = 1)
	public void setActionName(String action) {
		actionName = action;
	}

	@Override
	public void init(DataDistributorContext context) {
		this.ddContext = context;
		this.parameters = context.getRequestParameters();
	}

	@Override
	public String getActionName() {
		return actionName;
	}
}
