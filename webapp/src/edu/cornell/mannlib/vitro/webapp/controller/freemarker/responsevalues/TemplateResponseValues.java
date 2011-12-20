/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TemplateResponseValues extends BaseResponseValues {

    private final String templateName;
    private final Map<String, Object> map;
    
    public TemplateResponseValues(String templateName) {
        this.templateName = templateName;
        this.map = new HashMap<String, Object>();
    }

    public TemplateResponseValues(String templateName, int statusCode) {
        super(statusCode);
        this.templateName = templateName;
        this.map = new HashMap<String, Object>();
    }
    
    public TemplateResponseValues(String templateName, Map<String, Object> map) {
        this.templateName = templateName;
        this.map = map;
    }

    public TemplateResponseValues(String templateName, Map<String, Object> map, int statusCode) {
        super(statusCode);
        this.templateName = templateName;
        this.map = map;
    }
    
    public TemplateResponseValues put(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

    @Override
    public Map<String, Object> getMap() {
        return Collections.unmodifiableMap(this.map);
    }

    @Override
    public String getTemplateName() {
        return this.templateName;
    }   

}

