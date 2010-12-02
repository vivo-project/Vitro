/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.impl.Util;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.DbAdminEditingIdentifierFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory.SelfEditing;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddResource;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.DropResource;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.AddNewUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.LoadOntology;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.RebuildTextIndex;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.RemoveUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.ServerStatus;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.UpdateTextIndex;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.UploadFile;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.AdminRequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.OntoRequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
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
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * Policy to use for Vivo non-privileged but user accouunt-based editing
 * All methods in this class should be thread safe
 * and side effect free.
 */
public class DbAdminEditingPolicy implements VisitingPolicyIface {
    protected static Log log = LogFactory.getLog( DbAdminEditingPolicy.class );

    /** regex for extracting a namespace from a URI */
    // Do not use this; use Jena's splitNamespace() util instead.
    //private Pattern ns = Pattern.compile("([^#]*#)[^#]*");

    /**
     * Namespaces from which DbAdmins should not be able to use resources.
     */
    private  Set<String> prohibitedNs;

    /** URIs of properties that DbAdmins should not be able to use in statements*/
    protected  Set<String>prohibitedProperties;

    /** URIs of resources that DbAdmins should not be able to use in statements*/
    protected  Set<String>prohibitedResources;
    
    /** Indicates which Authorization to use when the user isn't explicitly authorized. */
    protected Authorization defaultFailure = Authorization.INCONCLUSIVE;

    /** URIs of properties from prohibited namespaces that DbAdmins need to be
     * able to edit */
    protected  Set<String> editableVitroUris;

    public DbAdminEditingPolicy(
            Set<String>prohibitedProperties,
            Set<String>prohibitedResources,
            Set<String>prohibitedNamespaces,
            Set<String>editableVitroUris ){

        if( prohibitedProperties != null )
            this.prohibitedProperties = prohibitedProperties;
        else 
            this.prohibitedProperties = Collections.EMPTY_SET;

        if( prohibitedResources != null )
            this.prohibitedResources = prohibitedResources;
        else
            this.prohibitedResources =  Collections.EMPTY_SET;
        
        if( prohibitedNamespaces != null )
            this.prohibitedNs = prohibitedNamespaces;
        else{
            prohibitedNs = new HashSet<String>();
            prohibitedNs.add( VitroVocabulary.vitroURI);
            prohibitedNs.add( VitroVocabulary.OWL );
            prohibitedNs.add("");
        }

        if( editableVitroUris != null )
            this.editableVitroUris = editableVitroUris;
        else{
            this.editableVitroUris = new HashSet<String>();
            this.editableVitroUris.add(VitroVocabulary.MONIKER);
            this.editableVitroUris.add(VitroVocabulary.BLURB);
            this.editableVitroUris.add(VitroVocabulary.DESCRIPTION);            
            this.editableVitroUris.add(VitroVocabulary.MODTIME);
            this.editableVitroUris.add(VitroVocabulary.TIMEKEY);

            this.editableVitroUris.add(VitroVocabulary.CITATION);
            this.editableVitroUris.add(VitroVocabulary.IND_MAIN_IMAGE);

            this.editableVitroUris.add(VitroVocabulary.LINK);
            this.editableVitroUris.add(VitroVocabulary.PRIMARY_LINK);
            this.editableVitroUris.add(VitroVocabulary.ADDITIONAL_LINK);
            this.editableVitroUris.add(VitroVocabulary.LINK_ANCHOR);
            this.editableVitroUris.add(VitroVocabulary.LINK_URL);

            this.editableVitroUris.add(VitroVocabulary.KEYWORD_INDIVIDUALRELATION);
            this.editableVitroUris.add(VitroVocabulary.KEYWORD_INDIVIDUALRELATION_INVOLVESKEYWORD);
            this.editableVitroUris.add(VitroVocabulary.KEYWORD_INDIVIDUALRELATION_INVOLVESINDIVIDUAL);
            this.editableVitroUris.add(VitroVocabulary.KEYWORD_INDIVIDUALRELATION_MODE);
        }
    }

