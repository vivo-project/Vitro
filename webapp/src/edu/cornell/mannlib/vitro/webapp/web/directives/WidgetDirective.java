/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.web.widgets.Widget;
import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class WidgetDirective extends BaseTemplateDirectiveModel {

    private static final Log log = LogFactory.getLog(WidgetDirective.class);
    private static final String WIDGET_PACKAGE = "edu.cornell.mannlib.vitro.webapp.web.widgets";
    
    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
            TemplateDirectiveBody body) throws TemplateException, IOException {

        if (loopVars.length != 0) {
            throw new TemplateModelException(
                "The dump directive doesn't allow loop variables.");
        }
        if (body != null) {
            throw new TemplateModelException(
                "The dump directive doesn't allow nested content.");
        }
        
        Object nameParam = params.get("name");
        if ( !(nameParam instanceof SimpleScalar)) {
            throw new TemplateModelException(
               "Value of parameter 'name' must be a string.");     
        }
        String widgetName = nameParam.toString();

        // Optional param
        Object includeParam = params.get("include");
        String methodName;
        // If include param is missing, or something other than "assets", 
        // assign default value "markup"
        if (includeParam == null) {
            methodName = "markup";
        } else {
            methodName = includeParam.toString();
            if ( ! ("assets".equals(methodName)) ) {
                methodName = "markup";
            }
        }
        methodName = "do" + StringUtils.capitalize(methodName);
        
        try {       
            String widgetClassName = WIDGET_PACKAGE + "." + StringUtils.capitalize(widgetName) + "Widget";
            Class<?> widgetClass = Class.forName(widgetClassName); 
            Widget widget = (Widget) widgetClass.newInstance();             
            Method method = widgetClass.getMethod(methodName, Environment.class, Map.class);
            
            // Right now it seems to me that we will always be producing a string for the widget calls. If we need greater
            // flexibility, we can return a ResponseValues object and deal with different types here.
            String output = (String) method.invoke(widget, env, params);

            // If we're in the body template, automatically invoke the doAssets() method, so it
            // doesn't need to be called explicitly from the enclosing template.
            String templateType = env.getDataModel().get("templateType").toString();
            if ("doMarkup".equals(methodName) && FreemarkerHttpServlet.BODY_TEMPLATE_TYPE.equals(templateType)) {
                output += widgetClass.getMethod("doAssets", Environment.class, Map.class).invoke(widget, env, params);
            }
              
            Writer out = env.getOut();
            out.write(output);
            
        } catch  (ClassNotFoundException e) {
            log.error("Widget " + widgetName + " not found.");
        } catch (IOException e) {
                log.error("Error writing output for widget " + widgetName, e);  
        } catch (Exception e) {
            log.error("Error invoking widget " + widgetName, e);
        }
        
    }

    @Override
    public Map<String, Object> help(String name) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        map.put("effect", "Add a reuseable block of markup and functionality to the template, with associated scripts and stylesheets injected into the page &lt;head&gt; element.");
        
        map.put("comments", "From a body template, insert widget directive in desired location with no include value or include=\"markup\". Both assets and markup will be included. " +
                            "From a page template, insert widget directive at top of template with include=\"assets\". Insert widget directive in desired location " +
                            "with no include value or include=\"markup\".");
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", "name of widget");
        params.put("include", "values: \"assets\" to include scripts and stylesheets associated with the widget; \"markup\" to include the markup. " +
                              "\"markup\" is default value, so does not need to be specified.");
        map.put("parameters", params);
        
        List<String> examples = new ArrayList<String>();
        examples.add("<@" + name + " name=\"login\" /> (use in body and page templates where markup should be inserted)");
        examples.add("<@" + name + " name=\"login\" include=\"markup\" /> (same as example 1)");
        examples.add("<@" + name + " name=\"login\" include=\"assets\" /> (use at top of page template to get scripts and stylesheets inserted into the &lt;head&gt; element)");
        
        map.put("examples", examples);
        
        return map;
    }

}
