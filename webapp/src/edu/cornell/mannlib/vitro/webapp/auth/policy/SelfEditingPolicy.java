/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory.SelfEditing;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;
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
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.AdminRequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.OntoRequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.CreateOwlClass;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.DefineDataProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.DefineObjectProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.RemoveOwlClass;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * Policy to use for Vivo Self-Editing based on NetId for use at Cornell.
 * All methods in this class should be thread safe
 * and side effect free.
 */
public class SelfEditingPolicy implements VisitingPolicyIface {
    protected static Log log = LogFactory.getLog( SelfEditingPolicy.class );

    /**
     * Namespaces from which Self Editors should not be able to use resources.
     */
    private  Set<String> prohibitedNs;

    /** URIs of properties that SelfEditors should not be able to use in statements*/
    protected  Set<String>prohibitedProperties;

    /** URIs of resources that SelfEditors should not be able to use in statements*/
    protected  Set<String>prohibitedResources;

    /** URIs of properties from prohibited namespaces that Self Editors need to be
     * able to edit */
    protected  Set<String> editableVitroUris;       
    
    protected OntModel model;
    
    /** Indicates which Authorization to use when the user isn't explicitly authorized. */
    private static Authorization defaultFailure = Authorization.INCONCLUSIVE;

    


    public SelfEditingPolicy(
            Set<String>prohibitedProperties,
            Set<String>prohibitedResources,
            Set<String>prohibitedNamespaces,
            Set<String> editableVitroUris ,
            OntModel model){
        this.model = model;
        
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
            return pd.setMessage("whoToAuth was null");
        if(whatToAuth == null)
            return pd.setMessage("whatToAuth was null");

        SelfEditingIdentifierFactory.SelfEditing selfEditId = SelfEditingIdentifierFactory.getSelfEditingIdentifier(whoToAuth);
        if( selfEditId == null )
            return pd.setMessage("no SelfEditing Identifier found in IdentifierBundle");
        
        if( selfEditId.getBlacklisted() != null ){
            //pd.setAuthorized(Authorization.UNAUTHORIZED);
            return pd.setMessage("user blacklisted because of " + selfEditId.getBlacklisted());
        }
        
        String editorUri = selfEditId.getValue();
        if (editorUri == null)
            return new BasicPolicyDecision(Authorization.INCONCLUSIVE,
                    "No Identifiers Related to SelfEditing found");        
        
        if (whatToAuth instanceof OntoRequestedAction)
            return pd.setMessage("JenaNetidPolicy doesn't authorize OntoRequestedActions");
        if (whatToAuth instanceof AdminRequestedAction)
            return pd.setMessage("JenaNetidPolicy doesn't authorize AdminRequestedActions");

        //kick off the visitor pattern
        return whatToAuth.accept(this, whoToAuth);
    }

    protected String getUriOfEditor( IdentifierBundle whoToAuth) {
        if( whoToAuth == null ) return null;

        String uriStr = null;
        for(Identifier id : whoToAuth){
            if (id instanceof SelfEditing) {
                SelfEditing seu = (SelfEditing) id;
                uriStr = seu.getValue();
                log.debug("found SelfEditingUri " + uriStr);
                break;
            }
        }
        return uriStr;
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

        if( prohibitedProperties.contains(uri)) {
            log.debug("The uri "+uri+" represents a predicate that cannot be modified because it is on a list of properties prohibited from self editing");
            return false;
        }
        
        if( editableVitroUris.contains( uri ) )
            return true;

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
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy, null action or ids");

        //cannot edit resources related to system
        if( !canModifyResource( action.uriOfObject ) )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy does not grant access to admin resources; " +
                    "cannot modify " + action.uriOfObject);

