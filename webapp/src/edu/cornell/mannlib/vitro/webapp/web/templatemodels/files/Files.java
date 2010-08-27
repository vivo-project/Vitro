/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.files;

import java.util.LinkedHashSet;

import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public abstract class Files extends BaseTemplateModel {
    
    protected LinkedHashSet<String> list = null;
    private String themeDir = null;
    
    public Files() {
        this.list = new LinkedHashSet<String>();
    }
    
    public Files(String themeDir) {
        this();
        this.themeDir = themeDir;
    }
    
    public Files(LinkedHashSet<String> list) {
        this.list = list;
    }
    
    public void add(String path) {
        list.add(getUrl(path));
    }
    
    public void add(String... paths) {
        for (String path : paths) {
            add(path);
        }
    }
    
    public void addFromTheme(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        path = themeDir + getThemeSubDir() + path;
        add(path);
    }
    
    public void addFromTheme(String... paths) {
        for (String path : paths) {
            addFromTheme(path);
        }
    }
    
    public String getTags() {
        String tags = "";
      
        for (String file : list) {
            tags += getTag(file);
        }
        return tags;
    }
    
    public String dump() {
        return list.toString();
    }
    
    protected abstract String getThemeSubDir();
    protected abstract String getTag(String url);
    
}
