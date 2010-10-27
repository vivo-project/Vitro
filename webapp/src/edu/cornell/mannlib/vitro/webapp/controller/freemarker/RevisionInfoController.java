/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * Display the detailed revision information.
 */
public class RevisionInfoController extends FreemarkerHttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(RevisionInfoController.class);
    private static final String TEMPLATE_DEFAULT = "revisionInfo.ftl";
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        Map<String, Object> body = new HashMap<String, Object>();

        body.put("revisionInfoBean", RevisionInfoBean.getBean(getServletContext()));
        
        return new TemplateResponseValues(TEMPLATE_DEFAULT, body);
    }

    @Override
    protected String getTitle(String siteName) {
    	return "Revision Information for " + siteName;
    }


}
