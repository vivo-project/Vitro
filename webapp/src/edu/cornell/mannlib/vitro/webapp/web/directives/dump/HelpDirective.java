/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives.dump;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.web.directives.BaseTemplateDirectiveModel;
import edu.cornell.mannlib.vitro.webapp.web.methods.BaseTemplateMethodModel;
import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

public class HelpDirective extends BaseTemplateDirectiveModel {

    private static final Log log = LogFactory.getLog(HelpDirective.class);
    
    @SuppressWarnings("unchecked")
    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
            TemplateDirectiveBody body) throws TemplateException, IOException {

        if (loopVars.length != 0) {
            throw new TemplateModelException(
                "The help directive doesn't allow loop variables.");
        }
        if (body != null) {
            throw new TemplateModelException(
                "The help directive doesn't allow nested content.");
        }
        
        Object o = params.get("for");
        
        if ( o == null) {
            throw new TemplateModelException(
                "Must specify 'for' argument.");
        }

        if ( !(o instanceof SimpleScalar)) {
            throw new TemplateModelException(
               "Value of parameter 'for' must be a string.");     
        }  
        
        String name = ((SimpleScalar)o).getAsString(); 
        TemplateHashModel dataModel = env.getDataModel();    
        Map<String, Object> dm = (Map<String, Object>) DeepUnwrap.permissiveUnwrap(dataModel);
        Object value = dm.get(name);
        
        if (value == null) {
            throw new TemplateModelException(
                "Value of parameter '" + name + "' must be the name of a directive or method");              
        }

        String help;
        String type;
        if (value instanceof BaseTemplateDirectiveModel) {
            help = ((BaseTemplateDirectiveModel) value).help(name, env);
            type = "directive";
        } else if (value instanceof BaseTemplateMethodModel) {
            help = ((BaseTemplateMethodModel) value).help(name, env);
            type = "method";
        } else {
            throw new TemplateModelException(
                "Value of parameter '" + name + "' must be the name of a directive or method");            
        }

        Map<String, Object> map = new HashMap<String, Object>();       
        map.put("help", help);
        map.put("type", type);

        DumpHelper helper = new DumpHelper(env);  
        helper.writeDump("help.ftl", map, name, dataModel);   
        
    }
    
    @Override
    public String help(String name, Environment env) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("name", name);
        
        map.put("effect", "Output help for a directive or method.");
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("for", "name of directive or method");
        map.put("params", params);
        
        List<String> examples = new ArrayList<String>();
        examples.add("<@" + name + " for=\"dump\" />");
        examples.add("<@" + name + " for=\"profileUrl\" />");
        map.put("examples", examples);
        
        return mergeToHelpTemplate(map, env);
    }

}
