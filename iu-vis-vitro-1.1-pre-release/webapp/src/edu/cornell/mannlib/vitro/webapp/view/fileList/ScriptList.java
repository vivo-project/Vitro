/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.view.fileList;

public class ScriptList extends FileList {
    
    protected static final String THEME_SUBDIR = "/js";

    public ScriptList() { }
    
    public ScriptList(String themeDir) {
        super(themeDir);
    }

    protected String getTag(String url) {
        return "<script type=\"text/javascript\" src=\"" + url + "\"></script>";
    }

    protected String getThemeSubDir() {
        return THEME_SUBDIR;
    }
}
