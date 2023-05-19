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
            ServletContext ctx = sce.getServletContext();
            // Register listener with the RDFServices
            RDFService contentRdfService = ModelAccess.on(ctx).getRDFService(WhichService.CONTENT);
            registerChangeListener(contentRdfService);
            RDFService configurationRdfService = ModelAccess.on(ctx).getRDFService(WhichService.CONFIGURATION);
            registerChangeListener(configurationRdfService);
        }
    }

    /**
     * Register a change listener with the RDFService
     * @param ctx
     */
    protected synchronized void registerChangeListener(RDFService rdfService) {
        try {
            rdfService.registerListener(CHANGE_LISTENER);
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
