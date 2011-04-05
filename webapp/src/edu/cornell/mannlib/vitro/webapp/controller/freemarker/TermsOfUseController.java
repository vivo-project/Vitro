/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

public class TermsOfUseController extends FreemarkerHttpServlet {
    
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(TermsOfUseController.class);
    private static final String TEMPLATE_DEFAULT = "termsOfUse.ftl";

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        Portal portal = vreq.getPortal();
        
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, String> termsOfUse = new HashMap<String, String>();
        
        String rootBreadCrumbAnchor = portal.getRootBreadCrumbAnchor();
        String siteName = StringUtils.isEmpty(rootBreadCrumbAnchor) ? portal.getAppName() : rootBreadCrumbAnchor; 
        termsOfUse.put("siteName", siteName);
        
        String siteHost = portal.getCopyrightAnchor();
        if (siteHost == null) {
            siteHost = "the hosting institution";
        }
        termsOfUse.put("siteHost", siteHost);
        
        map.put("termsOfUse", termsOfUse);
        return new TemplateResponseValues(TEMPLATE_DEFAULT, map);
    }
    
    @Override
    protected String getTitle(String siteName, VitroRequest vreq) {
        return siteName + " Terms of Use";
    }
}