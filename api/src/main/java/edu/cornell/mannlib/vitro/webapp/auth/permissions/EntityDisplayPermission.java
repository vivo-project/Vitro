/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.permissions;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayDataProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayDataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayObjectProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class EntityDisplayPermission extends EntityPermission {
    private static final Log log = LogFactory.getLog(EntityDisplayPermission.class);

    public EntityDisplayPermission(String uri) {
        super(uri);
    }

    @Override
    public boolean isAuthorized(List<String> personUris, RequestedAction whatToAuth) {
        boolean result = false;

        if (whatToAuth instanceof DisplayDataProperty) {
            String predicateUri = ((DisplayDataProperty)whatToAuth).getDataProperty().getURI();
            result = isAuthorizedFor(new Property(predicateUri));
        } else if (whatToAuth instanceof DisplayObjectProperty) {
            result = isAuthorizedFor(((DisplayObjectProperty)whatToAuth).getObjectProperty());
        } else if (whatToAuth instanceof DisplayDataPropertyStatement) {
            DataPropertyStatement stmt = ((DisplayDataPropertyStatement)whatToAuth).getDataPropertyStatement();

            // Subject [stmt.getIndividualURI()] is a resource
            // Previous auth code always evaluated as true when checking permissions for resources
            // Do we need to implement a check on permissions the class for the resource?

            String predicateUri = stmt.getDatapropURI();
            result = isAuthorizedFor(new Property(predicateUri));
        } else if (whatToAuth instanceof DisplayObjectPropertyStatement) {

            // Subject [((DisplayObjectPropertyStatement)whatToAuth).getSubjectUri()] is a resource
            // Object [((DisplayObjectPropertyStatement)whatToAuth).getObjectUri()] is resource
            // Previous auth code always evaluated as true when checking permissions for resources
            // Do we need to implement a check on permissions the class for the resource?

            Property op = ((DisplayObjectPropertyStatement)whatToAuth).getProperty();
            result = isAuthorizedFor(op);
        }

        if (result) {
            log.debug(this + " authorizes " + whatToAuth);
        } else {
            log.debug(this + " does not authorize " + whatToAuth);
        }

        return result;
    }
}
