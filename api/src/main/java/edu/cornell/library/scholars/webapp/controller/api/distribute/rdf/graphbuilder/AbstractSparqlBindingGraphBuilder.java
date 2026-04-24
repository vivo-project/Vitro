/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.NotAuthorizedException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;
import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.util.VariableBinder;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.IndividualAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.SimpleAuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.QueryHolder;
import org.apache.commons.lang3.StringUtils;

/**
 * Keep track of the binding details for DataDistributors that bind request
 * parameters into SPARQL queries.
 */
public abstract class AbstractSparqlBindingGraphBuilder extends AbstractGraphBuilder {
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

    protected QueryHolder bindParametersToQuery(DataDistributorContext ddContext, QueryHolder rawQuery)
            throws DataDistributorException {
        Map<String, String[]> parameters = ddContext.getRequestParameters();
        checkAuthorization(ddContext, parameters);
        return new VariableBinder(parameters).bindValuesToQuery(uriBindingNames, literalBindingNames, rawQuery);
    }

    private void checkAuthorization(DataDistributorContext ddContext, Map<String, String[]> parameters)
            throws NotAuthorizedException {
        for (String name : uriBindingNames) {
            if (parameters.containsKey(name)) {
                String[] uris = parameters.get(name);
                for (String uri : uris) {
                    if (StringUtils.isNotBlank(uri)) {
                        AccessObject ao = new IndividualAccessObject(uri);
                        ao.setModel(ModelAccess.getInstance().getOntModel(ModelNames.FULL_UNION));
                        AuthorizationRequest request = new SimpleAuthorizationRequest(ao, AccessOperation.DISPLAY);
                        if (!ddContext.isAuthorized(request)) {
                            throw new NotAuthorizedException();
                        }
                    }
                }
            }
        }
    }
}
