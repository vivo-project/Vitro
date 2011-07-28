/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionSetsLoader;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class BaseResourceBean implements ResourceBean {
	
	private static final Log log = LogFactory.getLog(BaseResourceBean.class.getName());
    protected String URI          = null;
    protected String namespace    = null;
    protected String localName    = null;
    protected String localNameWithPrefix = null;
    protected String pickListName = null;
    
    // these will be phased in and used in the filters Brian C. has been setting up,
    // with hiddenFromDisplay to control the level at which any class, individual, object property, or data property is displayed
    // and prohibitedFromEditing to control when a control for editing is made available
    protected RoleLevel hiddenFromDisplayBelowRoleLevel = null;
    //protected RoleLevel prohibitedFromCreateBelowRoleLevel = null;
    protected RoleLevel prohibitedFromUpdateBelowRoleLevel = null;
    //protected RoleLevel prohibitedFromDeleteBelowRoleLevel = null;
    
    public enum RoleLevel { PUBLIC("http://vitro.mannlib.cornell.edu/ns/vitro/role#public","public","public"),
                            SELF("http://vitro.mannlib.cornell.edu/ns/vitro/role#selfEditor","self-authenticated","self"),
                            EDITOR("http://vitro.mannlib.cornell.edu/ns/vitro/role#editor","editor, curator, site administrator","editor"),
                            CURATOR("http://vitro.mannlib.cornell.edu/ns/vitro/role#curator","curator, site administrator","curator"),
                            DB_ADMIN("http://vitro.mannlib.cornell.edu/ns/vitro/role#dbAdmin","site administrator","siteAdmin"),
                            NOBODY("http://vitro.mannlib.cornell.edu/ns/vitro/role#nobody","root user","root");
    
        private final String uri;
        private final String label;
        private final String shorthand;
        
        RoleLevel(String uriStr,String labelStr, String shortStr) {
            this.uri = uriStr;
            this.label = labelStr;
            this.shorthand = shortStr;
        }
        
        public String getURI() {
            return uri;
        }
        
        public String getLabel() {
            return label;
        }

        public String getShorthand() {
            return shorthand;
        }
        
        public static RoleLevel getRoleByUri(String uri2) {
            if( uri2 == null )
                return RoleLevel.values()[0];
           
            for( RoleLevel role : RoleLevel.values() ){
                if( role.uri.equals( uri2 ) )
                    return role;
            }
            return RoleLevel.values()[0];            
        }
        
		public static RoleLevel getRoleFromLoginStatus(HttpServletRequest req) {
			UserAccount u = LoginStatusBean.getCurrentUser(req);
			if (u == null) {
				return PUBLIC;
			}
			
			Set<String> roles = u.getPermissionSetUris();
			if (roles.contains(PermissionSetsLoader.URI_DBA)) {
				return  DB_ADMIN;
			} else if (roles.contains(PermissionSetsLoader.URI_CURATOR)) {
				return CURATOR;
			} else if (roles.contains(PermissionSetsLoader.URI_EDITOR)) {
				return EDITOR;
			} else if (roles.contains(PermissionSetsLoader.URI_SELF_EDITOR)) {
				return SELF;
			} else {
				// Logged in but with no recognized role? Make them SELF
				return SELF;
			}
		}
    }

    public boolean isAnonymous() {        
    	return (this.URI==null || VitroVocabulary.PSEUDO_BNODE_NS.equals(this.getNamespace()));
    }
    
    public String getURI() {
        return URI;
    }
    public void setURI(String URI) {  
        if( this.localName != null || this.namespace != null)
            buildLocalAndNS(URI);
        else
            this.URI = URI;
    }

    private void buildLocalAndNS(String URI) {
        if (URI == null) {
            this.URI = null;
            this.namespace = null;
            this.localName = null;
        } else {
            this.URI = URI;
            Resource uri = ResourceFactory.createResource(URI);
            this.namespace = uri.getNameSpace();
            this.localName = uri.getLocalName();
        }
    }
    
    public String getNamespace() {
        if( namespace == null && this.URI != null)
            buildLocalAndNS(this.URI);        
        return namespace;
    }
    public void setNamespace(String namespace) {
        this.namespace = namespace;
        if (namespace != null && localName != null ) {
            this.URI = namespace + localName;
        }
    }

    public String getLocalName() {
        if( localName == null && this.URI != null)
            buildLocalAndNS(this.URI);        
        return localName;
    }
    
    public void setLocalName(String localName) {
        this.localName = localName;
        if (namespace != null && localName != null) {
            this.URI = namespace + localName;
        }
    }

    public String getLocalNameWithPrefix() {
        return localNameWithPrefix==null ? getLocalName()==null ? 
                (URI==null ? "(no name)" : URI ): getLocalName() : localNameWithPrefix;
    }
    public void setLocalNameWithPrefix(String prefixedLocalName) {
        this.localNameWithPrefix = prefixedLocalName;
    }
    
    public String getPickListName() {
        return pickListName==null ? getLocalName()==null ? 
                (URI==null ? "(no name)" : URI ): getLocalName() : pickListName;
    }
    public void setPickListName(String pickListName) {
        this.pickListName = pickListName;
    }
    
    public RoleLevel getHiddenFromDisplayBelowRoleLevel() {
        return hiddenFromDisplayBelowRoleLevel;
    }
    
    public void setHiddenFromDisplayBelowRoleLevel(RoleLevel eR) {
        hiddenFromDisplayBelowRoleLevel = eR;
    }
    
    public void setHiddenFromDisplayBelowRoleLevelUsingRoleUri(String roleUri) {
        hiddenFromDisplayBelowRoleLevel = BaseResourceBean.RoleLevel.getRoleByUri(roleUri);
    }

    /*
    public RoleLevel getProhibitedFromCreateBelowRoleLevel() {
        return prohibitedFromCreateBelowRoleLevel;
    }
    
    public void setProhibitedFromCreateBelowRoleLevel(RoleLevel eR) {
        prohibitedFromCreateBelowRoleLevel = eR;
    }
    
    public void setProhibitedFromCreateBelowRoleLevelUsingRoleUri(String roleUri) {
        prohibitedFromCreateBelowRoleLevel = BaseResourceBean.RoleLevel.getRoleByUri(roleUri);
    }
    */
    
    public RoleLevel getProhibitedFromUpdateBelowRoleLevel() {
        return prohibitedFromUpdateBelowRoleLevel;
    }
    
    public void setProhibitedFromUpdateBelowRoleLevel(RoleLevel eR) {
        prohibitedFromUpdateBelowRoleLevel = eR;
    }
    
    public void setProhibitedFromUpdateBelowRoleLevelUsingRoleUri(String roleUri) {
        prohibitedFromUpdateBelowRoleLevel = BaseResourceBean.RoleLevel.getRoleByUri(roleUri);
    }
    /*
    public RoleLevel getProhibitedFromDeleteBelowRoleLevel() {
        return prohibitedFromDeleteBelowRoleLevel;
    }
    
    public void setProhibitedFromDeleteBelowRoleLevel(RoleLevel eR) {
        prohibitedFromDeleteBelowRoleLevel = eR;
    }
    
    public void setProhibitedFromDeleteBelowRoleLevelUsingRoleUri(String roleUri) {
        prohibitedFromDeleteBelowRoleLevel = BaseResourceBean.RoleLevel.getRoleByUri(roleUri);
    }
    */

	@Override
	public boolean equals(Object obj) {
		if(obj == null ) 
			return false;
		else if (obj instanceof BaseResourceBean ){
			String thisURI = this.getURI();
			String thatURI = ((BaseResourceBean)obj).getURI();
			if( thisURI != null && thatURI != null ){
				return thisURI.equals(thatURI);
			}
		}
		return obj.hashCode() == this.hashCode();			
	}
	
	@Override
	public int hashCode() {
		if( getURI() != null )
			return getURI().hashCode();
		else
			return super.hashCode();
	}

    
}
