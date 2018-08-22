/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.audit;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.audit.storage.AuditDAOFactory;
import edu.cornell.mannlib.vitro.webapp.audit.storage.AuditDAO;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;

/**
 * Listens for requests and manages tracking and writing audit entries
 */
@WebListener
public class AuditRequestListener implements ServletRequestListener {
    // Collection to track content models per request (thread)
    private static final ThreadLocal<AuditChangeSet> contentModelDatasets = new ThreadLocal<>();

    // The current RDFService change listener
    private static AuditChangeListener changeListener = null;

    /**
     * Get the changeset for the current request (used by the change listener)
     * @return
     */
    public static AuditChangeSet getAuditDatasetForContentModel() {
        return contentModelDatasets.get();
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        // Ensure that the audit module has been initialized
        if (isAuditEnabled()) {
            // Ensure the change listener is registered with the RDFService
            registerChangeListener(sre.getServletContext());

            // Crate a new changeset for this request / thread
            contentModelDatasets.set(new AuditChangeSet());
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        // Ensure that the audit module has been initialized
        if (isAuditEnabled()) {
            try {
                // Get the current change set
                AuditChangeSet dataset = contentModelDatasets.get();

                // If there are changes that have been tracked
                if (!dataset.isEmpty()) {
                    // Get a DAO instance
                    AuditDAO auditDAO = AuditDAOFactory.getAuditDAO(sre.getServletRequest());

                    // Write the changes to the audit store
                    auditDAO.write(dataset);
                }
            } finally {
                // Ensure the current change set is removed from the collection
                contentModelDatasets.remove();
            }
        }
    }

    /**
     * Register a change listener with the RDFService
     * @param ctx
     */
    private synchronized void registerChangeListener(ServletContext ctx) {
        // Check that no change listener has already been created
        if (changeListener == null) {
            // Get the RDF Service
            RDFService rdfService = ModelAccess.on(ctx).getRDFService();
            try {
                // Create a change listener
                changeListener = new AuditChangeListener();

                // Register the change listener
                rdfService.registerListener(new AuditChangeListener());
            } catch (RDFServiceException e) {
            }
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
        } catch (IllegalStateException ise) {
        }

        return false;
    }
}
