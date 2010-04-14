/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;

/**
 * User: bdc34
 * Date: Oct 18, 2007
 * Time: 3:41:23 PM
 */
public interface ResourceBean {
    String getURI();
    
    boolean isAnonymous();

    void setURI(String URI);

    String getNamespace();

    void setNamespace(String namespace);

    String getLocalName();

    void setLocalName(String localName);
    
    public RoleLevel getHiddenFromDisplayBelowRoleLevel() ;
    
    public void setHiddenFromDisplayBelowRoleLevel(RoleLevel eR) ;
    
    public void setHiddenFromDisplayBelowRoleLevelUsingRoleUri(String roleUri) ;
    /*
    public RoleLevel getProhibitedFromCreateBelowRoleLevel() ;
    
    public void setProhibitedFromCreateBelowRoleLevel(RoleLevel eR) ;
    
    public void setProhibitedFromCreateBelowRoleLevelUsingRoleUri(String roleUri) ;
    */
    public RoleLevel getProhibitedFromUpdateBelowRoleLevel() ;
    
    public void setProhibitedFromUpdateBelowRoleLevel(RoleLevel eR) ;
    
    public void setProhibitedFromUpdateBelowRoleLevelUsingRoleUri(String roleUri) ;
    /*
    public RoleLevel getProhibitedFromDeleteBelowRoleLevel() ;
    
    public void setProhibitedFromDeleteBelowRoleLevel(RoleLevel eR) ;
    
    public void setProhibitedFromDeleteBelowRoleLevelUsingRoleUri(String roleUri) ;
    */
}
