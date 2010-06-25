/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

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
        String output = var + ": " + val.toString();           
        
        // RY Improve by making presentation of various types more nuanced
        // Also merge to a template for formatting:
        // get config from environment; get a template from config
        // merge as in FreeMarkerHttpServlet.mergeToTemplate()
        Writer out = env.getOut();
        out.write(output + "<br />");

    }

}
