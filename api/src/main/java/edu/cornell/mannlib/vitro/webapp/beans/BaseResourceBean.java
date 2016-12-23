/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionSets;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class BaseResourceBean implements ResourceBean {
	
	private static final Log log = LogFactory.getLog(BaseResourceBean.class.getName());
    protected String URI          = null;
    protected String namespace    = null;
    protected String localName    = null;
    protected String localNameWithPrefix = null;
    protected String pickListName = null;
    
    protected RoleLevel hiddenFromDisplayBelowRoleLevel = null;
    protected RoleLevel prohibitedFromUpdateBelowRoleLevel = null;
    protected RoleLevel hiddenFromPublishBelowRoleLevel = null;
    
	public enum RoleLevel {
		PUBLIC("http://vitro.mannlib.cornell.edu/ns/vitro/role#public",
				"all users, including public", "all users who can log in",
				"public"),

		SELF("http://vitro.mannlib.cornell.edu/ns/vitro/role#selfEditor",
				"self-editor and above", "self-editor and above", "self"),

		EDITOR("http://vitro.mannlib.cornell.edu/ns/vitro/role#editor",
				"editor and above", "editor and above", "editor"),

		CURATOR("http://vitro.mannlib.cornell.edu/ns/vitro/role#curator",
				"curator and above", "curator and above", "curator"),

		DB_ADMIN("http://vitro.mannlib.cornell.edu/ns/vitro/role#dbAdmin",
				"site admin and root user", "site admin and root user",
				"siteAdmin"),

		NOBODY("http://vitro.mannlib.cornell.edu/ns/vitro/role#nobody",
				"root user", "root user", "root");

		private final String uri;
		private final String displayLabel;
		private final String updateLabel;
		private final String shorthand;

		private RoleLevel(String uri, String displayLabel, String updateLabel,
				String shorthand) {
			this.uri = uri;
			this.displayLabel = displayLabel;
			this.updateLabel = updateLabel;
			this.shorthand = shorthand;
		}

		public String getURI() {
			return uri;
		}

		public String getDisplayLabel() {
			return displayLabel;
		}

		public String getUpdateLabel() {
			return updateLabel;
		}

		public String getShorthand() {
			return shorthand;
		}
		
		// Never returns null.
		public static RoleLevel getRoleByUri(String uri2) {
			if (uri2 == null)
				return RoleLevel.values()[0];

			for (RoleLevel role : RoleLevel.values()) {
				if (role.uri.equals(uri2))
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
			if (roles.contains(PermissionSets.URI_DBA)) {
				return DB_ADMIN;
			} else if (roles.contains(PermissionSets.URI_CURATOR)) {
				return CURATOR;
			} else if (roles.contains(PermissionSets.URI_EDITOR)) {
				return EDITOR;
			} else if (roles.contains(PermissionSets.URI_SELF_EDITOR)) {
				return SELF;
			} else {
				// Logged in but with no recognized role? Make them SELF
				return SELF;
			}
		}
	}

	public BaseResourceBean() {
	    // default constructor
	}
	
	public BaseResourceBean(String uri) {
	    buildLocalAndNS(uri);
	}
	
    @Override
	public boolean isAnonymous() {        
    	return (this.URI==null || VitroVocabulary.PSEUDO_BNODE_NS.equals(this.getNamespace()));
    }
    
    @Override
	public String getURI() {
        return URI;
    }
    @Override
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
    
    @Override
	public String getNamespace() {
        if( namespace == null && this.URI != null)
            buildLocalAndNS(this.URI);        
        return namespace;
    }
    @Override
	public void setNamespace(String namespace) {
        this.namespace = namespace;
        if (namespace != null && localName != null ) {
            this.URI = namespace + localName;
        }
    }
    
    @Override
	public String getLabel() {
        return getLocalName();
    }

    @Override
	public String getLocalName() {
        if( localName == null && this.URI != null)
            buildLocalAndNS(this.URI);        
        return localName;
    }
    
    @Override
	public void setLocalName(String localName) {
        this.localName = localName;
        if (namespace != null && localName != null) {
            this.URI = namespace + localName;
        }
    }

    public String getLocalNameWithPrefix() {
        return localNameWithPrefix != null ? localNameWithPrefix : 
                    getLocalName() != null ?  getLocalName() :
                        URI != null ? URI : "(no name)" ;
    }
    public void setLocalNameWithPrefix(String prefixedLocalName) {
        this.localNameWithPrefix = prefixedLocalName;
    }
    
    @Override
	public String getPickListName() {
        return pickListName==null ? getLocalName()==null ? 
                (URI==null ? "(no name)" : URI ): getLocalName() : pickListName;
    }
    public void setPickListName(String pickListName) {
        this.pickListName = pickListName;
    }
    
    @Override
	public RoleLevel getHiddenFromDisplayBelowRoleLevel() {
        return hiddenFromDisplayBelowRoleLevel;
    }
    
    @Override
	public void setHiddenFromDisplayBelowRoleLevel(RoleLevel level) {
        hiddenFromDisplayBelowRoleLevel = level;
    }
    
    @Override
	public void setHiddenFromDisplayBelowRoleLevelUsingRoleUri(String roleUri) {
        hiddenFromDisplayBelowRoleLevel = RoleLevel.getRoleByUri(roleUri);
    }

    @Override
	public RoleLevel getProhibitedFromUpdateBelowRoleLevel() {
        return prohibitedFromUpdateBelowRoleLevel;
    }
    
    @Override
	public void setProhibitedFromUpdateBelowRoleLevel(RoleLevel level) {
        prohibitedFromUpdateBelowRoleLevel = level;
    }
    
    @Override
	public void setProhibitedFromUpdateBelowRoleLevelUsingRoleUri(String roleUri) {
        prohibitedFromUpdateBelowRoleLevel = RoleLevel.getRoleByUri(roleUri);
    }

	@Override
	public RoleLevel getHiddenFromPublishBelowRoleLevel() {
        return hiddenFromPublishBelowRoleLevel;
	}
    
    @Override
	public void setHiddenFromPublishBelowRoleLevel(RoleLevel level) {
        hiddenFromPublishBelowRoleLevel = level;
    }
    
    @Override
	public void setHiddenFromPublishBelowRoleLevelUsingRoleUri(String roleUri) {
    	hiddenFromPublishBelowRoleLevel = BaseResourceBean.RoleLevel.getRoleByUri(roleUri);
    }
    
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
