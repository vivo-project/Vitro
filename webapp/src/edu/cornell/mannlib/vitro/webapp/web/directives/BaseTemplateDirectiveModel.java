/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.TemplateProcessingHelper;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.TemplateDirectiveModel;

public abstract class BaseTemplateDirectiveModel implements TemplateDirectiveModel {

    private static final Log log = LogFactory.getLog(BaseTemplateDirectiveModel.class);
    
    public String help(Environment environment) {
        Map<String, Object> map = new HashMap<String, Object>();
        
        String name = getDirectiveName();
        map.put("name", name);
        
        return mergeToHelpTemplate(map, environment);
    }
    
    protected String getDirectiveName() {
        String className = this.getClass().getName();
        String[] nameParts = className.split("\\.");
        String directiveName = nameParts[nameParts.length-1];
        directiveName = directiveName.replaceAll("Directive$", "");
        directiveName = directiveName.substring(0, 1).toLowerCase() + directiveName.substring(1);
        return directiveName;               
    }
    
    protected String mergeToHelpTemplate(Map<String, Object> map, Environment environment) {
        TemplateProcessingHelper helper = getFreemarkerHelper(environment);
        return helper.processTemplateToString("help-directive.ftl", map); 
    }
    
    public static TemplateProcessingHelper getFreemarkerHelper(Environment env) {
        Configuration config = env.getConfiguration();
        // In a directive, custom attributes for request and context are available in the Environment.
        // They are put there when the enclosing template is processed.
        HttpServletRequest request = (HttpServletRequest) env.getCustomAttribute("request");
        ServletContext context = (ServletContext) env.getCustomAttribute("context");
        return new TemplateProcessingHelper(config, request, context);
    }

}
