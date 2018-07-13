/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.utils;

import java.util.LinkedList;
import java.util.List;

import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionSets;
import edu.cornell.mannlib.vitro.webapp.beans.ResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO Remove this class?
public class RoleLevelOptionsSetup {
    private static final Log log = LogFactory.getLog(RoleLevelOptionsSetup.class.getName());
    
    public static List<Option> getDisplayOptionsList(ResourceBean b) {
        List<Option> hiddenFromDisplayList = new LinkedList<Option>();
        Option publicOption = new Option("http://vitro.mannlib.cornell.edu/ns/vitro/role#public", "all users, including public");
        publicOption.setSelected(true);
        hiddenFromDisplayList.add(publicOption);
        hiddenFromDisplayList.add(new Option("http://vitro.mannlib.cornell.edu/ns/vitro/role#selfEditor", "self-editor and above"));
        hiddenFromDisplayList.add(new Option("http://vitro.mannlib.cornell.edu/ns/vitro/role#editor", "editor and above"));
        hiddenFromDisplayList.add(new Option("http://vitro.mannlib.cornell.edu/ns/vitro/role#curator", "curator and above"));
        hiddenFromDisplayList.add(new Option("http://vitro.mannlib.cornell.edu/ns/vitro/role#dbAdmin", "site admin and root user"));
        return hiddenFromDisplayList;
    }
    
    public static List<Option> getUpdateOptionsList(ResourceBean b) {
        List<Option> prohibitedFromUpdateList = new LinkedList<Option>();
        Option publicOption = new Option("http://vitro.mannlib.cornell.edu/ns/vitro/role#public", "all users, including public");
        publicOption.setSelected(true);
        prohibitedFromUpdateList.add(publicOption);
        prohibitedFromUpdateList.add(new Option("http://vitro.mannlib.cornell.edu/ns/vitro/role#selfEditor", "self-editor and above"));
        prohibitedFromUpdateList.add(new Option("http://vitro.mannlib.cornell.edu/ns/vitro/role#editor", "editor and above"));
        prohibitedFromUpdateList.add(new Option("http://vitro.mannlib.cornell.edu/ns/vitro/role#curator", "curator and above"));
        prohibitedFromUpdateList.add(new Option("http://vitro.mannlib.cornell.edu/ns/vitro/role#dbAdmin", "site admin and root user"));
        return prohibitedFromUpdateList;
    }

    public static List<Option> getPublishOptionsList(ResourceBean b) {
        List<Option> hiddenFromPublishList = new LinkedList<Option>();
        Option publicOption = new Option("http://vitro.mannlib.cornell.edu/ns/vitro/role#public", "all users, including public");
        publicOption.setSelected(true);
        hiddenFromPublishList.add(publicOption);
        hiddenFromPublishList.add(new Option("http://vitro.mannlib.cornell.edu/ns/vitro/role#selfEditor", "self-editor and above"));
        hiddenFromPublishList.add(new Option("http://vitro.mannlib.cornell.edu/ns/vitro/role#editor", "editor and above"));
        hiddenFromPublishList.add(new Option("http://vitro.mannlib.cornell.edu/ns/vitro/role#curator", "curator and above"));
        hiddenFromPublishList.add(new Option("http://vitro.mannlib.cornell.edu/ns/vitro/role#dbAdmin", "site admin and root user"));
        return hiddenFromPublishList;
    }
    

}
