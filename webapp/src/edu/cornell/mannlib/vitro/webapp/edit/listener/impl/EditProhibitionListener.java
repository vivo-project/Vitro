package edu.cornell.mannlib.vitro.webapp.edit.listener.impl;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.listener.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.auth.policy.setup.CuratorEditingPolicySetup;
import edu.cornell.mannlib.vitro.webapp.auth.policy.setup.DbAdminEditingPolicySetup;
import edu.cornell.mannlib.vitro.webapp.auth.policy.setup.EditorEditingPolicySetup;
import edu.cornell.mannlib.vitro.webapp.auth.policy.setup.SelfEditingPolicySetup;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.Property;

public class EditProhibitionListener implements ChangeListener {
    private static final Log log = LogFactory.getLog(EditProhibitionListener.class.getName());
    private ServletContext context = null;
    
    public EditProhibitionListener(ServletContext context) {
        this.context = context;
    }

    public void doDeleted(Object oldObj, EditProcessObject epo) {
        Property p = (Property) oldObj;
        Model model = (Model) context.getAttribute("jenaOntModel");
        BaseResourceBean.RoleLevel oldRoleLevel = p.getProhibitedFromUpdateBelowRoleLevel();
        if (oldRoleLevel != null) {
            log.debug("replacing all edit prohibition policies after deletion");
            // do you want to do something more selective, such as seeing whether only certain policies are affected?
            // But, some (lower) will be affected if higher levels change (or will they if the object has been deleted?)
            SelfEditingPolicySetup.replaceSelfEditing(context,model);
            EditorEditingPolicySetup.replaceEditorEditing(context,model);
            CuratorEditingPolicySetup.replaceCuratorEditing(context,model);
            DbAdminEditingPolicySetup.replaceDbAdminEditing(context,model);
            /*
            if (oldRoleLevel.compareTo(BaseResourceBean.RoleLevel.PUBLIC)==0) {
                SelfEditingPolicySetup.replaceSelfEditing(context,model);
            } else if (oldRoleLevel.compareTo(BaseResourceBean.RoleLevel.SELF)==0) {
                SelfEditingPolicySetup.replaceSelfEditing(context,model);
            } else if (oldRoleLevel.compareTo(BaseResourceBean.RoleLevel.EDITOR)==0) {
                CuratorEditingPolicySetup.replaceCuratorEditing(context,model);
            } else if (oldRoleLevel.compareTo(BaseResourceBean.RoleLevel.CURATOR)==0) {
                CuratorEditingPolicySetup.replaceCuratorEditing(context,model);
            } else if (oldRoleLevel.compareTo(BaseResourceBean.RoleLevel.DB_ADMIN)==0) {
                CuratorEditingPolicySetup.replaceCuratorEditing(context,model);
            } else if (oldRoleLevel.compareTo(BaseResourceBean.RoleLevel.NOBODY)==0) {
                CuratorEditingPolicySetup.replaceCuratorEditing(context,model);
            } */
        }
    }

    public void doInserted(Object newObj, EditProcessObject epo) {
        Property p = (Property) newObj;
        Model model = (Model) context.getAttribute("jenaOntModel");
        BaseResourceBean.RoleLevel newRoleLevel = p.getProhibitedFromUpdateBelowRoleLevel();
        if (newRoleLevel != null) { // note have to replace even at same level since may have been unspecified
            if (newRoleLevel.compareTo(BaseResourceBean.RoleLevel.SELF)==0) {
                log.debug("replacing self editing editing policies after insertion of \"self\" update level");
                SelfEditingPolicySetup.replaceSelfEditing(context,model);
            } else if (newRoleLevel.compareTo(BaseResourceBean.RoleLevel.EDITOR)==0) {
                log.debug("replacing editor and lower editing policies after insertion of new \"editor\" update level");
                SelfEditingPolicySetup.replaceSelfEditing(context,model);
                EditorEditingPolicySetup.replaceEditorEditing(context,model);
            } else if (newRoleLevel.compareTo(BaseResourceBean.RoleLevel.CURATOR)==0) {
                log.debug("replacing curator and lower editing policies after insertion of new \"curator\" update level");
                SelfEditingPolicySetup.replaceSelfEditing(context,model);
                EditorEditingPolicySetup.replaceEditorEditing(context,model);
                CuratorEditingPolicySetup.replaceCuratorEditing(context,model);
            } else if (newRoleLevel.compareTo(BaseResourceBean.RoleLevel.DB_ADMIN)==0) {
                log.debug("replacing db_admin and lower editing policies after insertion of new \"db_admin\" update level");
                SelfEditingPolicySetup.replaceSelfEditing(context,model);
                EditorEditingPolicySetup.replaceEditorEditing(context,model);
                CuratorEditingPolicySetup.replaceCuratorEditing(context,model);
                DbAdminEditingPolicySetup.replaceDbAdminEditing(context,model);
            } else if (newRoleLevel.compareTo(BaseResourceBean.RoleLevel.NOBODY)==0) {
                log.debug("replacing db_admin and lower editing policies after insertion of new \"nobody\" update level");
                SelfEditingPolicySetup.replaceSelfEditing(context,model);
                EditorEditingPolicySetup.replaceEditorEditing(context,model);
                CuratorEditingPolicySetup.replaceCuratorEditing(context,model);
                DbAdminEditingPolicySetup.replaceDbAdminEditing(context,model);
            }
        }
    }

    public void doUpdated(Object oldObj, Object newObj, EditProcessObject epo) {
        Property oldP = (Property) oldObj;
        Property newP = (Property) newObj;
        Model model = (Model) context.getAttribute("jenaOntModel");
        BaseResourceBean.RoleLevel oldRoleLevel = oldP.getProhibitedFromUpdateBelowRoleLevel();
        BaseResourceBean.RoleLevel newRoleLevel = newP.getProhibitedFromUpdateBelowRoleLevel();
        if (newRoleLevel != null) { // will always be true since select box has no non-empty choices
            if (oldRoleLevel != null) {
                if (newRoleLevel.compareTo(oldRoleLevel)!=0) {
                    log.debug("replacing all editing policies after update when new level different from old");
                    SelfEditingPolicySetup.replaceSelfEditing(context,model);
                    EditorEditingPolicySetup.replaceEditorEditing(context,model);
                    CuratorEditingPolicySetup.replaceCuratorEditing(context,model);
                    DbAdminEditingPolicySetup.replaceDbAdminEditing(context,model);
                } else {
                    log.debug("update did not change role level");
                }
            } else {
                log.debug("replacing all editing policies after update when a role level introduced");
                SelfEditingPolicySetup.replaceSelfEditing(context,model);
                EditorEditingPolicySetup.replaceEditorEditing(context,model);
                CuratorEditingPolicySetup.replaceCuratorEditing(context,model);
                DbAdminEditingPolicySetup.replaceDbAdminEditing(context,model);
            }
        } else if (oldRoleLevel != null) { // with fixed selections, not likely to happen
            log.debug("replacing all editing policies after update when old role level removed");
            SelfEditingPolicySetup.replaceSelfEditing(context,model);
            EditorEditingPolicySetup.replaceEditorEditing(context,model);
            CuratorEditingPolicySetup.replaceCuratorEditing(context,model);
            DbAdminEditingPolicySetup.replaceDbAdminEditing(context,model);
        }
    }
}
