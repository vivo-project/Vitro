/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives.dump;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
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

        TemplateModel tm =  null;
        try {
            tm = dataModel.get(varName);
        } catch (TemplateModelException e) {
            log.error("Error getting value of template model '" + varName + "' from data model.");
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("var", varName);
        
        // DON'T return null if tm == null. We still want to return the map to the template.
        if (tm != null) {   
            Object unwrappedModel = null;
            try {
                unwrappedModel = DeepUnwrap.permissiveUnwrap(tm);
            } catch (TemplateModelException e) {
                log.error("Cannot unwrap template model  " + varName + ".");
                return null;
            }
            
            // Just use toString() method for now. Handles nested collections. Could make more sophisticated later.
            // tm.toString() gives wrong results in the case of, e.g., a boolean value in a hash. tm.toString() may
            // return a TemplateBooleanModel object, while unwrappedModel.toString() returns "true" or "false."
            String value = unwrappedModel.toString(); // tm.toString();
            String className = unwrappedModel.getClass().getName();
            String type = null;
            
            // For basic Java types such as string, date, boolean, it's most helpful for the dump to
            // show the shorthand type assigned below, rather than the Java class name. But for our
            // BaseTemplateModel objects, show the actual class, since that provides additional
            // information about the object (available methods, for example) that it is helpful to
            // view in the dump. Not sure if we should handle our application-specific, non-template
            // model objects in the same way. For now, these get assigned a shorthand type below.
            if (unwrappedModel instanceof BaseTemplateModel) {
                map.putAll(getTemplateModelValues(tm, (BaseTemplateModel)unwrappedModel)); 
                type = className;
            // Can't use this, because tm of (at least some) POJOs are
            // StringModels, which are both TemplateScalarModels and TemplateHashModels
            // if (tm instanceof TemplateScalarModel)
            } else if (unwrappedModel instanceof String) {
                type = "String";
            } else if (tm instanceof TemplateDateModel) { 
                type = "Date";
            } else if (tm instanceof TemplateNumberModel) {
                type = "Number";
            } else if (tm instanceof TemplateBooleanModel) {
                type = "Boolean";
                try {
                    value = ((TemplateBooleanModel) tm).getAsBoolean() ? "true" : "false";
                } catch (TemplateModelException e) {
                    log.error("Error getting boolean value for " + varName + ".");
                }
            } else if (tm instanceof TemplateSequenceModel){
                type = "Sequence";
            } else if (tm instanceof TemplateHashModel) {
                type = "Hash";
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
    
    public void writeDump(String templateName, Map<String, Object> map, String modelName) {
        String output = BaseTemplateDirectiveModel.processTemplateToString(templateName, map, env);      
        Writer out = env.getOut();
        try {
            out.write(output);
        } catch (IOException e) {
            log.error("Error writing dump of " + modelName + ".");
        }          
    }
    
    protected List<Method> getMethodsAvailableToTemplate(TemplateModel wrappedModel, Class<?> cls) {       
        int exposureLevel = getExposureLevel(wrappedModel, cls);
        return getMethodsAvailableToTemplate(exposureLevel, cls);
    }
    
    private int getExposureLevel(TemplateModel model, Class<?> cls) {
        
        int exposureLevel;
        // Get the exposure level of the BeansWrapper that wrapped this object.
        if (model instanceof BeanModel) {
            exposureLevel = WrapperExtractor.getWrapperExposureLevel((BeanModel) model);
            log.debug("Exposure level for class " + cls.getCanonicalName() + " wrapped as " + model.getClass() + " = " + exposureLevel);
        // We don't expect to get here, since we are dealing only with BaseTemplateModel objects, which get wrapped into BeanModel objects, 
        // but it's here as a safety net.
        } else {
            HttpServletRequest request = (HttpServletRequest) env.getCustomAttribute("request");
            Configuration config = (Configuration) request.getAttribute("freemarkerConfig");
            BeansWrapper wrapper = (BeansWrapper) config.getObjectWrapper();
            exposureLevel = WrapperExtractor.getWrapperExposureLevel(wrapper);
            log.debug("Class " + cls.getCanonicalName() + " wrapped as " + model.getClass() + " uses default exposure level " + exposureLevel);
        }
        
        return exposureLevel;
    }
    
    private List<Method> getMethodsAvailableToTemplate(int exposureLevel, Class<?> cls) {
        List<Method> methods = new ArrayList<Method>();
        
        // Go up the class hierarchy only as far as the immediate subclass of BaseTemplateModel
        if (! cls.getName().equals("edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel")) {
            methods = getDeclaredMethodsAvailableToTemplate(exposureLevel, cls);
            methods.addAll(getMethodsAvailableToTemplate(exposureLevel, cls.getSuperclass()));
        }
        
        return methods;
    }
        
    private List<Method> getDeclaredMethodsAvailableToTemplate(int exposureLevel, Class<?> cls) {
        
        List<Method> methods = new ArrayList<Method>();
        Method[] declaredMethods = cls.getDeclaredMethods();
        for (Method method : declaredMethods) {
            int mod = method.getModifiers();
            if (Modifier.isPublic(mod) && !Modifier.isStatic(mod)) {
                Class<?>[] params = method.getParameterTypes();
                // If the method takes arguments, then it is not available to the template unless
                // the exposure level of the BeansWrapper that wrapped the object allows it. 
                if (params.length > 0 && exposureLevel > BeansWrapper.EXPOSE_SAFE) {                    
                    continue;
                }
                methods.add(method);
            }
        }
        return methods;
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
            methodName += "(" + StringUtils.join(paramTypeList) + ")";
        } else {
            methodName = methodName.replaceAll("^(get|is)", "");
            methodName = methodName.substring(0, 1).toLowerCase() + methodName.substring(1);           
        }
        
        return methodName;        
    }
    
    private Map<String, Object> getTemplateModelValues(TemplateModel wrappedModel, BaseTemplateModel unwrappedModel) {
        int exposureLevel = getExposureLevel(wrappedModel, unwrappedModel.getClass());
        return getTemplateModelValues(unwrappedModel, exposureLevel);      
    }
    
    private Map<String, Object> getTemplateModelValues(BaseTemplateModel model, int exposureLevel) {
        Map<String, Object> map = new HashMap<String, Object>();   
        map.put("value", model.dump());
        
        List<Method> publicMethods = getMethodsAvailableToTemplate(exposureLevel, model.getClass());        
        Map<String, String> properties = new HashMap<String, String>();
        List<String> methods = new ArrayList<String>();
        for (Method method : publicMethods) {
            // Don't include the dump method, since this is used above to provide the value of the object.
            if (method.getName().equals("dump")) {
                continue;
            }
            String key = getMethodDisplayName(method);           
            if (key.endsWith(")")) {
                methods.add(key);
            } else {
                try {                   
                    Object result = method.invoke(model);
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
        map.put("methods", methods);        
        return map;
    }
    
    private String getTemplateModelDump(BaseTemplateModel model, int exposureLevel) {
        Map<String, Object> map = getTemplateModelValues(model, exposureLevel);       
        return BaseTemplateDirectiveModel.processTemplateToString("dump-var.ftl", map, env);
    }

}
