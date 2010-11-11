/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.widgets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.TemplateProcessingHelper;
import freemarker.cache.TemplateLoader;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;

public abstract class Widget {

    private static final Log log = LogFactory.getLog(Widget.class);
    
    protected Environment env = null;
    private TemplateProcessingHelper helper = null;
    private String name = null;
 
//    protected TemplateDirectiveModel directive = null;
//    protected Macro markupMacro = null;
//    protected Macro assetsMacro = null;
    
    public Widget(Environment env, String name) {
       this.env = env; 
       this.name = name;
       Configuration config = env.getConfiguration();
       HttpServletRequest request = (HttpServletRequest) env.getCustomAttribute("request");
       ServletContext context = (ServletContext) env.getCustomAttribute("context");
       this.helper = new TemplateProcessingHelper(config, request, context);
       
       //this.directive = directive;
       //Template template = getTemplate();
       //Map templateMacros = template.getMacros();
       //markupMacro = (Macro) templateMacros.get("markup");
       //assetsMacro = (Macro) templateMacros.get("assets");
    }
    
    public String doAssets() {
        String templateName = "widget-" + name + "-assets.ftl";
        
        // Allow the assets template to be absent without generating an error.
        TemplateLoader templateLoader = env.getConfiguration().getTemplateLoader();
        try {
            if ( templateLoader.findTemplateSource(templateName) == null ) {
                return "";
            }
        } catch (IOException e) {
            log.error("Error finding template source", e);
        }
        
        Map<String, Object> map = new HashMap<String, Object>();
        TemplateHashModel dataModel = env.getDataModel();
        
        try {
            map.put("stylesheets", dataModel.get("stylesheets"));
            map.put("scripts", dataModel.get("scripts"));
            map.put("headScripts", dataModel.get("headScripts"));
        } catch (TemplateModelException e) {
            log.error("Error getting asset values from data model.");
        }
        
        return helper.processTemplateToString(templateName, map);        
    }
    
    public String doMarkup() {
        String templateName = "widget-" + name + "-markup.ftl";
        Map<String, Object> map = getDataModel();
        return helper.processTemplateToString(templateName, map);
    }

    

    protected abstract Map<String, Object> getDataModel();
    
//    # You can capture the output of an arbitrary part of the template into a context variable.
//    # You can interpret arbitrary context variable as if it were a template definition.   
}
