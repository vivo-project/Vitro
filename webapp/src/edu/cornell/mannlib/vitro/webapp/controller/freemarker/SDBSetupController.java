/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasoner;

public class SDBSetupController extends FreemarkerHttpServlet {

    private static final Log log = LogFactory.getLog(
            SimpleReasonerRecomputeController.class);
    
    protected ResponseValues processRequest(VitroRequest vreq) { 
        // Due to requiresLoginLevel(), we don't get here unless logged in as DBA
        if (!LoginStatusBean.getBean(vreq)
                .isLoggedInAtLeast(LoginStatusBean.DBA)) {
            return new RedirectResponseValues(UrlBuilder.getUrl(Route.LOGIN));
        }
        Map<String, Object> body = new HashMap<String, Object>();
        
        String messageStr = "";
        try {
            
            if (false) {
            
            } else {
                if (false) {
                    
                } else {
                    new Thread(new SDBSetupRunner()).start();
                    messageStr = "SDB setup started";
                }
            }
            
        } catch (Exception e) {
            log.error("Error setting up SDB store", e);
            body.put("errorMessage", 
                    "Error setting up SDB store: " + 
                    e.getMessage());
            return new ExceptionResponseValues(
                    Template.ERROR_MESSAGE.toString(), body, e);            
        }
        
        body.put("message", messageStr); 
        return new TemplateResponseValues(Template.MESSAGE.toString(), body);
    }
    
    private class SDBSetupRunner implements Runnable {
        
        private SimpleReasoner simpleReasoner;
        
        public SDBSetupRunner() {
            
        }
        
        public void run() {
            
        }
        
    }
    
}
