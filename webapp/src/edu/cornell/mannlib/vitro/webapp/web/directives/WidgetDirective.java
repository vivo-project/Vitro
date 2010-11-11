/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
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
            // Use Constructor.newInstance() rather than Class.newInstance() so we can pass arguments 
            // to the constructor.
            // Widget widget = (Widget) widgetClass.newInstance();
            Constructor<?> widgetConstructor = widgetClass.getConstructor(new Class[]{Environment.class, String.class});
            Widget widget = (Widget) widgetConstructor.newInstance(env, widgetName);              
            Method method = widgetClass.getMethod(methodName);
            
            // Right now it seems to me that we will always be producing a string for the widget calls. If we need greater
            // flexibility, we can return a ResponseValues object and deal with different types here.
            String output = (String) method.invoke(widget);
            
            String templateType = env.getDataModel().get("templateType").toString();
            // If we're in the body template, automatically invoke the doAssets() method, so it
            // doesn't need to be called explicitly.
            if ("doMarkup".equals(methodName) && FreemarkerHttpServlet.BODY_TEMPLATE_TYPE.equals(templateType)) {
                output += widgetClass.getMethod("doAssets").invoke(widget);
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
    

}
