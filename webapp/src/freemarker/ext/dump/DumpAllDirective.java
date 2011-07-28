/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package freemarker.ext.dump;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

public class DumpAllDirective extends BaseDumpDirective {

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(DumpDirective.class);
    
    @SuppressWarnings("rawtypes")
    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
            TemplateDirectiveBody body) throws TemplateException, IOException {

        if (params.size() != 0) {
            throw new TemplateModelException(
                "The dumpAll directive doesn't allow parameters.");
        }    
        
        if (loopVars.length != 0) {
            throw new TemplateModelException(
                "The dump directive doesn't allow loop variables.");
        }
        
        if (body != null) {
            throw new TemplateModelException(
                "The dump directive doesn't allow nested content.");
        }

        SortedMap<String, Object> dump = getDataModelDump(env);
        String title = "Template data model dump for " + env.getTemplate().getName();
        dump(dump, env, title);
    }
    
    SortedMap<String, Object> getDataModelDump(Environment env) throws TemplateModelException {
        SortedMap<String, Object> dump = new TreeMap<String, Object>();
        TemplateHashModel dataModel = env.getDataModel();  
        // Need to unwrap in order to iterate through the variables
        @SuppressWarnings("unchecked")
        Map<String, Object> unwrappedDataModel = (Map<String, Object>) DeepUnwrap.permissiveUnwrap(dataModel);        
        List<String> varNames = new ArrayList<String>(unwrappedDataModel.keySet());  

        for (String varName : varNames) {
            dump.putAll(getTemplateVariableDump(varName, dataModel.get(varName)));
        }
        
        return dump;
        
    }

    @Override
    public Map<String, Object> help(String name) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        map.put("effect", "Dumps the contents of the template data model.");

        List<String> examples = new ArrayList<String>();
        examples.add("<@" + name + " />");
        map.put("examples", examples);
        
        return map;
    }
}
