/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives.dump;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.web.directives.BaseTemplateDirectiveModel;
import edu.cornell.mannlib.vitro.webapp.web.methods.BaseTemplateMethodModel;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

public class DumpAllDirective extends BaseTemplateDirectiveModel {

    private static final Log log = LogFactory.getLog(DumpAllDirective.class);
    
    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
            TemplateDirectiveBody body) throws TemplateException, IOException {

        if (params.size() != 0) {
            throw new TemplateModelException(
                "The dumpAll directive doesn't allow parameters.");
        }       
        if (loopVars.length != 0) {
            throw new TemplateModelException(
                "The dumpAll directive doesn't allow loop variables.");
        }
        if (body != null) {
            throw new TemplateModelException(
                "The dumpAll directive doesn't allow nested content.");
        }
       
        TemplateHashModel dataModel = env.getDataModel();    
        @SuppressWarnings("unchecked")
        Map<String, Object> dm = (Map<String, Object>) DeepUnwrap.permissiveUnwrap(dataModel);        
        List<String> varNames = new ArrayList<String>(dm.keySet()); 
        Collections.sort(varNames);
        
        DumpHelper helper = new DumpHelper(env);       
        List<String> models = new ArrayList<String>();
        List<String> directives = new ArrayList<String>();
        List<String> methods = new ArrayList<String>();

        for (String var : varNames) {
            Object value = dm.get(var);
            if (value instanceof BaseTemplateDirectiveModel) {
                String help = ((BaseTemplateDirectiveModel) value).help(var, env);
                directives.add(help);
            } else if (value instanceof BaseTemplateMethodModel) {
                String help = ((BaseTemplateMethodModel) value).help(var, env);
                methods.add(help);                
            } else {
                models.add(helper.getVariableDump(var));
            }
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("models", models);
        map.put("directives", directives);
        map.put("methods", methods);
        map.put("containingTemplate", env.getTemplate().getName());

        helper.writeDump("dumpAll.ftl", map, "template data model", dataModel);

    }

    @Override
    public String help(String name, Environment env) {
        Map<String, Object> map = new HashMap<String, Object>();
        
        map.put("name", name);
        
        map.put("effect", "Dump the contents of the template data model.");

        map.put("comments", "Sequences (lists and arrays) are enclosed in square brackets. Hashes are enclosed in curly braces.");
        
        List<String> examples = new ArrayList<String>();
        examples.add("<@" + name + " />");
        map.put("examples", examples);
        
        return mergeToHelpTemplate(map, env);
    }

}