    public PolicyDecision isAuthorized(IdentifierBundle whoToAuth, RequestedAction whatToAuth) {
        BasicPolicyDecision pd = new BasicPolicyDecision(this.defaultFailure,"not yet set");
        if( whoToAuth == null )
            return pd.setMessage("whomToAuth was null");
        if(whatToAuth == null)
            return pd.setMessage("whatToAuth was null");
                
        String roleStr = getRoleOf(whoToAuth);
        if (roleStr == null)
            return pd.setMessage("Unable to get a role for the dbAdmin from IdBundle");
        
        try{
            if( Integer.parseInt( roleStr ) /*<*/ != LoginStatusBean.DBA) {
                return pd.setMessage("DbAdminEditingPolicy found role of "+roleStr+" and only authorizes for users logged in as DB_ADMIN");
            }
        } catch(NumberFormatException nef){
            log.debug(nef,nef);
        }
        
        try{
            SelfEditing sei = SelfEditingIdentifierFactory.getSelfEditingIdentifier(whoToAuth);
            if( sei != null && sei.isFake() ){
                return pd.setMessage("DbAdminEditingPolicy will not authorize actions for a fake self editor");                
            }
        }catch( Exception e ){
            log.debug(e,e);
        }
        
        if (whatToAuth instanceof OntoRequestedAction)
            return pd.setMessage("DbAdminEditingPolicy doesn't authorize OntoRequestedActions");
        if (whatToAuth instanceof AdminRequestedAction)
            return pd.setMessage("DbAdminEditingPolicy doesn't authorize AdminRequestedActions");

        //kick off the visitor pattern
        return whatToAuth.accept(this, whoToAuth);
    }

    
    protected String getRoleOf( IdentifierBundle whomToAuth) {
        if( whomToAuth == null ) return null;
        
        for(Identifier id : whomToAuth){
            if (id instanceof DbAdminEditingIdentifierFactory.DbAdminEditingId) {
                return ((DbAdminEditingIdentifierFactory.DbAdminEditingId)id).getRole();
            }
        }
        return null;
    }

    protected boolean canModifyResource(String uri){
        if( uri == null || uri.length() == 0 )
            return false;

        if( editableVitroUris.contains( uri ) )
            return true;

        String namespace = uri.substring(0, Util.splitNamespace(uri));
        //Matcher match = ns.matcher(uri);
        //if( match.matches() && match.groupCount() > 0){
        //    String namespace = match.group(1);
            if( prohibitedNs.contains( namespace ) ) {
                log.debug("The uri "+uri+" represents a resource that cannot be modified because it matches a prohibited namespace");
                return false;
            }
        //} 
        return true;         
    }


    protected boolean canModifyPredicate(String uri){
        if( uri == null || uri.length() == 0 )
            return false;

        if( editableVitroUris.contains( uri ) ) // properties like moniker that are never (currently) set non-editable
            return true;

        if( prohibitedProperties.contains(uri)) {
            log.debug("The uri "+uri+" represents a predicate that cannot be modified because it is on a list of properties prohibited from dbAdmin editing");
            return false;
        }
        
        String namespace = uri.substring(0, Util.splitNamespace(uri));
        //Matcher match = ns.matcher(uri);
        //if( match.matches() && match.groupCount() > 0){
        //    String namespace = match.group(1);
            if( prohibitedNs.contains( namespace ) ) {
                log.debug("The uri "+uri+" represents a predicate that cannot be modified because it matches a prohibited namespace");
                return false;
            }
        //} 
        return true;         
    }

    public PolicyDecision visit(IdentifierBundle ids, AddObjectPropStmt action) {
        if( ids == null || action == null )
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy, null action or ids");

        //cannot edit resources related to system
        if( !canModifyResource( action.uriOfObject ) )
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy does not grant access to admin resources; " +
                    "cannot modify " + action.uriOfObject);

