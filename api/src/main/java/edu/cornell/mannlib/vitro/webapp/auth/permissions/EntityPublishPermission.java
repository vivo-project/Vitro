/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.permissions;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.publish.PublishDataProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.publish.PublishDataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.publish.PublishObjectProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.publish.PublishObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class EntityPublishPermission extends EntityPermission {
    private static final Log log = LogFactory.getLog(EntityPublishPermission.class);

    public EntityPublishPermission(String uri) {
        super(uri);
    }

    @Override
    public boolean isAuthorized(List<String> personUris, RequestedAction whatToAuth) {
        boolean result = false;

        if (whatToAuth instanceof PublishDataProperty) {
            String predicateUri = ((PublishDataProperty)whatToAuth).getDataProperty().getURI();
            result = isAuthorizedFor(new Property(predicateUri));
        } else if (whatToAuth instanceof PublishObjectProperty) {
            ObjectProperty op = ((PublishObjectProperty)whatToAuth).getObjectProperty();
            result = isAuthorizedFor(op);
        } else if (whatToAuth instanceof PublishDataPropertyStatement) {

            // Subject [((PublishDataPropertyStatement)whatToAuth).getSubjectUri()] is a resource
            // Previous auth code always evaluated as true when checking permissions for resources
            // Do we need to implement a check on permissions the class for the resource?

            String predicateUri = ((PublishDataPropertyStatement)whatToAuth).getPredicateUri();
            result = isAuthorizedFor(new Property(predicateUri));
        } else if (whatToAuth instanceof PublishObjectPropertyStatement) {

            // Subject [((PublishObjectPropertyStatement)whatToAuth).getSubjectUri()] is a resource
            // Object  [((PublishObjectPropertyStatement)whatToAuth).getObjectUri()] is a resource
            // Previous auth code always evaluated as true when checking permissions for resources
            // Do we need to implement a check on permissions the class for the resource?

            Property predicate = ((PublishObjectPropertyStatement)whatToAuth).getPredicate();
            result = isAuthorizedFor(predicate);
        }

        if (result) {
            log.debug(this + " authorizes " + whatToAuth);
        } else {
            log.debug(this + " does not authorize " + whatToAuth);
        }

        return result;
    }
}
