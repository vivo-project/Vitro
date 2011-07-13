/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseMiscellaneousAdminPages;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasoner;

public class SimpleReasonerRecomputeController extends FreemarkerHttpServlet {

    private static final Log log = LogFactory.getLog(
            SimpleReasonerRecomputeController.class);
    
    private static final String RECOMPUTE_INFERENCES_FTL = "recomputeInferences.ftl";
    
    @Override
	protected Actions requiredActions(VitroRequest vreq) {
    	return new Actions(new UseMiscellaneousAdminPages());
	}

	protected ResponseValues processRequest(VitroRequest vreq) { 
        Map<String, Object> body = new HashMap<String, Object>();
        
        String messageStr = "";
        try {
        	
        	Object simpleReasoner = vreq.getSession().getServletContext().getAttribute(SimpleReasoner.class.getName());
        	
            if ((simpleReasoner instanceof SimpleReasoner) && !((SimpleReasoner)simpleReasoner).isABoxReasoningAsynchronous()) {
                messageStr = "No SimpleReasoner has been set up.";
            } else {
            	String signal = (String) vreq.getParameter("signal");
            	
        	    if (simpleReasoner instanceof SimpleReasoner) {
        	    	 
 	                if (((SimpleReasoner)simpleReasoner).isRecomputing()) {
	                    messageStr = 
	                         "The SimpleReasoner is currently in the process of " +
	                         "recomputing inferences.";
	                } else{
	                	String restart = (String)getServletContext().getAttribute("restart");
	                	if(restart == null || restart.equals("showButton") || signal == null){
	                		body.put("link", "show");
	                    	messageStr = null;
	                    	getServletContext().setAttribute("restart", "yes");
	                	}
	                	else if(signal!=null && signal.equals("Recompute")){
	                		new Thread(new Recomputer(((SimpleReasoner)simpleReasoner))).start();
	                        messageStr = "Recomputation of inferences started";
	                        getServletContext().setAttribute("restart", "showButton");
	                	}	
	                }
        	    } else {
        	    	log.equals("The attribute with name " + SimpleReasoner.class.getName() + " is not an instance of SimpleReasoner");
        	    }
            }
            
        } catch (Exception e) {
            log.error("Error recomputing inferences with SimpleReasoner", e);
            body.put("errorMessage", 
                    "There was an error while recomputing inferences: " + 
                    e.getMessage());
          return new ExceptionResponseValues(
            RECOMPUTE_INFERENCES_FTL, body, e);  
        }
        
        body.put("message", messageStr); 
        body.put("redirecturl",vreq.getContextPath()+"/RecomputeInferences");
        return new TemplateResponseValues(RECOMPUTE_INFERENCES_FTL, body);
    }
    
    private class Recomputer implements Runnable {
        
        private SimpleReasoner simpleReasoner;
        
        public Recomputer(SimpleReasoner simpleReasoner) {
            this.simpleReasoner = simpleReasoner;
        }
        
        public void run() {
            simpleReasoner.recompute();
        }
        
    }
    
}
