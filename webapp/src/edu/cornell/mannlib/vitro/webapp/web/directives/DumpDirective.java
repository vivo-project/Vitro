/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHelper;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;

public class DumpDirective implements TemplateDirectiveModel {

    private static final Log log = LogFactory.getLog(DumpDirective.class);
    
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
               "Value of parameter 'var' must be a string.");     
        }
        String var = ((SimpleScalar)o).getAsString();
        
        Object r = params.get("dataModelDump");
        boolean dataModelDump = false;
        if (r != null) {
            if ( !(r instanceof TemplateBooleanModel)) {
                throw new TemplateModelException(
                   "Value of parameter 'recursive' must be a boolean: true or false without quotation marks.");     
            }
            dataModelDump = ((TemplateBooleanModel) r).getAsBoolean(); 
        }

        TemplateHashModel dataModel = env.getDataModel();
        if (dataModelDump) {
            dataModel = (TemplateHashModel) dataModel.get("datamodel");
        }
        
        TemplateModel val =  null;
        try {
            val = dataModel.get(var);
        } catch (TemplateModelException tme) {
            log.error("Error getting value of template model " + var + " from data model.");
        }
        
        // Just use this for now. Handles nested collections.
        String value = val.toString(); 
        String type = null;

        if (val instanceof TemplateScalarModel) {
            type = "string";
        } else if (val instanceof TemplateDateModel) { 
            type = "date";
        } else if (val instanceof TemplateNumberModel) {
            type = "number";
        } else if (val instanceof TemplateBooleanModel) {
            value =  ((TemplateBooleanModel) val).getAsBoolean() ? "true" : "false";
            type = "boolean";
        } else if (val instanceof TemplateSequenceModel){
            type = "sequence";
        } else if (val instanceof TemplateHashModel) {
            type = "hash";
        // In recursive dump, we've gotten down to a raw string. Just output it.    
//        } else if (val == null) {
//            out.write(var);
//            return;
        // Add a case for BaseTemplateModel - our template model objects will have a dump() method.
        } else {
            type = "object";
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("var", var);
        map.put("value", value);
        map.put("type", type);
        
        map.put("stylesheets", dataModel.get("stylesheets"));
        //map.put("dump", this);

        Configuration config = env.getConfiguration();
        String templateName = "dump-var.ftl";
        FreemarkerHelper helper = new FreemarkerHelper();
        String output = helper.mergeMapToTemplate(templateName, map, config);      

        Writer out = env.getOut();
        out.write(output);

    }

}
