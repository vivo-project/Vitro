/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.files;

public class Scripts extends Files {

    public Scripts() { }
    
    public Scripts(String themeDir) {
        super(themeDir);
    }

    protected String getTag(String url) {
        return "<script type=\"text/javascript\" src=\"" + url + "\"></script>\n";
    }
}
