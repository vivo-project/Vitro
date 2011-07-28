/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels;

import java.util.LinkedHashSet;

public class Tags extends BaseTemplateModel {
    
    protected final LinkedHashSet<String> tags;

    public Tags() {
        this.tags = new LinkedHashSet<String>();
    }
    
    public Tags(LinkedHashSet<String> tags) {
        this.tags = tags;
    }

    public void add(String... tags) {
        for (String tag : tags) {
            add(tag);
        }
    }
    
    public void add(String tag) {
        tags.add(tag);
    }
    
    public String getList() {
        String tagList = "";
        
        for (String tag : tags) {
            tagList += tag;
        }
        return tagList;
    }
    
}
