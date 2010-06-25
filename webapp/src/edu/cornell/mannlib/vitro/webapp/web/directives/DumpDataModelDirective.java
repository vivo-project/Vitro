/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

public class DumpDataModelDirective implements TemplateDirectiveModel {

    @SuppressWarnings({ "unchecked", "deprecation" })
    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
            TemplateDirectiveBody body) throws TemplateException, IOException {

        if (params.size() != 0) {
            throw new TemplateModelException(
                "The dump directive doesn't allow parameters.");
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
        List<String> models = new ArrayList<String>();
        List<String> directives = new ArrayList<String>();
          
        Map<String, Object> dm = (Map<String, Object>) DeepUnwrap.permissiveUnwrap(dataModel);
        Set varNames = dm.keySet();
        for (Object varName : varNames) {
            if (dm.get(varName) instanceof TemplateDirectiveModel) {
                directives.add((String) varName);
            } else {
                models.add((String) varName);
            }
        }

        Collections.sort(models);
        Collections.sort(directives);
 
        // RY Improve by making presentation of various types more nuanced
        // Also merge to a template for formatting
        // get config from environment; get a template from config
        // merge as in FreeMarkerHttpServlet.mergeToTemplate()
        String modelNames = "<p><strong>Data model:</strong> " + StringUtils.join(models, ", ") + ".</p>";
        String directiveNames = "<p><strong>Directives:</strong> " + StringUtils.join(directives, ", ") + ".</p>";

        String output = modelNames + directiveNames;
        Writer out = env.getOut();
        out.write(output);

    }

}
