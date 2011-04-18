/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package freemarker.ext.dump;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        TemplateHashModel dataModel = env.getDataModel();  
        // Need to unwrap in order to iterate through the variables
        @SuppressWarnings("unchecked")
        Map<String, Object> unwrappedDataModel = (Map<String, Object>) DeepUnwrap.permissiveUnwrap(dataModel);        
        List<String> varNames = new ArrayList<String>(unwrappedDataModel.keySet()); 
        Collections.sort(varNames);        

        // *** RY Change data structure so it's
        // "employee" => { type =>..., value => ...}
        // rather than { name => "employee", type => ..., value => ...}
        // Then this will be a SortedMap
        // Otherwise we need a Comparator to sort on the "name" key. Yuck!
        // The first data structure seems more natural
        List<Map<String, Object>> models = new ArrayList<Map<String, Object>>();
        for (String varName : varNames) {
            models.add(getTemplateVariableDump(varName, dataModel.get(varName)));
        }
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("templateName", env.getTemplate().getName());
        
        
        map.put("models", models);

        dump("dumpAll.ftl", map, env); 
    }

    
}
