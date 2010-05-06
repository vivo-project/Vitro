/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.view.menu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class MenuItem {

    private static final Log log = LogFactory.getLog(MenuItem.class.getName());
    
    private String text;
    private String url;
    
    public MenuItem(String linkText, String url) {
        text = linkText;
        this.url = url;
    }
    
    public String getLinkText() {
        return text; 
    }
    
    public String getUrl() {
        return url;
    }
    
}
