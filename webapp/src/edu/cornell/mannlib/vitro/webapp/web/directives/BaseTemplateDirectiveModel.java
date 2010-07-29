/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHelper;
import freemarker.template.Configuration;
import freemarker.template.TemplateDirectiveModel;

public abstract class BaseTemplateDirectiveModel implements TemplateDirectiveModel {
    
    public String help(Configuration config) {
        Map<String, Object> map = new HashMap<String, Object>();
        
        String name = getDirectiveName();
        map.put("name", name);
        
        return mergeToTemplate(map, config);
    }
    
    protected String getDirectiveName() {
        String className = this.getClass().getName();
        String[] nameParts = className.split("\\.");
        String directiveName = nameParts[nameParts.length-1];
        directiveName = directiveName.replaceAll("Directive$", "");
        directiveName = directiveName.substring(0, 1).toLowerCase() + directiveName.substring(1);
        return directiveName;               
    }
    
    protected String mergeToTemplate(Map<String, Object> map, Configuration config) {
        return new FreemarkerHelper().mergeMapToTemplate("directive-help.ftl", map, config); 
    }

}
