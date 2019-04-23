/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public abstract class AbstractGraphBuilder implements GraphBuilder {
    private static final String BUILDER_NAME_PROPERTY = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#builderName";

    protected String builderName;

    public String getBuilderName() { return builderName; }

    @Property(uri = BUILDER_NAME_PROPERTY, minOccurs = 1, maxOccurs = 1)
    public void setBuilderName(String name) { this.builderName = name; }
}
