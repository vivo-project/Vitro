/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.widgets;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import freemarker.core.Environment;

public class TestWidget extends Widget {

    @Override
    protected WidgetTemplateValues process(Environment env, Map params,
            VitroRequest vreq, ServletContext context) {
        Map<String, Object> map = new HashMap<String, Object>();
        String macroName;
        if (LoginStatusBean.getBean(vreq).isLoggedIn()) {
            map.put("status", "logged in");
            macroName = "loggedIn";
        } else {
            map.put("status", "not logged in");
            macroName = "notLoggedIn";
        }
        return new WidgetTemplateValues (macroName, map);
    }

}
