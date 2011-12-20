/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.menu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class MenuItem extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(MenuItem.class.getName());
    
    private final String text;
    private final String path;
    private boolean active;
    
    public MenuItem(String linkText, String path) {
        text = linkText;
        this.path = path;
    }
    
    public MenuItem(String linkText, String path, boolean active){
        this.text = linkText;
        this.path = path;
        this.active = active;
    }
    
    /* Template properties */
    
    public String getLinkText() {
        return text; 
    }
    
    public String getUrl() {
        return getUrl(path);
    } 
    
    public boolean getActive(){
        return active;
    }
}
