/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHelper;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.files.Stylesheets;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.SimpleDate;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class DumpDirective implements TemplateDirectiveModel {

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
            TemplateDirectiveBody body) throws TemplateException, IOException {

        if (loopVars.length != 0) {
            throw new TemplateModelException(
                "The dump directive doesn't allow loop variables.");
        }
        if (body != null) {
            throw new TemplateModelException(
                "The dump directive doesn't allow nested content.");
        }
        
        Object o = params.get("var");
        if ( !(o instanceof SimpleScalar)) {
            throw new TemplateModelException(
               "Value of 'var' must be a string.");     
        }
        String var = ((SimpleScalar)o).getAsString();

        TemplateHashModel dataModel = env.getDataModel();
        TemplateModel val = dataModel.get(var);  
        
        Configuration config = env.getConfiguration();
        String templateName = "dump-var.ftl";
        String includeTemplate;
        Object value = val;
        String type = null;
        Writer out = env.getOut();

        if (val instanceof SimpleScalar) {
            includeTemplate = "dump-string.ftl"; 
            type = "string";
        } else if (val instanceof SimpleDate) {
            includeTemplate = "dump-string.ftl"; 
            value = value.toString();
            type = "date";
        } else if (val instanceof TemplateBooleanModel) {
            includeTemplate = "dump-string.ftl"; 
            value =  ((TemplateBooleanModel) val).getAsBoolean() ? "true" : "false";
            type = "boolean";
        } else if (val instanceof SimpleSequence){
            includeTemplate = "dump-array.ftl";
        } else if (val instanceof SimpleHash) {
            includeTemplate = "dump-hash.ftl";
        // In recursive dump, we've gotten down to a raw string    
//        } else if (val == null) {
//            out.write(var);
//            return;
        } else {
            includeTemplate = "dump-string.ftl";
            value = value.toString();
            type = "object";
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("var", var);
        map.put("value", value);
        map.put("includeTemplate", includeTemplate);
        map.put("type", type);
        
        map.put("stylesheets", dataModel.get("stylesheets"));
        //map.put("dump", this);
        
        FreemarkerHelper helper = new FreemarkerHelper();
        String output = helper.mergeMapToTemplate(templateName, map, config);      

        out.write(output);

    }

}
