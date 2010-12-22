/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * Display the detailed revision information.
 */
public class RevisionInfoController extends FreemarkerHttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(RevisionInfoController.class);
    private static final String TEMPLATE_DEFAULT = "revisionInfo.ftl";
    private static final int REQUIRED_LOGIN_LEVEL = LoginStatusBean.EDITOR;

    /* requiredLoginLevel() must be an instance method, else, due to the way sublcass
     * hiding works, when called from FreemarkerHttpServlet we will get its own method,
     * rather than the subclass method. To figure out whether to display links at the
     * page level, we need another, static method.
     */
    public static int staticRequiredLoginLevel() {
        return REQUIRED_LOGIN_LEVEL;
    }
    
    @Override
    protected int requiredLoginLevel() {
        return staticRequiredLoginLevel();
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
