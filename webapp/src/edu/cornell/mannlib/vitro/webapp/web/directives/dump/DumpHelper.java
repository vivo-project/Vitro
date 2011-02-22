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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import edu.cornell.mannlib.vitro.webapp.web.directives.BaseTemplateDirectiveModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;
import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.WrapperExtractor;
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
            return null;
        }
        
        if (tm == null) {
            log.error("No variable '" + varName + "' defined in data model." );
            return null;
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("var", varName);
        
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
                value = getTemplateModelDump(tm, (BaseTemplateModel)unwrappedModel); //((BaseTemplateModel)unwrappedModel).dump();   
                type = className;
            }            
            // Can't use this, because tm of (at least some) POJOs are
            // StringModels, which are both TemplateScalarModels and TemplateHashModels
            // if (tm instanceof TemplateScalarModel)
            else if (unwrappedModel instanceof String) {
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
            
            map.put("value", value);
            map.put("type", type);
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
        List<Method> methods = new ArrayList<Method>();

        // Go up the class hierarchy only as far as the immediate subclass of BaseTemplateModel
        if (! cls.getName().equals("edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel")) {
            methods = getDeclaredMethodsAvailableToTemplate(wrappedModel, cls);
            methods.addAll(getMethodsAvailableToTemplate(wrappedModel, cls.getSuperclass()));
        }
        
        return methods;
    }
        
    private List<Method> getDeclaredMethodsAvailableToTemplate(TemplateModel wrappedModel, Class<?> cls) {
        
        List<Method> methods = new ArrayList<Method>();
        Method[] declaredMethods = cls.getDeclaredMethods();
        for (Method method : declaredMethods) {
            int mod = method.getModifiers();
            if (Modifier.isPublic(mod) && !Modifier.isStatic(mod)) {
                Class<?>[] params = method.getParameterTypes();
                // If the method takes arguments...
                if (params.length > 0) {                    
                    // Unless the object has been wrapped with a non-default BeansWrapper with an exposure
                    // level that is more permissive than the Configuration's default BeansWrapper, this
                    // method is not visible to the template.
                    if ( ! ( wrappedModel instanceof BeanModel ) ) { 
                        continue;
                    }
                    int exposureLevel = WrapperExtractor.getWrapperExposureLevel((BeanModel)wrappedModel);
                    if ( exposureLevel > BeansWrapper.EXPOSE_SAFE ) {
                        continue;
                    }
                }
                methods.add(method);
            }
        }
        return methods;
    }
    
    protected String getMethodDisplayName(Method method) {
        String methodName = method.getName();
        methodName = methodName.replaceAll("^(get|is)", "");
        methodName = methodName.substring(0, 1).toLowerCase() + methodName.substring(1);

        Class<?>[] paramTypes = method.getParameterTypes();
        String paramList = "";
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
            paramList = "(" + StringUtils.join(paramTypeList) + ")";
        }
        
        return methodName + paramList;        
    }
    
    private String getTemplateModelDump(TemplateModel wrappedModel, BaseTemplateModel unwrappedModel) {

//        if (tm instanceof BeanModel) {                
//            int exposureLevel = WrapperExtractor.getWrapperExposureLevel((BeanModel) tm);
//            log.debug(varName + " is an instance of BeanModel. Exposure level = " + exposureLevel);
//        }
        
        Map<String, Object> map = new HashMap<String, Object>();        
        List<Method> publicMethods = getMethodsAvailableToTemplate(wrappedModel, unwrappedModel.getClass());        
        Map<String, String> properties = new HashMap<String, String>();
        List<String> methods = new ArrayList<String>();
        for (Method method : publicMethods) {
            String key = getMethodDisplayName(method);
            
            if (key.endsWith(")")) {
                methods.add(key);
            } else {
                try {                   
                    Object result = method.invoke(unwrappedModel);
                    String value = result == null ? "null" : result.toString();
                    properties.put(key, value);
                } catch (Exception e) {
                    log.error(e, e);
                    continue;
                }
            }
        }

        map.put("properties", properties);
        map.put("methods", methods);
        return BaseTemplateDirectiveModel.processTemplateToString("dump-tm.ftl", map, env);
        
    }
    
}
