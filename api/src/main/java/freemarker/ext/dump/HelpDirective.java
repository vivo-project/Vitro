/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package freemarker.ext.dump;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class HelpDirective extends BaseDumpDirective {

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
        
        String varName = o.toString(); //((SimpleScalar)o).getAsString();  
        TemplateHashModel dataModel = env.getDataModel();    
        Object templateModel = dataModel.get(varName);

        if (! (templateModel instanceof TemplateMethodModel || templateModel instanceof TemplateDirectiveModel)) {
            throw new TemplateModelException(
                "Value of parameter '" + varName + "' must be the name of a directive or method");            
        }

        Map<String, Object> map = getTemplateVariableDump(varName, env);
        
        String type = templateModel instanceof TemplateMethodModel ? "method" : "directive";
        String title = "Template " + type + " help";
        dump(map, env, title);         
    }
    
    @Override
    public Map<String, Object> help(String name) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        
        map.put("effect", "Outputs help for a directive or method.");

        Map<String, String> params = new HashMap<String, String>();
        params.put("for", "name of directive or method");
        map.put("parameters", params);
        
        List<String> examples = new ArrayList<String>();
        examples.add("<@" + name + " for=\"dump\" />");
        examples.add("<@" + name + " for=\"profileUrl\" />");
        map.put("examples", examples);
        
        return map;
    }

}
