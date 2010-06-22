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
        String output;
        
        if (var.equals("data_model")) {       
            output = "Data model: ";
            Map<String, Object> dm = (Map<String, Object>) DeepUnwrap.permissiveUnwrap(dataModel);
            Set varNames = dm.keySet();
            for (Object varName : varNames) {
                output += (String) varName + ", ";
            }
            
            // Add shared variables
            Configuration config = env.getConfiguration();
            Set sharedVars = config.getSharedVariableNames();
            Iterator i = sharedVars.iterator();
            while (i.hasNext()) {
                String sv = (String) i.next();
                TemplateModel tm = config.getSharedVariable(sv);
                if (tm instanceof TemplateDirectiveModel ||
                        // Legacy built-ins that are added to all configurations
                        tm instanceof freemarker.template.utility.CaptureOutput ||
                        tm instanceof freemarker.template.utility.StandardCompress ||
                        tm instanceof freemarker.template.utility.HtmlEscape ||
                        tm instanceof freemarker.template.utility.NormalizeNewlines ||
                        tm instanceof freemarker.template.utility.XmlEscape) {
                    continue;
                }
                output += sv + ", ";
            }

            output = output.replaceAll(", $", ".");
            
        } else {           
            TemplateModel val = dataModel.get(var);            
            output = var + ": " + val.toString();           
        }
        
        // RY Improve by making presentation of various types more nuanced
        // Also merge to a template for formatting
        // get config from environment; get a template from config
        // merge as in FreeMarkerHttpServlet.mergeToTemplate()
        Writer out = env.getOut();
        out.write(output + "<br />");

    }

}
