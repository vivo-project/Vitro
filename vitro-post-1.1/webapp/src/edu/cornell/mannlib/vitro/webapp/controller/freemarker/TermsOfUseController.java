/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import freemarker.template.Configuration;

public class TermsOfUseController extends FreemarkerHttpServlet {
    
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(TermsOfUseController.class.getName());
    
    protected String getTitle(String siteName) {
        return siteName + " Terms of Use";
    }
    
    protected String getBody(VitroRequest vreq, Map<String, Object> body, Configuration config) {

        Portal portal = vreq.getPortal();
        
        String rootBreadCrumbAnchor = portal.getRootBreadCrumbAnchor();
        String websiteName = StringUtils.isEmpty(rootBreadCrumbAnchor) ? portal.getAppName() : rootBreadCrumbAnchor;
 
        body.put("websiteName", websiteName);
        body.put("copyrightAnchor", portal.getCopyrightAnchor());
        
        String bodyTemplate = "termsOfUse.ftl";             
        return mergeBodyToTemplate(bodyTemplate, body, config);
    }
}