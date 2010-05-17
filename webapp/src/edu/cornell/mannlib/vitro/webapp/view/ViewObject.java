/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.view;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.FreeMarkerHttpServlet;

// RY We may want an interface that the superclass would implement.
// RY Consider using FreeMarker's DisplayObjectWrapper instead, or extending it.

public abstract class ViewObject {

    private static final Log log = LogFactory.getLog(ViewObject.class.getName());

    // RY Can probably remove this, since we're using the FreeMarkerHttpServlet methods instead
    public static String contextPath;
    
    protected String getUrl(String path) {
        return FreeMarkerHttpServlet.getUrl(path);
    }
    
    protected String getUrl(String path, Map<String, String> params) {
        return FreeMarkerHttpServlet.getUrl(path, params);
    }
    
    protected String urlEncode(String str) {
        return FreeMarkerHttpServlet.urlEncode(str);
    }

}