        if(  !canModifyResource( action.uriOfSubject ) )
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy does not grant access to admin resources; " +
                    "cannot modify " + action.uriOfSubject);

        if( !canModifyPredicate( action.uriOfPredicate ) )
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy does not grant access to prohibited predicates or certain namespaces: " +
                    "cannot modify " + action.uriOfPredicate);
        
        return new BasicPolicyDecision(Authorization.AUTHORIZED,"DbAdminEditingPolicy: user can edit allowed properties of anybody");

        /* see SelfEditingPolicy for examples of any individual-based policy decisions */
    }


    public PolicyDecision visit(IdentifierBundle ids, DropResource action) {
        if( ids == null || action == null )
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy, null action or ids");

        if(  prohibitedNs.contains( action.getSubjectUri() ) )
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy does not removal of admin resources");

        return new BasicPolicyDecision(Authorization.AUTHORIZED,"DbAdminEditingPolicy: may remove resource");
    }

    public PolicyDecision visit(IdentifierBundle ids, AddResource action) {
        if( ids == null || action == null )
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy, null action or ids");

        if(  prohibitedNs.contains( action.getSubjectUri() ) )
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy does not allow creation of admin resources");

        return new BasicPolicyDecision(Authorization.AUTHORIZED,"DbAdminEditingPolicy: may add resource");
    }

    public PolicyDecision visit(IdentifierBundle ids, DropDataPropStmt action) {
        if( ids == null || action == null ) {
            log.debug("DbAdminEditingPolicy for DropDataPropStmt is inconclusive because the test has null action or ids");
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy, null action or ids");
        }
        //cannot edit resources related to system
        if(  prohibitedNs.contains( action.uriOfSubject() ) ) { // jc55 was getResourceURI()
            log.debug("DbAdminEditingPolicy for DropDatapropStmt is inconclusive because it does not grant access to admin resources");
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy does not grant access to admin resources");
        }

        //many predicates are prohibited by namespace but there are many ones that dbAdmin editors need to work with
        if(  prohibitedNs.contains(action.uriOfPredicate() ) && ! editableVitroUris.contains( action.uriOfPredicate() ) ) {
            log.debug("DbAdminEditingPolicy for DropDatapropStmt is inconclusive because it does not grant access to admin controls");
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy does not grant access to admin controls");
        }
        
        //cannot edit resources related to system
        if( !canModifyResource( action.uriOfSubject() ) ) {
            log.debug("DbAdminEditingPolicy for EditDatapropStmt action is inconclusive because it does not grant access to admin resources; cannot modify " + action.uriOfSubject());
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy does not grant access to admin resources; " +
                    "cannot modify " + action.uriOfSubject());
        }
        if( !canModifyPredicate( action.uriOfPredicate() ) ) {
            log.debug("DbAdminEditingPolicy does not grant access to prohibited predicates or certain namespaces: cannot modify " + action.uriOfPredicate());
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy does not grant access to prohibited predicates or certain namespaces: " +
                    "cannot modify " + action.uriOfPredicate());
        }
        log.debug("DbAdminEditingPolicy for DropDatapropStmt returns authorization because the user is a dbAdmin");
        
        return new BasicPolicyDecision(Authorization.AUTHORIZED,"DbAdminEditingPolicy: user is may drop data property statement");

        /* see SelfEditingPolicy for examples of any individual-based policy decisions */
    }


    public PolicyDecision visit(IdentifierBundle ids, DropObjectPropStmt action) {
        if( ids == null || action == null )
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy, null action or ids");

        //cannot edit resources related to system
        if( !canModifyResource( action.uriOfObject ) )
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy does not grant access to admin resources; " +
                    "cannot modify " + action.uriOfObject);

        if(  !canModifyResource( action.uriOfSubject ) )
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy does not grant access to admin resources; " +
                    "cannot modify " + action.uriOfSubject);

        if( !canModifyPredicate( action.uriOfPredicate ) )
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy does not grant access to prohibited predicates or certain namespaces: " +
                    "cannot modify " + action.uriOfPredicate);

        return new BasicPolicyDecision(Authorization.AUTHORIZED,"DbAdminEditingPolicy: user can edit any individual");

        /* see SelfEditingPolicy for examples of any individual-based policy decisions */
    }

    public PolicyDecision visit(IdentifierBundle ids, AddDataPropStmt action) {
        if( ids == null || action == null )
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy has null action or ids");

        //cannot edit resources related to system
        if(  prohibitedNs.contains( action.getResourceUri() ) )
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy does not grant access to admin resources from prohibited namespaces");

        //many predicates are prohibited by namespace but there are many ones that dbAdmin editors need to work with
        if(  prohibitedNs.contains(action.getDataPropUri() ) && ! editableVitroUris.contains( action.getDataPropUri() ) )
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy does not grant access to admin controls");
        
        if( !canModifyPredicate( action.getDataPropUri() ) ) {
            log.debug("DbAdminEditingPolicy for AddDataPropStmt does not grant access to prohibited predicates or certain namespaces: cannot modify " + action.getDataPropUri());
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy for AddDataPropStmt does not grant access to prohibited predicates or certain namespaces: " +
                    "cannot modify " + action.getDataPropUri());
        }
        
        return new BasicPolicyDecision(Authorization.AUTHORIZED,"DbAdminEditingPolicy: user may add this data property statement");

        /* see SelfEditingPolicy for examples of any individual-based policy decisions */
    }


    public PolicyDecision visit(IdentifierBundle ids, EditDataPropStmt action) {
        
        if( ids == null || action == null ) {
            log.debug("DbAdminEditingPolicy for EditDataPropStmt is inconclusive because the test has null action or ids");
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy, null action or ids");
        }

        //cannot edit resources related to system
        if( !canModifyResource( action.uriOfSubject() ) ) {
            log.debug("DbAdminEditingPolicy for EditDatapropStmt action is inconclusive because it does not grant access to admin resources; cannot modify " + action.uriOfSubject());
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy does not grant access to admin resources; " +
                    "cannot modify " + action.uriOfSubject());
        }
        if( !canModifyPredicate( action.uriOfPredicate() ) ) {
            log.debug("DbAdminEditingPolicy for EditDataPropStmt does not grant access to prohibited predicates or certain namespaces: cannot modify " + action.uriOfPredicate());
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy for EditDataPropStmt does not grant access to prohibited predicates or certain namespaces: " +
                    "cannot modify " + action.uriOfPredicate());
        }
        
        log.debug("DbAdminEditingPolicy for EditDatapropStmt returns authorization because the user is a dbAdmin");
        return new BasicPolicyDecision(Authorization.AUTHORIZED,"DbAdminEditingPolicy: user may edit data property statement");

        /* see SelfEditingPolicy for examples of any individual-based policy decisions */
    }

    public PolicyDecision visit(IdentifierBundle ids, EditObjPropStmt action) {
        if( ids == null || action == null )
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy, null action or ids");

        //cannot edit resources related to system
        if( !canModifyResource( action.uriOfObject ) )
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy does not grant access to admin resources; " +
                    "cannot modify " + action.uriOfObject);

        if(  !canModifyResource( action.uriOfSubject ) )
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy does not grant access to admin resources; " +
                    "cannot modify " + action.uriOfSubject);

        if( !canModifyPredicate( action.uriOfPredicate ) )
            return new BasicPolicyDecision(this.defaultFailure,"DbAdminEditingPolicy for EditObjPropStmt does not grant access to prohibited predicates or certain namespaces: " +
                    "cannot modify " + action.uriOfPredicate);
        return new BasicPolicyDecision(Authorization.AUTHORIZED,"DbAdminEditingPolicy: user may edit any individual");

        /* see SelfEditingPolicy for examples of any individual-based policy decisions */
    }


    public PolicyDecision visit(IdentifierBundle ids, UploadFile action) {
        return new BasicPolicyDecision(Authorization.AUTHORIZED,"DbAdminEditingPolicy: may upload files");
    }


    // *** the following actions are generally not part of dbAdmin editing *** //

    public PolicyDecision visit(IdentifierBundle ids, AddNewUser action) {
        return new BasicPolicyDecision(Authorization.AUTHORIZED,"DbAdminEditingPolicy does authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, RemoveUser action) {
        return new BasicPolicyDecision(Authorization.AUTHORIZED,"DbAdminEditingPolicy does authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, LoadOntology action) {
        return new BasicPolicyDecision(Authorization.AUTHORIZED,"DbAdminEditingPolicy does authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, RebuildTextIndex action) {
        return new BasicPolicyDecision(Authorization.AUTHORIZED,"DbAdminEditingPolicy does authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, UpdateTextIndex action) {
        return new BasicPolicyDecision(Authorization.AUTHORIZED,"DbAdminEditingPolicy does authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, ServerStatus action) {
        return new BasicPolicyDecision(Authorization.AUTHORIZED,"DbAdminEditingPolicy does authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, CreateOwlClass action) {
        return new BasicPolicyDecision(Authorization.AUTHORIZED,"DbAdminEditingPolicy does not authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, RemoveOwlClass action) {
        return new BasicPolicyDecision(Authorization.AUTHORIZED,"DbAdminEditingPolicy does authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, DefineDataProperty action) {
        return new BasicPolicyDecision(Authorization.AUTHORIZED,"DbAdminEditingPolicy does authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, DefineObjectProperty action) {
        return new BasicPolicyDecision(Authorization.AUTHORIZED,"DbAdminEditingPolicy does authorize administrative modifications");
    }
    
    public String toString(){
        return "DbAdminEditingPolicy " + hashCode()  
        + " nspaces: " + prohibitedNs.size() + " prohibited Props: " 
        + prohibitedProperties.size() + " prohibited resources: " 
        + prohibitedResources.size();
    }
}
