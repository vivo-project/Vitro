/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddResource;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.DropResource;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.AddNewUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.LoadOntology;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.RebuildTextIndex;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.RemoveUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.ServerStatus;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.UpdateTextIndex;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.UploadFile;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.CreateOwlClass;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.DefineDataProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.DefineObjectProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.RemoveOwlClass;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditObjPropStmt;

/**
 * This is a interface to write a policy that uses the Visitor pattern.
 * In general this should be avoided, just implement PolicyIface.
 * 
 * @author bdc34
 *
 */
public interface VisitingPolicyIface extends PolicyIface {

    //visitor pattern abstract visitor:
    public abstract PolicyDecision visit(IdentifierBundle ids,
            CreateOwlClass action);

    public abstract PolicyDecision visit(IdentifierBundle ids,
            RemoveOwlClass action);

    public abstract PolicyDecision visit(IdentifierBundle ids,
            DefineDataProperty action);

    public abstract PolicyDecision visit(IdentifierBundle ids,
            DefineObjectProperty action);

    public abstract PolicyDecision visit(IdentifierBundle ids,
            AddObjectPropStmt action);

    public abstract PolicyDecision visit(IdentifierBundle ids,
            DropResource action);

    public abstract PolicyDecision visit(IdentifierBundle ids,
            DropDataPropStmt action);

    public abstract PolicyDecision visit(IdentifierBundle ids,
            DropObjectPropStmt action);

    public abstract PolicyDecision visit(IdentifierBundle ids,
            AddResource action);

    public abstract PolicyDecision visit(IdentifierBundle ids,
            AddDataPropStmt action);

    public abstract PolicyDecision visit(IdentifierBundle ids, AddNewUser action);

    public abstract PolicyDecision visit(IdentifierBundle ids, RemoveUser action);

    public abstract PolicyDecision visit(IdentifierBundle ids,
            LoadOntology action);

    public abstract PolicyDecision visit(IdentifierBundle ids,
            RebuildTextIndex action);

    public abstract PolicyDecision visit(IdentifierBundle ids,
            UpdateTextIndex action);

    public abstract PolicyDecision visit(IdentifierBundle ids, UploadFile action);

    public abstract PolicyDecision visit(IdentifierBundle ids,
            ServerStatus action);

    public abstract PolicyDecision visit(IdentifierBundle ids,
            EditDataPropStmt action);

    public abstract PolicyDecision visit(IdentifierBundle ids,
            EditObjPropStmt action);

}