/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.audit;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Listens for requests and manages tracking and writing audit entries
 */
@WebListener
public class AuditSetup implements ServletContextListener {
    private static final AuditChangeListener CHANGE_LISTENER = new AuditChangeListener();
    // Collection to track content models per request (thread)
    private static final Log log = LogFactory.getLog(AuditSetup.class.getName());


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Ensure that the audit module has been initialized
        if (isAuditEnabled()) {
            // Ensure the change listener is registered with the RDFService
            registerChangeListener(sce.getServletContext());
        }
    }

    /**
     * Register a change listener with the RDFService
     * @param ctx
     */
    private synchronized void registerChangeListener(ServletContext ctx) {
        // Check that no change listener has already been created
        RDFService contentRdfService = ModelAccess.on(ctx).getRDFService(WhichService.CONTENT);
        try {
            // Register the change listener
            contentRdfService.registerListener(CHANGE_LISTENER);
        } catch (RDFServiceException e) {
            log.error(e, e);
        }
        RDFService configurationRdfService = ModelAccess.on(ctx).getRDFService(WhichService.CONFIGURATION);
        try {
            // Register the change listener
            configurationRdfService.registerListener(CHANGE_LISTENER);
        } catch (RDFServiceException e) {
            log.error(e, e);
        }
    }

    /**
     * Check that the audit module has been initialized
     * @return
     */
    private boolean isAuditEnabled(){
        try {
            // Audit module is enabled if there is one available in the application
            return ApplicationUtils.instance().getAuditModule() != null;
        } catch (IllegalStateException e) {
            log.error(e, e);
        }
        return false;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
