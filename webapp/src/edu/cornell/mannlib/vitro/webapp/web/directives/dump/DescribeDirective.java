/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives.dump;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.web.directives.BaseTemplateDirectiveModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;
import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

public class DescribeDirective extends BaseTemplateDirectiveModel {

    private static final Log log = LogFactory.getLog(DescribeDirective.class);
    
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
        
        DumpHelper helper = new DumpHelper(env); 
        List<Method> methods = helper.getMethodsAvailableToTemplate(tm, unwrappedModel.getClass());
        List<String> methodDisplayNames = new ArrayList<String>(methods.size());
        for (Method m : methods) {
            methodDisplayNames.add(helper.getMethodDisplayName(m));
        }
        Collections.sort(methodDisplayNames);
        
        Map<String, Object> map = new HashMap<String, Object>(); 
        map.put("var", varName);
        map.put("methods", methodDisplayNames);

        helper.writeDump("describe.ftl", map, varName, dataModel);   
    }
    
    @Override
    public String help(String name, Environment env) {
        Map<String, Object> map = new HashMap<String, Object>();
        
        map.put("name", name);
        
        map.put("effect", "Describe the methods callable on a template variable.");
        
        //map.put("comments", "");
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("var", "name of variable to describe");
        map.put("params", params);
        
        List<String> examples = new ArrayList<String>();
        examples.add("<@" + name + " var=\"stylesheets\" />");
        map.put("examples", examples);
        
        return mergeToHelpTemplate(map, env);
    }

}
