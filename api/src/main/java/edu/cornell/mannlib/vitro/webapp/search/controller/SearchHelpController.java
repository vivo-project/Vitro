/* $This file is distributed under the terms of the license in LICENSE$ */
package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;


@WebServlet(name = "SearchHelpController", urlPatterns = {"/searchHelp"} )
public class SearchHelpController extends FreemarkerHttpServlet {

    private static final Log log = LogFactory.getLog(SearchHelpController.class.getName());
    private static final String TEMPLATE_NAME = "search-help.ftl";

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {

        Map<String, Object> body = new HashMap<String, Object>();

        String pointOfOrigin = "helpLink";

        body.put("origination", pointOfOrigin);

        return new TemplateResponseValues(TEMPLATE_NAME, body);
    }

}