        if(  !canModifyResource( action.uriOfSubject ) )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy does not grant access to admin resources; " +
                    "cannot modify " + action.uriOfSubject);

        if( !canModifyPredicate( action.uriOfPredicate ) )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy does not grant access to admin predicates; " +
                    "cannot modify " + action.uriOfPredicate);

        String userUri = getUriOfEditor(ids);
        if( userUri == null )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy, no uri found for editor");

        if( userUri.equals( action.uriOfObject ) )
            return new BasicPolicyDecision(Authorization.AUTHORIZED,"SelfEditingPolicy: user is object of statement");
        if( userUri.equals( action.uriOfSubject ) )
            return new BasicPolicyDecision(Authorization.AUTHORIZED,"SelfEditingPolicy: user is subject of statement");

        return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy: no close relation to editor");
    }


    public PolicyDecision visit(IdentifierBundle ids, DropResource action) {
        if( ids == null || action == null )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy, null action or ids");

        if(  prohibitedNs.contains( action.getSubjectUri() ) )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy does not removal of admin resources");

        return new BasicPolicyDecision(Authorization.AUTHORIZED,"SelfEditingPolicy: may remove resource");
    }

    public PolicyDecision visit(IdentifierBundle ids, AddResource action) {
        if( ids == null || action == null )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy, null action or ids");

        if(  prohibitedNs.contains( action.getSubjectUri() ) )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy does not allow creation of admin resources");

        return new BasicPolicyDecision(Authorization.AUTHORIZED,"SelfEditingPolicy: may add resource");
    }

    public PolicyDecision visit(IdentifierBundle ids, DropDataPropStmt action) {
        if( ids == null || action == null ) {
            log.debug("SelfEditingPolicy for DropDataPropStmt is inconclusive because the test has null action or ids");
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy, null action or ids");
        }
        //cannot edit resources related to system
        if(  prohibitedNs.contains( action.uriOfSubject() ) ) {
            log.debug("SelfEditingPolicy for DropDatapropStmt is inconclusive because it does not grant access to admin resources");
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy does not grant access to admin resources");
        }

        //many predicates are prohibited by namespace but there are many ones that self editors need to work with
        if(  prohibitedNs.contains(action.uriOfPredicate() )  ) {
            log.debug("SelfEditingPolicy for DropDatapropStmt is inconclusive because it does not grant access to admin controls");
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy does not grant access to admin controls");
        }

        if( !canModifyPredicate( action.uriOfPredicate() ) )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy does not grant access to admin predicates; " +
                    "cannot modify " + action.uriOfPredicate());
        
        String userUri = getUriOfEditor(ids);
        if( userUri == null ) {
            log.debug("SelfEditingPolicy for DropDatapropStmt is inconclusive because found no uri for editor");
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy, no uri found for editor");
        }

        if( userUri.equals( action.uriOfSubject() ) ) {
            log.debug("SelfEditingPolicy for DropDatapropStmt authorizes since user is subject of statement");
            return new BasicPolicyDecision(Authorization.AUTHORIZED,"SelfEditingPolicy: user is subject of statement");
        }

        log.debug("SelfEditingPolicy for DropDatapropStmt returns inconclusive because the statement has no close relation to the editor");
        return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy: no close relation to editor");
   }


    public PolicyDecision visit(IdentifierBundle ids, DropObjectPropStmt action) {
        if( ids == null || action == null )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy, null action or ids");

        //cannot edit resources related to system
        if( !canModifyResource( action.uriOfObject ) )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy does not grant access to admin resources; " +
                    "cannot modify " + action.uriOfObject);

        if(  !canModifyResource( action.uriOfSubject ) )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy does not grant access to admin resources; " +
                    "cannot modify " + action.uriOfSubject);

        if( !canModifyPredicate( action.uriOfPredicate ) )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy does not grant access to admin predicates; " +
                    "cannot modify " + action.uriOfPredicate);

        String userUri = getUriOfEditor(ids);
        if( userUri == null )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy, no uri found for editor");

        if( userUri.equals( action.uriOfObject ) )
            return new BasicPolicyDecision(Authorization.AUTHORIZED,"SelfEditingPolicy: user is object of statement");
        if( userUri.equals( action.uriOfSubject ) )
            return new BasicPolicyDecision(Authorization.AUTHORIZED,"SelfEditingPolicy: user is subject of statement");

        return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy: no close relation to editor");
    }

    public PolicyDecision visit(IdentifierBundle ids, AddDataPropStmt action) {
        if( ids == null || action == null )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy, null action or ids");

        //cannot edit resources related to system
        if(  prohibitedNs.contains( action.getResourceUri() ) )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy does not grant access to admin resources");

        if(  prohibitedProperties.contains( action.getDataPropUri() ) )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy does not grant access to admin controls");
        
        if( !canModifyPredicate( action.getDataPropUri() ) )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy does not grant access to admin predicates; " +
                    "cannot modify " + action.getDataPropUri());
        
        String userUri = getUriOfEditor(ids);
        if( userUri == null )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy, no uri found for editor");

        if( userUri.equals( action.getResourceUri() ) )
            return new BasicPolicyDecision(Authorization.AUTHORIZED,"SelfEditingPolicy: user is subject of statement");

        return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy: no close relation to editor");
    }


    public PolicyDecision visit(IdentifierBundle ids, EditDataPropStmt action) {

        if( ids == null || action == null ) {
            log.debug("SelfEditingPolicy for EditDataPropStmt is inconclusive because the test has null action or ids");
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy, null action or ids");
        }

        //cannot edit resources related to system
        if( !canModifyResource( action.uriOfSubject() ) ) {
            log.debug("SelfEditingPolicy for EditDatapropStmt action is inconclusive because it does not grant access to admin resources; cannot modify " + action.uriOfSubject());
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy does not grant access to admin resources; " +
                    "cannot modify " + action.uriOfSubject());
        }
        if( !canModifyPredicate( action.uriOfPredicate() ) ) {
            log.debug("SelfEditingPolicy for EditDatapropStmt is inconclusive because it does not grant access to admin predicates; cannot modify " + action.uriOfPredicate());
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy does not grant access to admin predicates; " +
                    "cannot modify " + action.uriOfPredicate());
        }        
        String userUri = getUriOfEditor(ids);
        if( userUri == null ) {
            log.debug("SelfEditingPolicy for EditDatapropStmt returns inconclusive because no uri was found for editor");
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy, no uri found for editor");
        }
        if( userUri.equals( action.uriOfSubject() ) ) {
            log.debug("SelfEditingPolicy for EditDatapropStmt returns authorization because the user is subject of statement");
            return new BasicPolicyDecision(Authorization.AUTHORIZED,"SelfEditingPolicy: user is subject of statement");
        }
        log.debug("SelfEditingPolicy for EditDatapropStmt returns inconclusive because the statement has no close relation to the editor");
        return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy: no close relation to editor");
    }


    public PolicyDecision visit(IdentifierBundle ids, EditObjPropStmt action) {
        if( ids == null || action == null )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy, null action or ids");

        if( "http://vivoweb.org/ontology/core#informationResourceInAuthorship".equals( action.getUriOfPredicate() ) ){
            return canEditAuthorship(ids, action, model);            
        }
        
        //cannot edit resources related to system
        if( !canModifyResource( action.uriOfObject ) )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy does not grant access to admin resources; " +
                    "cannot modify " + action.uriOfObject);

        if(  !canModifyResource( action.uriOfSubject ) )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy does not grant access to admin resources; " +
                    "cannot modify " + action.uriOfSubject);

        if( !canModifyPredicate( action.uriOfPredicate ) )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy does not grant access to admin predicates; " +
                    "cannot modify " + action.uriOfPredicate);

        String userUri = getUriOfEditor(ids);
        if( userUri == null )
            return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy, no uri found for editor");

        if( userUri.equals( action.uriOfObject ) )
            return new BasicPolicyDecision(Authorization.AUTHORIZED,"SelfEditingPolicy: user is object of statement");
        if( userUri.equals( action.uriOfSubject ) )
            return new BasicPolicyDecision(Authorization.AUTHORIZED,"SelfEditingPolicy: user is subject of statement");

        return new BasicPolicyDecision(this.defaultFailure,"SelfEditingPolicy: editor not involved in triple");
    }


    public PolicyDecision visit(IdentifierBundle ids, UploadFile action) {
        return new BasicPolicyDecision(Authorization.AUTHORIZED,"SelfEditingPolicy: may upload files");
    }


    // *** the following actions are generally not part of self editing *** //

    public PolicyDecision visit(IdentifierBundle ids, AddNewUser action) {
        return new BasicPolicyDecision(Authorization.INCONCLUSIVE,"SelfEditingPolicy does not authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, RemoveUser action) {
        return new BasicPolicyDecision(Authorization.INCONCLUSIVE,"SelfEditingPolicy does not authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, LoadOntology action) {
        return new BasicPolicyDecision(Authorization.INCONCLUSIVE,"SelfEditingPolicy does not authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, RebuildTextIndex action) {
        return new BasicPolicyDecision(Authorization.INCONCLUSIVE,"SelfEditingPolicy does not authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, UpdateTextIndex action) {
        return new BasicPolicyDecision(Authorization.INCONCLUSIVE,"SelfEditingPolicy does not authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, ServerStatus action) {
        return new BasicPolicyDecision(Authorization.INCONCLUSIVE,"SelfEditingPolicy does not authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, CreateOwlClass action) {
        return new BasicPolicyDecision(Authorization.INCONCLUSIVE,"SelfEditingPolicy does not authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, RemoveOwlClass action) {
        return new BasicPolicyDecision(Authorization.INCONCLUSIVE,"SelfEditingPolicy does not authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, DefineDataProperty action) {
        return new BasicPolicyDecision(Authorization.INCONCLUSIVE,"SelfEditingPolicy does not authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, DefineObjectProperty action) {
        return new BasicPolicyDecision(Authorization.INCONCLUSIVE,"SelfEditingPolicy does not authorize administrative modifications");
    }


    private PolicyDecision canEditAuthorship(IdentifierBundle ids, EditObjPropStmt action, OntModel model2) {        
        PolicyDecision pd = null;
        String selfEditorUri = SelfEditingIdentifierFactory.getSelfEditingUri(ids);
        if( selfEditorUri == null || selfEditorUri.isEmpty() )
            return pd;
                
        model2.enterCriticalSection(Lock.READ);
        try{
            if( action != null && action.getUriOfObject() != null ){
                Individual authorship = model2.getIndividual(action.getUriOfObject());
                if( authorship != null ){                    
                    NodeIterator authors = authorship.listPropertyValues(LINKED_AUTHOR_PROPERTY );
                    try{
                        while(authors.hasNext()){
                            Resource author = (Resource)authors.nextNode();
                            if( author != null && selfEditorUri.equals( author.getURI() ) ){
                                pd = new BasicPolicyDecision(Authorization.AUTHORIZED, "SelfEditingPolicy, may edit because SelfEditor is author");
                                
                            }                        
                        }
                    }finally{
                        if( authors != null)
                            authors.close();
                    }                    
                }
            }
        }finally{
            model2.leaveCriticalSection();
        }
        if( pd == null )
            return new BasicPolicyDecision(Authorization.INCONCLUSIVE,
            "SelfEditingPolicy from canEditAuthorship");
        else
            return pd;
    }

    private static Property LINKED_AUTHOR_PROPERTY = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#linkedAuthor");
    
    public String toString(){
        return "SelfEditingPolicy " + hashCode()
        + " nspaces: " + prohibitedNs.size() + " prohibited Props: "
        + prohibitedProperties.size() + " prohibited resources: "
        + prohibitedResources.size();
    }
    
    public static void setDefaultFailure( Authorization defaultFail){
        SelfEditingPolicy.defaultFailure = defaultFail;       
    }
    
    
}
