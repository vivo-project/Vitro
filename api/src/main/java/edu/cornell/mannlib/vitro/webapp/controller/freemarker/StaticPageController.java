/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;

import javax.servlet.annotation.WebServlet;

/*
 * Servlet that only specifies a template, without putting any data
 * into the template model. Page content is fully specified in the template.
 */
@WebServlet(name = "StaticPageController", urlPatterns = {"/login"} )
public class StaticPageController extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(StaticPageController.class);

    @SuppressWarnings("serial")
    private static final Map<String, String> urlsToTemplates = new HashMap<String, String>(){
        {
            put("/login", "login.ftl");
        }
    };

    @Override
    protected String getTitle(String siteName, VitroRequest vreq) {
        String requestedUrl = vreq.getServletPath();
        String title = null;
        if (requestedUrl.equals("/login")) {
            title = StringUtils.capitalize(I18n.text(vreq, "log_in")) + " - " + siteName;
        }
        return title;
    }

    protected ResponseValues processRequest(VitroRequest vreq) {
        String requestedUrl = vreq.getServletPath();
        String templateName = urlsToTemplates.get(requestedUrl);

		log.debug("requestedUrl='" + requestedUrl + "', templateName='"
				+ templateName + "'");

        return new TemplateResponseValues(templateName);
    }
}
