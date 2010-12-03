/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet.Template;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

public class HomePageController extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(HomePageController.class);
    private static final String PAGE_TEMPLATE = "page-home.ftl";
    private static final String BODY_TEMPLATE = "home.ftl";

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) { 
        
        Map<String, Object> body = new HashMap<String, Object>();        
        // Add home page data to body here         
        return new TemplateResponseValues(BODY_TEMPLATE, body);
    }

    @Override
    protected String getTitle(String siteName) {
        return siteName;
    }

    @Override
    protected String getPageTemplateName() {
        return PAGE_TEMPLATE;
    }
}
