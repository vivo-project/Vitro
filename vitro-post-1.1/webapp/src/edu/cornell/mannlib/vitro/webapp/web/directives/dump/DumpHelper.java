/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives.dump;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.TemplateProcessingHelper;
import edu.cornell.mannlib.vitro.webapp.web.directives.BaseTemplateDirectiveModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;
import freemarker.core.Environment;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;
import freemarker.template.utility.DeepUnwrap;

public class DumpHelper {

    private static final Log log = LogFactory.getLog(DumpHelper.class);
    
    private Environment environment = null;
    
    public DumpHelper(Environment env) {
        environment = env;
    }   

    public String getVariableDump(String varName) {
        Map<String, Object> map = getVariableDumpData(varName);
        TemplateProcessingHelper helper = BaseTemplateDirectiveModel.getFreemarkerHelper(environment);
        return helper.processTemplateToString("dump-var.ftl", map);
    }

    public Map<String, Object> getVariableDumpData(String varName) {
        TemplateHashModel dataModel = environment.getDataModel();

        TemplateModel tm =  null;
        try {
            tm = dataModel.get(varName);
        } catch (TemplateModelException tme) {
            log.error("Error getting value of template model " + varName + " from data model.");
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("var", varName);
        
        if (tm != null) {            
            String type = null;
            Object unwrappedModel = null;

            try {
                unwrappedModel = DeepUnwrap.permissiveUnwrap(tm);
            } catch (TemplateModelException e) {
                log.error("Cannot unwrap template model  " + varName + ".");
            }
            
            // Just use toString() method for now. Handles nested collections. Could make more sophisticated later.
            // tm.toString() gives wrong results in the case of, e.g., a boolean value in a hash. tm.toString() may
            // return a TemplateBooleanModel object, while unwrappedModel.toString() returns "true" or "false."
            String value = unwrappedModel.toString(); // tm.toString();  
            
            // This case must precede the TemplateScalarModel case, because
            // tm is an instance of StringModel and thus a TemplateScalarModel.
            if (unwrappedModel instanceof BaseTemplateModel) {
                type = unwrappedModel.getClass().getName(); 
                value = ((BaseTemplateModel)unwrappedModel).dump();
            } else if (tm instanceof TemplateScalarModel) {
                type = "string";
            } else if (tm instanceof TemplateDateModel) { 
                type = "date";
            } else if (tm instanceof TemplateNumberModel) {
                type = "number";
            } else if (tm instanceof TemplateBooleanModel) {
                type = "boolean";
                try {
                    value =  ((TemplateBooleanModel) tm).getAsBoolean() ? "true" : "false";
                } catch (TemplateModelException e) {
                    log.error("Error getting boolean value for " + varName + ".");
                }
            } else if (tm instanceof TemplateSequenceModel){
                type = "sequence";
            } else if (tm instanceof TemplateHashModel) {
                type = "hash";
            // In recursive dump, we've gotten down to a raw string. Just output it.    
            //  } else if (val == null) {
            //    out.write(var);
            //    return;
            } else {
                type = "object";
            }
            map.put("value", value);
            map.put("type", type);
        }

        return map;
    }
    
    public void writeDump(String templateName, Map<String, Object> map, String modelName) {

        TemplateProcessingHelper helper = BaseTemplateDirectiveModel.getFreemarkerHelper(environment);
        String output = helper.processTemplateToString(templateName, map);      
        Writer out = environment.getOut();
        try {
            out.write(output);
        } catch (IOException e) {
            log.error("Error writing dump of " + modelName + ".");
        }          
    }
    
}
