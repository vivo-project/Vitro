/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread.WorkLevel.WORKING;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.services.freemarker.FreemarkerProcessingServiceSetup;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SimpleReasonerRecomputeController extends FreemarkerHttpServlet {

    private static final Log log = LogFactory.getLog(
            SimpleReasonerRecomputeController.class);

    private static ReasonerHistory history;

    private static final String RECOMPUTE_INFERENCES_FTL        = "recomputeInferences.ftl";
    private static final String RECOMPUTE_INFERENCES_STATUS_FTL = "recomputeInferencesStatus.ftl";

    public static void setHistory(ReasonerHistory history) {
        SimpleReasonerRecomputeController.history = history;
        history.receiveEvent(new SimpleReasoner.Event(SimpleReasoner.Event.Type.STARTUP, "Initialising reasoner history"));
    }

    @Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
    	return SimplePermission.USE_MISCELLANEOUS_ADMIN_PAGES.ACTION;
	}

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String status = (String)req.getParameter("status");
        if (status != null) {
            if (!PolicyHelper.isAuthorizedForActions(req, SimplePermission.USE_MISCELLANEOUS_ADMIN_PAGES.ACTION)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("You are not authorized to access this page.");
                return;
            }

            try {
                Map<String, Object> body = new HashMap<>();
                body.put("statusUrl",  UrlBuilder.getUrl("/RecomputeInferences", "status", "true"));
                body.put("rebuildUrl", UrlBuilder.getUrl("/RecomputeInferences", "rebuild", "true"));
                if (history != null) {
                    body.put("history", history.toMaps());
                }

                String rendered = FreemarkerProcessingServiceSetup.getService(
                        getServletContext()).renderTemplate(RECOMPUTE_INFERENCES_STATUS_FTL,
                        body, req);
                resp.getWriter().write(rendered);
            } catch (Exception e) {
                resp.setStatus(500);
                resp.getWriter().write(e.toString());
                log.error(e, e);
            }
        } else {
            super.doGet(req, resp);
        }
    }

    protected ResponseValues processRequest(VitroRequest vreq) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("statusUrl",  UrlBuilder.getUrl("/RecomputeInferences", "status", "true"));

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

    /**
     * This listener keeps a list of the last several SimpleReasoner events, and will
     * format them for display in a Freemarker template.
     */
    public static class ReasonerHistory implements SimpleReasoner.Listener {
        private static final Log log = LogFactory.getLog(ReasonerHistory.class);

        private final static int MAX_EVENTS = 20;

        private final Deque<SimpleReasoner.Event> events = new LinkedList<>();

        @Override
        public void receiveEvent(SimpleReasoner.Event event) {
//            if (log.isInfoEnabled()) {
//                log.info(event);
//            }
            synchronized (events) {
                events.addFirst(event);
                while (events.size() > MAX_EVENTS) {
                    events.removeLast();
                }
            }
        }

        public List<Map<String, Object>> toMaps() {
            synchronized (events) {
                List<Map<String, Object>> list = new ArrayList<>();
                for (SimpleReasoner.Event event : events) {
                    list.add(toMap(event));
                }
                return list;
            }
        }

        private Map<String, Object> toMap(SimpleReasoner.Event event) {
            Map<String, Object> map = new HashMap<>();
            map.put("event", event.getType());
            map.put("message", event.getMessage());
            return map;
        }
    }
}
