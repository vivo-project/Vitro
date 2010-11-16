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
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

public class DumpAllDirective extends BaseTemplateDirectiveModel {

    private static final Log log = LogFactory.getLog(DumpAllDirective.class);
    
    @SuppressWarnings({ "unchecked" })
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
        Map<String, Object> dm = (Map<String, Object>) DeepUnwrap.permissiveUnwrap(dataModel);        
        List<String> varNames = new ArrayList(dm.keySet()); 
        Collections.sort(varNames);
        
        DumpHelper helper = new DumpHelper(env);
        Configuration config = env.getConfiguration();        
        List<String> models = new ArrayList<String>();
        List<String> directives = new ArrayList<String>();
        
        for (String var : varNames) {
            Object value = dm.get(var);
            if (value instanceof BaseTemplateDirectiveModel) {
                String help = ((BaseTemplateDirectiveModel) value).help(env);
                directives.add(help);
            } else {
                models.add(helper.getVariableDump(var));
            }
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("models", models);
        map.put("directives", directives);
        map.put("containingTemplate", env.getTemplate().getName());

        try {
            map.put("stylesheets", dataModel.get("stylesheets"));
        } catch (TemplateModelException e) {
            log.error("Error getting value of stylesheets variable from data model.");
        }
        
        helper.writeDump("dumpAll.ftl", map, "template data model");

    }

   @Override
    public String help(Environment env) {
        Map<String, Object> map = new HashMap<String, Object>();
        
        String name = getDirectiveName();
        map.put("name", name);
        
        map.put("effect", "Dump the contents of the template data model.");

        map.put("comments", "Sequences (lists and arrays) are enclosed in square brackets. Hashes are enclosed in curly braces.");
        
        List<String> examples = new ArrayList<String>();
        examples.add("<@" + name + " />");
        map.put("examples", examples);
        
        return mergeToHelpTemplate(map, env);
    }

}
