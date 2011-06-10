/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.utils;

import java.util.LinkedList;
import java.util.List;

import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vitro.webapp.beans.ResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RoleLevelOptionsSetup {
    private static final Log log = LogFactory.getLog(RoleLevelOptionsSetup.class.getName());
    
    public static List<Option> getDisplayOptionsList(ResourceBean b) {
        List<Option> hiddenFromDisplayList = new LinkedList<Option>();
        try {
            BaseResourceBean.RoleLevel currentLevel = b.getHiddenFromDisplayBelowRoleLevel();
            BaseResourceBean.RoleLevel roles[] = BaseResourceBean.RoleLevel.values();
            boolean someLevelSet=false;
            Option publicOption = null;
            for (BaseResourceBean.RoleLevel level : roles) {
                Option option = new Option (level.getURI(),level.getLabel(),false);
                if (level==BaseResourceBean.RoleLevel.PUBLIC) {
                    publicOption = option;
                }
                if (level==currentLevel) {
                    option.setSelected(true);
                    someLevelSet=true;
                }
                hiddenFromDisplayList.add(option);
            }
            if (!someLevelSet) {
                publicOption.setSelected(true);
            }
        } catch (Exception ex) {
            log.error("cannot create HiddenFromDisplayBelowRoleLevel options");
        }
        return hiddenFromDisplayList;
    }
    
    public static List<Option> getUpdateOptionsList(ResourceBean b) {
        List<Option> prohibitedFromUpdateList = new LinkedList<Option>();
        try {
            BaseResourceBean.RoleLevel currentLevel = b.getProhibitedFromUpdateBelowRoleLevel();
            BaseResourceBean.RoleLevel roles[] = BaseResourceBean.RoleLevel.values();
            boolean someLevelSet=false;
            Option publicOption = null;
            for (BaseResourceBean.RoleLevel level : roles) {
                Option option = new Option (level.getURI(),level.getLabel(),false);
                if (level==BaseResourceBean.RoleLevel.PUBLIC) {
                    publicOption = option;
                }
                if (level==currentLevel) {
                    option.setSelected(true);
                    someLevelSet=true;
                }
                prohibitedFromUpdateList.add(option);
            }
            if (!someLevelSet) {
                publicOption.setSelected(true);
            }
        } catch (Exception ex) {
            log.error("cannot create ProhibitedFromUpdateBelowRoleLevel options");
        }
        return prohibitedFromUpdateList;
    }
}
