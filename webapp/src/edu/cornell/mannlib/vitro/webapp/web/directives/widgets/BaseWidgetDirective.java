/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives.widgets;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.web.directives.BaseTemplateDirectiveModel;
import freemarker.core.Environment;
import freemarker.ext.servlet.AllHttpScopesHashModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class BaseWidgetDirective extends BaseTemplateDirectiveModel {

    private static final Log log = LogFactory.getLog(BaseWidgetDirective.class);
    
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
        
        Object o = params.get("name");
        if ( !(o instanceof SimpleScalar)) {
            throw new TemplateModelException(
               "Value of parameter 'name' must be a string.");     
        }

        AllHttpScopesHashModel dataModel = (AllHttpScopesHashModel)(env.getDataModel());
        
        System.out.println("In widget " + o.toString());
        
    }

}
