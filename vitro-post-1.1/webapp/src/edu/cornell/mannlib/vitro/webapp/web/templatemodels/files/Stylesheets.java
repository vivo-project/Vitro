/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.files;

public class Stylesheets extends Files {
    
    public Stylesheets() { }
    
    public Stylesheets(String themeDir) {
        super(themeDir);
    }
    
    protected String getTag(String url) {
        return "<link rel=\"stylesheet\" href=\"" + url + "\" />\n";
    }

}
