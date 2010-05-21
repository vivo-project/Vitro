/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreeMarkerHttpServlet;

// RY We may want an interface that the superclass would implement.
// RY Consider using FreeMarker's object wrappers instead.

public abstract class ViewObject {

    private static final Log log = LogFactory.getLog(ViewObject.class.getName());

    // RY Can probably remove this, since we're using the FreeMarkerHttpServlet methods instead
    // public static String contextPath;
    
    protected String getUrl(String path) {
        return FreeMarkerHttpServlet.getUrl(path);
    }
    
    protected String getUrl(String path, Map<String, String> params) {
        return FreeMarkerHttpServlet.getUrl(path, params);
    }
    
    protected String urlEncode(String str) {
        return FreeMarkerHttpServlet.urlEncode(str);
    }
    
    /*
     * public static List<?> wrapList(List<?> list, Class cl) 
     * throw error if cl not a child of ViewObject
     * This block of code is going to be repeated a lot:
            List<VClassGroup> groups = // code to get the data
            List<VClassGroupView> vcgroups = new ArrayList<VClassGroupView>(groups.size());
            Iterator<VClassGroup> i = groups.iterator();
            while (i.hasNext()) {
                vcgroups.add(new VClassGroupView(i.next()));
            }
            body.put("classGroups", vcgroups);
    Can we generalize it to a generic method of ViewObject - wrapList() ? 
    static method of ViewObject
    Params: groups, VClassGroupView (the name of the class) - but must be a child of ViewObject
    Return: List<viewObjectType>
     */

}
