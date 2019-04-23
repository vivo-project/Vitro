/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf;

import java.util.HashSet;
import java.util.Set;

import edu.cornell.library.scholars.webapp.controller.api.distribute.AbstractDataDistributor;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;
import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.util.VariableBinder;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

/**
 * Keep track of the binding details for DataDistributors that bind request
 * parameters into SPARQL queries.
 */
public abstract class AbstractSparqlBindingDistributor
        extends AbstractDataDistributor {
    protected VariableBinder binder;

    protected Set<String> uriBindingNames = new HashSet<>();
    protected Set<String> literalBindingNames = new HashSet<>();

    @Override
    public void init(DataDistributorContext ddContext)
            throws DataDistributorException {
        super.init(ddContext);
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
}
