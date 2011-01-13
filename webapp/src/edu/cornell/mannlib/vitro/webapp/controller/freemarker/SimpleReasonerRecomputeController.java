package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet.Template;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasoner;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;

public class SimpleReasonerRecomputeController extends FreemarkerHttpServlet {

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
            SimpleReasoner simpleReasoner = SimpleReasoner
                    .getSimpleReasonerFromServletContext(
                            vreq.getSession().getServletContext());
            if (simpleReasoner == null) {
                messageStr = "No SimpleReasoner has been set up.";
            } else {
                if (simpleReasoner.isRecomputing()) {
                    messageStr = 
                         "The SimpleReasoner is already in the process of " +
                         "recomputing inferences.";
                } else {
                    simpleReasoner.recompute();
                    messageStr = "Recomputation of inferences started";
                }
            }
            
        } catch (Exception e) {
            log.error("Error recomputing inferences with SimpleReasoner", e);
            body.put("errorMessage", 
                    "There was an error while recomputing inferences: " + 
                    e.getMessage());
            return new ExceptionResponseValues(
                    Template.ERROR_MESSAGE.toString(), body, e);            
        }
        
        body.put("message", messageStr); 
        return new TemplateResponseValues(Template.MESSAGE.toString(), body);
    }
    
}
