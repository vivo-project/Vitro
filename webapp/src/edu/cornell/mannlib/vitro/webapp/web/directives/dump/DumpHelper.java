/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives.dump;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.web.directives.BaseTemplateDirectiveModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;
import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.WrapperExtractor;
import freemarker.template.Configuration;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;
import freemarker.template.utility.DeepUnwrap;

public class DumpHelper {

    private static final Log log = LogFactory.getLog(DumpHelper.class);

    private Environment env = null;
    
    public DumpHelper(Environment env) {
        this.env = env;
    }   

    public String getVariableDump(String varName) {
        Map<String, Object> map = getVariableDumpData(varName);
        return BaseTemplateDirectiveModel.processTemplateToString("dump-var.ftl", map, env);
    }

    private Map<String, Object> getVariableDumpData(String varName) {
        TemplateHashModel dataModel = env.getDataModel();

        TemplateModel wrappedModel =  null;
        try {
            wrappedModel = dataModel.get(varName);
        } catch (TemplateModelException e) {
            log.error("Error getting value of template model '" + varName + "' from data model.");
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("var", varName);
        
        // RY Separate out here into a method that gets the rest of the map. This method will
        // be called recursively on the result of invoking a method (for a BaseTemplateModel) or
        // the members of a hash or sequence. See NewDumpHelper.getVariableDumpData().
        
        // DON'T return null if wrappedModel == null. We still want to return the map to the template.
        if (wrappedModel != null) {   
            Object unwrappedModel = null;
            try {
                unwrappedModel = DeepUnwrap.permissiveUnwrap(wrappedModel);
            } catch (TemplateModelException e) {
                log.error("Cannot unwrap template model  " + varName + ".");
                return null;
            }
            
            // Just use toString() method for now. Handles nested collections. Could make more sophisticated later.
            // wrappedModel.toString() gives wrong results in the case of, e.g., a boolean value in a hash. tm.toString() may
            // return a TemplateBooleanModel object, while unwrappedModel.toString() returns "true" or "false."
            String value = unwrappedModel.toString(); // wrappedModel.toString();
            String className = unwrappedModel.getClass().getName();
            String type = null;
            
            // For basic Java types such as string, date, boolean, it's most helpful for the dump to
            // show the shorthand type assigned below, rather than the Java class name. But for our
            // BaseTemplateModel objects, show the actual class, since that provides additional
            // information about the object (available methods, for example) that it is helpful to
            // view in the dump. Not sure if we should handle our application-specific, non-template
            // model objects in the same way. For now, these get assigned a shorthand type below.
            
            // if (wrappedModel instanceof TemplateHashModelEx) - 
            // a subcase of this is unwrappedModel instanceof BaseTemplateModel
            if (unwrappedModel instanceof BaseTemplateModel) {
                map.putAll(getBaseTemplateModelValues(wrappedModel, (BaseTemplateModel)unwrappedModel)); 
                type = className;
            // Do TemplateHashModel case first, because wrappedModel of (at least some) POJOs are
            // StringModels, which are both TemplateScalarModels and TemplateHashModels
            } else if (wrappedModel instanceof TemplateHashModel) {
                type = "Hash";
            } else if (wrappedModel instanceof TemplateScalarModel) {
                type = "String";
            } else if (wrappedModel instanceof TemplateDateModel) { 
                type = "Date";
            } else if (wrappedModel instanceof TemplateNumberModel) {
                type = "Number";
            } else if (wrappedModel instanceof TemplateBooleanModel) {
                type = "Boolean";
                try {
                    value = ((TemplateBooleanModel) wrappedModel).getAsBoolean() ? "true" : "false";
                } catch (TemplateModelException e) {
                    log.error("Error getting boolean value for " + varName + ".");
                }
            } else if (wrappedModel instanceof TemplateSequenceModel){
                type = "Sequence";

            } else {
                // One of the above cases should have applied. Just in case not, show the Java class name.
                type = className;
            }
            
            map.put("type", type);
            
            // Don't overwrite value returned from getTemplateModelValues().
            if (! map.containsKey("value")) {
                map.put("value", value);
            }           
            
        }

        return map;
    }

    private int getExposureLevel(TemplateModel model, BaseTemplateModel unwrappedModel) {
        
        int exposureLevel;
        // Get the exposure level of the BeansWrapper that wrapped this object.
        if (model instanceof BeanModel) {
            exposureLevel = WrapperExtractor.getWrapperExposureLevel((BeanModel) model);
            log.debug("Exposure level for class " + unwrappedModel.getClass().getCanonicalName() + " wrapped as " + model.getClass() + " = " + exposureLevel);
        // We don't expect to get here, since we are dealing only with BaseTemplateModel objects, which get wrapped into BeanModel objects, 
        // but it's here as a safety net.
        } else {
            HttpServletRequest request = (HttpServletRequest) env.getCustomAttribute("request");
            Configuration config = (Configuration) request.getAttribute("freemarkerConfig");
            BeansWrapper wrapper = (BeansWrapper) config.getObjectWrapper();
            exposureLevel = WrapperExtractor.getWrapperExposureLevel(wrapper);
            log.debug("Class " + unwrappedModel.getClass().getCanonicalName() + " wrapped as " + model.getClass() + " uses default exposure level " + exposureLevel);
        }
        
        return exposureLevel;
    }
    
    List<Method> getMethodsAvailableToTemplate(TemplateModel wrappedModel, BaseTemplateModel unwrappedModel) {
        int exposureLevel = getExposureLevel(wrappedModel, unwrappedModel);
        return getMethodsAvailableToTemplate(exposureLevel, unwrappedModel);
    }
    
    private List<Method> getMethodsAvailableToTemplate(int exposureLevel, BaseTemplateModel unwrappedModel) {
        List<Method> availableMethods = new ArrayList<Method>();
        
        Class<?> cls = unwrappedModel.getClass();
        Method[] classMethods = cls.getMethods(); 
        for (Method method : classMethods) {

            // Exclude static methods
            int mod = method.getModifiers();            
            if (Modifier.isStatic(mod)) {
                continue;
            }

            // Include only methods declared on BaseTemplateModel or a subclass;
            // exclude methods inherited from higher up the hierarchy like
            // toString(), getClass(), etc.
            Class<?> c = method.getDeclaringClass();
            if ( ! BaseTemplateModel.class.isAssignableFrom(c)) { 
                continue;
            }

            // If the method takes arguments, then it is not available to the template unless
            // the exposure level of the BeansWrapper that wrapped the object allows it. 
            Class<?>[] params = method.getParameterTypes();
            if (params.length > 0 && exposureLevel > BeansWrapper.EXPOSE_SAFE) {                    
                continue;
            }
            
            availableMethods.add(method);
        }
        
        return availableMethods;
    }

    protected String getMethodDisplayName(Method method) {
        String methodName = method.getName();
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length > 0) {
            List<String> paramTypeList = new ArrayList<String>(paramTypes.length);
            for (Class<?> cls : paramTypes) {
                String name = cls.getName();
                String[] nameParts = name.split("\\.");
                String typeName = nameParts[nameParts.length-1];
                typeName = typeName.replaceAll(";", "s");
                typeName = typeName.substring(0,1).toLowerCase() + typeName.substring(1);
                paramTypeList.add(typeName);
            }
            methodName += "(" + StringUtils.join(paramTypeList, ", ") + ")";
        } else {
            methodName = methodName.replaceAll("^(get|is)", "");
            methodName = methodName.substring(0, 1).toLowerCase() + methodName.substring(1);           
        }
        
        return methodName;        
    }
    
