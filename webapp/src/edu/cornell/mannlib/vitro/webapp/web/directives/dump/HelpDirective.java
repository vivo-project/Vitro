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
import freemarker.core.Environment;
import freemarker.template.Configuration;
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
        
        Object o = params.get("directive");
        if ( !(o instanceof SimpleScalar)) {
            throw new TemplateModelException(
               "Value of parameter 'directive' must be a string.");     
        }
        
        String directiveName = ((SimpleScalar)o).getAsString();  
        TemplateHashModel dataModel = env.getDataModel();    
        Map<String, Object> dm = (Map<String, Object>) DeepUnwrap.permissiveUnwrap(dataModel);
        Object value = dm.get(directiveName);
        
        if (! (value instanceof BaseTemplateDirectiveModel) ) {
            throw new TemplateModelException(
                directiveName + " must be the name of a directive.");  
        }
        
        Configuration config = env.getConfiguration();
        Map<String, Object> map = new HashMap<String, Object>();
        
        String help = ((BaseTemplateDirectiveModel) value).help(env);
        map.put("help", help);
        
        try {
            map.put("stylesheets", dataModel.get("stylesheets"));
        } catch (TemplateModelException e) {
            log.error("Error getting value of stylesheets variable from data model.");
        }

        DumpHelper helper = new DumpHelper(env);  
        helper.writeDump("help.ftl", map, directiveName);   
        
    }
    
    
    public String help(Environment env) {
        Map<String, Object> map = new HashMap<String, Object>();
        
        String name = getDirectiveName();
        map.put("name", name);
        
        map.put("effect", "Output directive help.");
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("var", "name of directive");
        map.put("params", params);
        
        List<String> examples = new ArrayList<String>();
        examples.add("<@" + name + " directive=\"dump\" />");
        map.put("examples", examples);
        
        return mergeToHelpTemplate(map, env);
    }

}
