/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.widgets;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet.TemplateResponseValues;
import freemarker.core.Environment;

public class LoginWidget extends Widget {

    private static final Log log = LogFactory.getLog(LoginWidget.class);
    
    public LoginWidget(Environment env, String name) {
        super(env, name);
    }

    @Override
    protected TemplateResponseValues getTemplateResponseValues() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("fruit", "bananas");
        return new TemplateResponseValues (markupTemplateName(), map);
    }

}