    private Map<String, Object> getBaseTemplateModelValues(TemplateModel wrappedModel, BaseTemplateModel unwrappedModel) {
        int exposureLevel = getExposureLevel(wrappedModel, unwrappedModel);
        return getBaseTemplateModelValues(unwrappedModel, exposureLevel);      
    }
    
    private Map<String, Object> getBaseTemplateModelValues(BaseTemplateModel model, int exposureLevel) {
        Map<String, Object> map = new HashMap<String, Object>();   
        map.put("value", model.toString());
        
        List<Method> availableMethods = getMethodsAvailableToTemplate(exposureLevel, model);        
        SortedMap<String, String> properties = new TreeMap<String, String>();
        List<String> methods = new ArrayList<String>();
        for (Method method : availableMethods) {
            String key = getMethodDisplayName(method);           
            if (key.endsWith(")")) {
                methods.add(key);
            } else {
                try {                   
                    Object result = method.invoke(model);
                    // RY Here we need to recurse in order to dump the full value of 
                    // the result, by calling a method formed from the second part
                    // of getVariableDumpData.
                    String value;
                    if (result ==  null) {
                        value = "null";
                    } else if (result instanceof BaseTemplateModel) {
                       value = getTemplateModelDump((BaseTemplateModel)result, exposureLevel); 
                    } else {
                        // Don't use ?html in the template, because then the output of
                        // getTemplateModelDump, which is html, gets escaped too.
                        value = StringEscapeUtils.escapeHtml(result.toString());
                    }
                    properties.put(key, value);
                } catch (Exception e) {
                    log.error(e, e);
                    continue;
                }
            }
        }
        
        map.put("type", model.getClass().getName());
        map.put("properties", properties);
        Collections.sort(methods);
        map.put("methods", methods);        
        return map;
    }
    
    private String getTemplateModelDump(BaseTemplateModel model, int exposureLevel) {
        Map<String, Object> map = getBaseTemplateModelValues(model, exposureLevel);       
        return BaseTemplateDirectiveModel.processTemplateToString("dump-var.ftl", map, env);
    }
    
    public void writeDump(String templateName, Map<String, Object> map, String modelName, TemplateHashModel dataModel) {
        
        // Add objects to data model of calling template that are needed by 
        // all dump templates.
        try {
            map.put("stylesheets", dataModel.get("stylesheets"));
            map.put("urls", dataModel.get("urls"));
        } catch (TemplateModelException e) {
            log.error("Error getting values from data model.");
        }
        
        String output = BaseTemplateDirectiveModel.processTemplateToString(templateName, map, env);      
        Writer out = env.getOut();
        try {
            out.write(output);
        } catch (IOException e) {
            log.error("Error writing dump of " + modelName + ".");
        }          
    }

}
