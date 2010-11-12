/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.widgets;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.cache.TemplateLoader;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;

public abstract class Widget {

    private static final Log log = LogFactory.getLog(Widget.class);
    
    /* Widget implementations don't get any state when they get constructed so that they 
     * can be reused. */
    public Widget() { }
    
    public String doAssets(Environment env, Map params) {
        String widgetName = params.get("name").toString(); //getWidgetName();
        String templateName = getAssetsTemplateName(widgetName);

        // Allow the assets template to be absent without generating an error.
        TemplateLoader templateLoader = env.getConfiguration().getTemplateLoader();
        try {
            if ( templateLoader.findTemplateSource(templateName) == null ) {
                return "";
            }
        } catch (IOException e) {
            log.error("Error finding template source", e);
        }
        
        TemplateHashModel dataModel = env.getDataModel();  
        Map<String, Object> map = new HashMap<String, Object>(); 
        
        try {
            map.put("stylesheets", dataModel.get("stylesheets"));
            map.put("scripts", dataModel.get("scripts"));
            map.put("headScripts", dataModel.get("headScripts"));
        } catch (TemplateModelException e) {
            log.error("Error getting asset values from data model.");
        }
        
        return processTemplateToString(widgetName, env, templateName, map);
 
    }
    
    public String doMarkup(Environment env, Map params) {
        HttpServletRequest request = (HttpServletRequest) env.getCustomAttribute("request");
        ServletContext context = (ServletContext) env.getCustomAttribute("context");
        String widgetName = params.get("name").toString(); // getWidgetName();
        WidgetTemplateValues values = process(env, params, widgetName, request, context);        
        return processTemplateToString(widgetName, env, values);
    }

    // Default assets template name. Can be overridden by subclasses.
    protected String getAssetsTemplateName(String widgetName) {
        return "widget-" + widgetName + "-assets.ftl";
    }
  
    // Default markup template name. Can be overridden in subclasses, or assigned
    // differently in the subclass process() method. For example, LoginWidget will
    // select a template according to login processing status.
    protected String getMarkupTemplateName(String widgetName) {
        return "widget-" + widgetName + "-markup.ftl";
    }
    
//    private String getWidgetName() {
//        String name = this.getClass().getName();
//        name= name.replaceAll(".*\\.", "");
//        name = name.replaceAll("Widget$", "");
//        name = name.substring(0, 1).toLowerCase() + name.substring(1);
//        return name;
//    }

    protected abstract WidgetTemplateValues process(Environment env, Map params, String widgetName, HttpServletRequest request, ServletContext context);
    
    private String processTemplateToString(String widgetName, Environment env, String templateName, Map<String, Object> map) {
        StringWriter out = new StringWriter();
        Configuration config = env.getConfiguration();
        try {
            Template template = config.getTemplate(templateName);
            template.process(map, out);
        } catch (Throwable th) {
            log.error("Could not process widget " + widgetName, th);
        }
        return out.toString();        
    }
    
    private String processTemplateToString(String widgetName, Environment env, WidgetTemplateValues values) {
        return processTemplateToString(widgetName, env, values.getTemplateName(), values.getMap());
    }
   
    
    protected static class WidgetTemplateValues {
        private final String templateName;
        private final Map<String, Object> map;
        
        public WidgetTemplateValues(String templateName, Map<String, Object> map) {
            this.templateName = templateName;
            this.map = map;
        }

        public WidgetTemplateValues put(String key, Object value) {
            this.map.put(key, value);
            return this;
        }

        public Map<String, Object> getMap() {
            return Collections.unmodifiableMap(this.map);
        }

        public String getTemplateName() {
            return this.templateName;
        }
 
    }

}
