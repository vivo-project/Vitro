/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder;

import java.util.HashSet;
import java.util.Set;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.MissingParametersException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;
import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.util.VariableBinder;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.QueryHolder;

/**
 * Keep track of the binding details for DataDistributors that bind request
 * parameters into SPARQL queries.
 */
public abstract class AbstractSparqlBindingGraphBuilder
        implements GraphBuilder {
    protected Set<String> uriBindingNames = new HashSet<>();
    protected Set<String> literalBindingNames = new HashSet<>();

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#uriBinding")
    public void addUriBindingName(String uriBindingName) {
        this.uriBindingNames.add(uriBindingName);
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#literalBinding")
    public void addLiteralBindingName(String literalBindingName) {
        this.literalBindingNames.add(literalBindingName);
    }

    protected QueryHolder bindParametersToQuery(
            DataDistributorContext ddContext, QueryHolder rawQuery)
            throws MissingParametersException {
        return new VariableBinder(ddContext.getRequestParameters())
                .bindValuesToQuery(uriBindingNames, literalBindingNames,
                        rawQuery);
    }
}
