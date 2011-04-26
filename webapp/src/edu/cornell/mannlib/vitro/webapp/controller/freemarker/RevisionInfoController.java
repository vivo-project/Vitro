/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.SeeRevisionInfo;
import edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * Display the detailed revision information.
 */
public class RevisionInfoController extends FreemarkerHttpServlet {
	private static final long serialVersionUID = 1L;
    private static final String TEMPLATE_DEFAULT = "revisionInfo.ftl";
    
    public static final Actions REQUIRED_ACTIONS = new Actions(new SeeRevisionInfo());
    
    @Override
    protected Actions requiredActions(VitroRequest vreq) {
    	return REQUIRED_ACTIONS;
    }
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        Map<String, Object> body = new HashMap<String, Object>();

        body.put("revisionInfoBean", RevisionInfoBean.getBean(getServletContext()));
        
        return new TemplateResponseValues(TEMPLATE_DEFAULT, body);
    }

    @Override
    protected String getTitle(String siteName, VitroRequest vreq) {
    	return "Revision Information for " + siteName;
    }

}
