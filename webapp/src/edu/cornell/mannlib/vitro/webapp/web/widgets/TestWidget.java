/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.widgets;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import freemarker.core.Environment;

public class TestWidget extends Widget {

    @Override
    protected WidgetTemplateValues process(Environment env, Map params, String widgetName, HttpServletRequest request, ServletContext context) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("fruit", "bananas");
        return new WidgetTemplateValues (getMarkupTemplateName(widgetName), map);
    }

}
