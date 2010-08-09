/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives.dump;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.web.directives.BaseTemplateDirectiveModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

public class DescribeDirective extends BaseTemplateDirectiveModel {

    private static final Log log = LogFactory.getLog(DescribeDirective.class);
    
    @SuppressWarnings("unchecked")
    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
            TemplateDirectiveBody body) throws TemplateException, IOException {

        if (loopVars.length != 0) {
            throw new TemplateModelException(
                "The describe directive doesn't allow loop variables.");
        }
        if (body != null) {
            throw new TemplateModelException(
                "The describe directive doesn't allow nested content.");
        }
        
        Object o = params.get("var");
        if ( !(o instanceof SimpleScalar)) {
            throw new TemplateModelException(
               "Value of parameter 'var' must be a string.");     
        }
        
        String varName = ((SimpleScalar)o).getAsString();  
        
        TemplateHashModel dataModel = env.getDataModel();

        TemplateModel tm =  null;
        try {
            tm = dataModel.get(varName);
        } catch (TemplateModelException tme) {
            log.error("Error getting value of template model " + varName + " from data model.");
        }

        Object unwrappedModel = null;
        try {
            unwrappedModel = DeepUnwrap.permissiveUnwrap(tm);
        } catch (TemplateModelException e) {
            log.error("Cannot unwrap template model  " + varName + ".");
        }

        if (! (unwrappedModel instanceof BaseTemplateModel) ) {
            throw new TemplateModelException(
                varName + " is not a template model.");                 
        }
        
        List<Method> methods = getPublicMethods(unwrappedModel.getClass());
        List<String> methodDescriptions = new ArrayList<String>(methods.size());
        for (Method m : methods) {
            methodDescriptions.add(getMethodDescription(m));
        }
        
        Map<String, Object> map = new HashMap<String, Object>(); 
        map.put("var", varName);
        map.put("methods", methodDescriptions);
        
        try {
            map.put("stylesheets", dataModel.get("stylesheets"));
        } catch (TemplateModelException e) {
            log.error("Error getting value of stylesheets variable from data model.");
        }

        DumpHelper helper = new DumpHelper(env); 
        helper.writeDump("describe.ftl", map, varName);   
    }
    
    
    public String help(Configuration config) {
        Map<String, Object> map = new HashMap<String, Object>();
        
        String name = getDirectiveName();
        map.put("name", name);
        
        map.put("effect", "Describe the methods callable on a template variable.");
        
        //map.put("comments", "");
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("var", "name of variable to describe");
        map.put("params", params);
        
        List<String> examples = new ArrayList<String>();
        examples.add("<@" + name + " var=\"stylesheets\" />");
        map.put("examples", examples);
        
        return mergeToTemplate(map, config);
    }
    
    private List<Method> getPublicMethods(Class<?> cls) {
        List<Method> methods = new ArrayList<Method>();

        // Go up the class hierarchy only until we get to the immediate subclass of BaseTemplateModel
        if (! cls.getName().equals("edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel")) {
            methods = getDeclaredPublicMethods(cls);
            methods.addAll(getPublicMethods(cls.getSuperclass()));
        }
        
        return methods;
    }
    
    private List<Method> getDeclaredPublicMethods(Class<?> cls) {
        
        List<Method> methods = new ArrayList<Method>();
        Method[] declaredMethods = cls.getDeclaredMethods();
        for (Method m : declaredMethods) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) && !Modifier.isStatic(mod)) {
                methods.add(m);
            }
        }
        return methods;
    }
   
    
    private String getMethodDescription(Method method) {
        return method.toString();
    }

}
