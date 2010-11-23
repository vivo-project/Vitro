/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.TemplateProcessingHelper;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;

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
    
    protected String mergeToHelpTemplate(Map<String, Object> map, Environment env) {
        return processTemplateToString("help-directive.ftl", map, env);        
    }
    
    public static String processTemplateToString(String templateName, Map<String, Object> map, Environment env) {
        Template template = getTemplate(templateName, env);
        StringWriter sw = new StringWriter();
        try {
            template.process(map, sw);
        } catch (TemplateException e) {
            log.error("Template Exception creating processing environment", e);
        } catch (IOException e) {
            log.error("IOException creating processing environment", e);
        }
        return sw.toString();        
    }
    
    private static Template getTemplate(String templateName, Environment env) {
        Template template = null;
        try {
            template = env.getConfiguration().getTemplate(templateName);
        } catch (IOException e) {
            // RY Should probably throw this error instead.
            log.error("Cannot get template " + templateName, e);
        }  
        return template;        
    }
    
}
