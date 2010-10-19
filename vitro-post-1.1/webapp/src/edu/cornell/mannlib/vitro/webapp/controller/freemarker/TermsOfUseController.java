/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import freemarker.template.Configuration;

public class TermsOfUseController extends FreemarkerHttpServlet {
    
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(TermsOfUseController.class);
    private static final String TEMPLATE_DEFAULT = "termsOfUse.ftl";

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        Portal portal = vreq.getPortal();
        
        Map<String, Object> body = new HashMap<String, Object>();
        
        String rootBreadCrumbAnchor = portal.getRootBreadCrumbAnchor();
        String websiteName = StringUtils.isEmpty(rootBreadCrumbAnchor) ? portal.getAppName() : rootBreadCrumbAnchor; 
        body.put("websiteName", websiteName);
        
        body.put("copyrightAnchor", portal.getCopyrightAnchor());
        
        return new TemplateResponseValues(TEMPLATE_DEFAULT, body);
    }
    
    @Override
    protected String getTitle(String siteName) {
        return siteName + " Terms of Use";
    }
}