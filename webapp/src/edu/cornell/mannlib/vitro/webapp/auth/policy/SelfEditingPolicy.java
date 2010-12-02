/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.impl.Util;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
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

    private static final String[] DEFAULT_PROHIBITED_PROPERTIES = {};

    private static final String[] DEFAULT_PROHIBITED_RESOURCES = {};
	
    private static final String[] DEFAULT_PROHIBITED_NAMESPACES = {
			VitroVocabulary.vitroURI, 
			VitroVocabulary.OWL, 
			"" 
			};
    
	private static final String[] DEFAULT_EDITABLE_VITRO_URIS = {
			VitroVocabulary.MONIKER, 
			VitroVocabulary.BLURB,
			VitroVocabulary.DESCRIPTION, 
			VitroVocabulary.MODTIME,
			VitroVocabulary.TIMEKEY,

			VitroVocabulary.CITATION, 
			VitroVocabulary.IND_MAIN_IMAGE,

			VitroVocabulary.LINK, 
			VitroVocabulary.PRIMARY_LINK,
			VitroVocabulary.ADDITIONAL_LINK, 
			VitroVocabulary.LINK_ANCHOR,
			VitroVocabulary.LINK_URL,

			VitroVocabulary.KEYWORD_INDIVIDUALRELATION,
			VitroVocabulary.KEYWORD_INDIVIDUALRELATION_INVOLVESKEYWORD,
			VitroVocabulary.KEYWORD_INDIVIDUALRELATION_INVOLVESINDIVIDUAL,
			VitroVocabulary.KEYWORD_INDIVIDUALRELATION_MODE 
			};
    
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
    
    public SelfEditingPolicy(
            Set<String>prohibitedProperties,
            Set<String>prohibitedResources,
            Set<String>prohibitedNamespaces,
            Set<String> editableVitroUris ,
            OntModel model){
        this.model = model;
        
		this.prohibitedProperties = useDefaultIfNull(prohibitedProperties,
				DEFAULT_PROHIBITED_PROPERTIES);
		this.prohibitedResources = useDefaultIfNull(prohibitedResources,
				DEFAULT_PROHIBITED_RESOURCES);
		this.prohibitedNs = useDefaultIfNull(prohibitedNamespaces,
				DEFAULT_PROHIBITED_NAMESPACES);
		this.editableVitroUris = useDefaultIfNull(editableVitroUris,
				DEFAULT_EDITABLE_VITRO_URIS);
    }

	private Set<String> useDefaultIfNull(Set<String> valueSet, String[] defaultArray) {
		Collection<String> strings = (valueSet == null) ? Arrays
				.asList(defaultArray) : valueSet;
		return Collections.unmodifiableSet(new HashSet<String>(strings));
	}

    private static final Authorization DEFAULT_AUTHORIZATION = Authorization.INCONCLUSIVE;
    
    public PolicyDecision isAuthorized(IdentifierBundle whoToAuth, RequestedAction whatToAuth) {
		if (whoToAuth == null) {
			return defaultDecision("whoToAuth was null");
		}
		if (whatToAuth == null) {
			return defaultDecision("whatToAuth was null");
		}
		if (whatToAuth instanceof OntoRequestedAction) {
			return defaultDecision("Won't authorize OntoRequestedActions");
		}
		if (whatToAuth instanceof AdminRequestedAction) {
			return defaultDecision("Won't authorize AdminRequestedActions");
		}
		if (getUrisOfSelfEditor(whoToAuth).isEmpty()) {
			return defaultDecision("no non-blacklisted SelfEditing Identifier " +
					"found in IdentifierBundle");
		}

        //kick off the visitor pattern
        return whatToAuth.accept(this, whoToAuth);
    }

    // ----------------------------------------------------------------------
	// Visitor methods.
	// ----------------------------------------------------------------------

    public PolicyDecision visit(IdentifierBundle ids, AddResource action) {
    	PolicyDecision pd = checkNullArguments(ids, action);
    	if (pd == null) pd = checkRestrictedResource(action.getSubjectUri());
    	if (pd == null) pd = authorizedDecision("May add resource.");
    	return pd;
    }

    public PolicyDecision visit(IdentifierBundle ids, DropResource action) {
    	PolicyDecision pd = checkNullArguments(ids, action);
    	if (pd == null) pd = checkRestrictedResource(action.getSubjectUri());
    	if (pd == null) pd = authorizedDecision("May remove resource.");
    	return pd;
    }

    public PolicyDecision visit(IdentifierBundle ids, AddObjectPropStmt action) {
    	PolicyDecision pd = checkNullArguments(ids, action);
    	if (pd == null) pd = checkRestrictedResource(action.uriOfSubject);
    	if (pd == null) pd = checkRestrictedResource(action.uriOfObject);
    	if (pd == null) pd = checkRestrictedPredicate(action.uriOfPredicate);
    	if (pd == null) pd = checkUserEditsAsSubjectOrObjectOfStmt(ids, action.uriOfSubject, action.uriOfObject);
    	if (pd == null) pd = defaultDecision("No basis for decision.");
    	return pd;
    }

    public PolicyDecision visit(IdentifierBundle ids, EditObjPropStmt action) {
    	PolicyDecision pd = checkNullArguments(ids, action);
    	if (pd == null) pd = checkRestrictedResource(action.uriOfSubject);
    	if (pd == null) pd = checkRestrictedResource(action.uriOfObject);
    	if (pd == null) pd = checkRestrictedPredicate(action.uriOfPredicate);
    	if (pd == null) pd = checkUserEditsAsSubjectOrObjectOfStmt(ids, action.uriOfSubject, action.uriOfObject);
    	if (pd == null) pd = defaultDecision("No basis for decision.");
    	return pd;
    }

    public PolicyDecision visit(IdentifierBundle ids, DropObjectPropStmt action) {
    	PolicyDecision pd = checkNullArguments(ids, action);
    	if (pd == null) pd = checkRestrictedResource(action.uriOfSubject);
    	if (pd == null) pd = checkRestrictedResource(action.uriOfObject);
    	if (pd == null) pd = checkRestrictedPredicate(action.uriOfPredicate);
    	if (pd == null) pd = checkUserEditsAsSubjectOrObjectOfStmt(ids, action.uriOfSubject, action.uriOfObject);
    	if (pd == null) pd = defaultDecision("No basis for decision.");
    	return pd;
    }

    public PolicyDecision visit(IdentifierBundle ids, AddDataPropStmt action) {
    	PolicyDecision pd = checkNullArguments(ids, action);
    	if (pd == null) pd = checkRestrictedResource(action.getResourceUri());
    	if (pd == null) pd = checkRestrictedPredicate(action.getDataPropUri());
    	if (pd == null) pd = checkUserEditsAsSubjectOfStmt(ids, action.getResourceUri());
    	if (pd == null) pd = defaultDecision("No basis for decision.");
    	return pd;
    }

    public PolicyDecision visit(IdentifierBundle ids, EditDataPropStmt action) {
    	PolicyDecision pd = checkNullArguments(ids, action);
    	if (pd == null) pd = checkRestrictedResource(action.uriOfSubject());
    	if (pd == null) pd = checkRestrictedPredicate(action.uriOfPredicate());
    	if (pd == null) pd = checkUserEditsAsSubjectOfStmt(ids, action.uriOfSubject());
    	if (pd == null) pd = defaultDecision("No basis for decision.");
    	return pd;
    }

    public PolicyDecision visit(IdentifierBundle ids, DropDataPropStmt action) {
    	PolicyDecision pd = checkNullArguments(ids, action);
    	if (pd == null) pd = checkRestrictedResource(action.uriOfSubject());
    	if (pd == null) pd = checkRestrictedPredicate(action.uriOfPredicate());
    	if (pd == null) pd = checkUserEditsAsSubjectOfStmt(ids, action.uriOfSubject());
    	if (pd == null) pd = defaultDecision("No basis for decision.");
    	return pd;
   }

	public PolicyDecision visit(IdentifierBundle ids, AddNewUser action) {
		return defaultDecision("does not authorize administrative modifications");
	}

    public PolicyDecision visit(IdentifierBundle ids, RemoveUser action) {
		return defaultDecision("does not authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, LoadOntology action) {
		return defaultDecision("does not authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, RebuildTextIndex action) {
		return defaultDecision("does not authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, UpdateTextIndex action) {
		return defaultDecision("does not authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, ServerStatus action) {
		return defaultDecision("does not authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, CreateOwlClass action) {
		return defaultDecision("does not authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, RemoveOwlClass action) {
		return defaultDecision("does not authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, DefineDataProperty action) {
		return defaultDecision("does not authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, DefineObjectProperty action) {
		return defaultDecision("does not authorize administrative modifications");
    }

    public PolicyDecision visit(IdentifierBundle ids, UploadFile action) {
		return defaultDecision("does not authorize administrative modifications");
    }

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private PolicyDecision checkNullArguments(IdentifierBundle ids,
			RequestedAction action) {
		if (ids == null || action == null) {
			return defaultDecision("Null action or ids.");
		}
		return null;
	}

	private PolicyDecision checkRestrictedResource(String uri) {
		if (!canModifyResource(uri)) {
			return defaultDecision("No access to admin resources; "
					+ "cannot modify " + uri);
		}
		return null;
	}

	private PolicyDecision checkRestrictedPredicate(String uri) {
		if (!canModifyPredicate(uri)) {
			return defaultDecision("No access to admin predicates; "
					+ "cannot modify " + uri);
		}
		return null;
	}

	private PolicyDecision checkUserEditsAsSubjectOfStmt(IdentifierBundle ids,
			String uriOfSubject) {
    	List<String> userUris = getUrisOfSelfEditor(ids);
    	for (String userUri: userUris) {
    		if (userUri.equals(uriOfSubject)) {
    			return authorizedDecision("User is subject of statement.");
    		}
    	}
    	return null;
	}

	private PolicyDecision checkUserEditsAsSubjectOrObjectOfStmt(IdentifierBundle ids,
			String uriOfSubject, String uriOfObject) {
    	List<String> userUris = getUrisOfSelfEditor(ids);
    	for (String userUri: userUris) {
    		if (userUri.equals(uriOfSubject)) {
    			return authorizedDecision("User is subject of statement.");
    		}
    		if (userUri.equals(uriOfObject)) {
    			return authorizedDecision("User is subject of statement.");
    		}
    	}
    	return null;
	}

	private List<String> getUrisOfSelfEditor(IdentifierBundle ids) {
		List<String> uris = new ArrayList<String>();
		if (ids != null) {
			for (Identifier id : ids) {
				if (id instanceof SelfEditing) {
					SelfEditing selfEditId = (SelfEditing) id;
					if (selfEditId.getBlacklisted() == null) {
						uris.add(selfEditId.getValue());
					}
				}
			}
		}
		return uris;
	}

	/** Package-level access to allow for unit tests. */
	boolean canModifyResource(String uri) {
		if (uri == null || uri.length() == 0) {
			log.debug("Resource URI is empty: " + uri);
			return false;
		}

		if (editableVitroUris.contains(uri)) {
			log.debug("Resource matches an editable URI: " + uri);
			return true;
		}

		String namespace = uri.substring(0, Util.splitNamespace(uri));
		if (prohibitedNs.contains(namespace)) {
			log.debug("Resource matches a prohibited namespace: " + uri);
			return false;
		}

		log.debug("Resource is not prohibited: " + uri);
		return true;
	}

	/** Package-level access to allow for unit tests. */
	boolean canModifyPredicate(String uri) {
		if (uri == null || uri.length() == 0) {
			log.debug("Predicate URI is empty: " + uri);
			return false;
		}

		if (prohibitedProperties.contains(uri)) {
			log.debug("Predicate matches a prohibited predicate: " + uri);
			return false;
		}

		if (editableVitroUris.contains(uri)) {
			return true;
		}

		String namespace = uri.substring(0, Util.splitNamespace(uri));
		if (prohibitedNs.contains(namespace)) {
			log.debug("Predicate matches a prohibited namespace: " + uri);
			return false;
		}
		return true;
	}

	private PolicyDecision defaultDecision(String message) {
		return new BasicPolicyDecision(DEFAULT_AUTHORIZATION,
				"SelfEditingPolicy: " + message);
	}    

	private PolicyDecision authorizedDecision(String message) {
		return new BasicPolicyDecision(Authorization.AUTHORIZED,
				"SelfEditingPolicy: " + message);
	}    

	@Override
	public String toString() {
		return "SelfEditingPolicy " + hashCode() + "[prohibitedNs="
				+ prohibitedNs + ", prohibitedProperties="
				+ prohibitedProperties + ", prohibitedResources="
				+ prohibitedResources + ", editableVitroUris="
				+ editableVitroUris + "]";
	}

}
