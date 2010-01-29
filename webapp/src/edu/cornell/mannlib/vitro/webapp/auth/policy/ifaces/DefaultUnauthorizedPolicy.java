package edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.BasicPolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddResource;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.DropDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.DropObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.DropResource;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.EditDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.EditObjPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.AddNewUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.LoadOntology;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.RebuildTextIndex;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.RemoveUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.ServerStatus;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.UpdateTextIndex;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.UploadFile;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.CreateOwlClass;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.DefineDataProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.DefineObjectProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.RemoveOwlClass;

/**
 *
 *a policy where every type of action is authorized as UNAUTHORIZED
 * by default.  This can be useful for a unauthenticated session or
 * as the last policy on a PolicyList to force INCONCLUSIVE decisions
 * to UNAUTHORIZED.
 */
public class DefaultUnauthorizedPolicy implements PolicyIface{

    protected static PolicyDecision UNAUTHORIZED_DECISION = new BasicPolicyDecision(
            Authorization.UNAUTHORIZED,
            "This is the default decision defined in DefaultUnauthorizedPolicy");


    public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
            RequestedAction whatToAuth) {
        if (whoToAuth == null)
            return new BasicPolicyDecision(Authorization.UNAUTHORIZED,
                    "null was passed as whoToAuth");
        if (whatToAuth == null)
            return new BasicPolicyDecision(Authorization.UNAUTHORIZED,
                    "null was passed as whatToAuth");
        return UNAUTHORIZED_DECISION;
    }     
}
