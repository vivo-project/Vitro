/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.widgets;

import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet.TemplateResponseValues;
import freemarker.core.Environment;

public class TestWidget extends Widget {

    public TestWidget(Environment env, String name) {
        super(env, name);
    }

    @Override
    protected TemplateResponseValues getTemplateResponseValues() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("fruit", "bananas");
        return new TemplateResponseValues (markupTemplateName(), map);
    }

}
