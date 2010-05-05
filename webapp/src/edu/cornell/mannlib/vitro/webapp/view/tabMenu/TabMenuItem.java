/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.view.tabMenu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.view.ViewObject;

// RY probably shouldn't subclass ViewObject - doesn't really use anything
public class TabMenuItem extends ViewObject {
    
    private static final Log log = LogFactory.getLog(TabMenuItem.class.getName());

    private String linkText;
    private String url;
    private boolean active = false;
    
    public TabMenuItem(String linkText, String path, VitroRequest vreq) {
        this.linkText = linkText;
        url = vreq.getContextPath() + "/" + path;
        active = vreq.getServletPath().equals("/" + path);
    }
    
    public String getLinkText() {
        return linkText; 
    }
    
    public String getUrl() {
        return url;
    }
    
    public boolean isActive() {
        return active;
    }
}
