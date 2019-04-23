/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute;

import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

/**
 * TODO
 */
public abstract class AbstractDataDistributor implements DataDistributor {
    private static final String ACTION_NAME_PROPERTY = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#actionName";

    protected DataDistributorContext ddContext;
    protected Map<String, String[]> parameters;
    protected String actionName;

    @Override
    public void init(DataDistributorContext ddc)
            throws DataDistributorException {
        this.ddContext = ddc;
        this.parameters = ddc.getRequestParameters();
    }

    @Property(uri = ACTION_NAME_PROPERTY)
    public void setActionName(String aName) {
        this.actionName = aName;
    }

}
