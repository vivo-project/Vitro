/* $This file is distributed under the terms of the license in LICENSE$ */
package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread.WorkLevel.WORKING;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasoner;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "SimpleReasonerRecomputeController", urlPatterns = {"/RecomputeInferences"} )
public class SimpleReasonerRecomputeController extends FreemarkerHttpServlet {

    private static final Log log = LogFactory.getLog(
            SimpleReasonerRecomputeController.class);

    private static final String RECOMPUTE_INFERENCES_FTL = "recomputeInferences.ftl";

    @Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
    	return SimplePermission.USE_MISCELLANEOUS_ADMIN_PAGES.ACTION;
	}

	protected ResponseValues processRequest(VitroRequest vreq) {
        Map<String, Object> body = new HashMap<String, Object>();

        String messageStr = "";
        try {

        	Object sr = getServletContext().getAttribute(SimpleReasoner.class.getName());

            if (!(sr instanceof SimpleReasoner)) {
                messageStr = "No SimpleReasoner has been set up.";

            } else {
                SimpleReasoner simpleReasoner = (SimpleReasoner) sr;
                if (simpleReasoner.isABoxReasoningAsynchronous()) {
                    messageStr = "Reasoning is currently in asynchronous mode so a recompute cannot be started. Please try again later.";
                } else if (simpleReasoner.isRecomputing()) {
                        messageStr =
                            "The system is currently in the process of " +
                            "recomputing inferences.";
                } else {
                    String submit = (String)vreq.getParameter("submit");
                    if (submit != null) {
                    	VitroBackgroundThread thread = new VitroBackgroundThread(new Recomputer((simpleReasoner)),
								"SimpleReasonerRecomputController.Recomputer");
						thread.setWorkLevel(WORKING);
						thread.start();
                        messageStr = "Recompute of inferences started. See log for further details.";
                    } else {
                        body.put("formAction", UrlBuilder.getUrl("/RecomputeInferences"));
                    }
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
