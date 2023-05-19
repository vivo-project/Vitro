/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.audit;

import edu.cornell.mannlib.vitro.webapp.audit.storage.AuditDAOFactory;
import edu.cornell.mannlib.vitro.webapp.audit.storage.AuditVocabulary;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.listeners.StatementListener;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelChangedListener;

/**
 * Listener for changes in the RDFService
 */
public class AuditChangeListener extends StatementListener implements ModelChangedListener, ChangeListener {
    private static final Log log = LogFactory.getLog(AuditChangeListener.class);

    @Override
    public void notifyModelChange(ModelChange modelChange) {

        // Convert the serialized statements into a Jena Model
        Model changes = RDFServiceUtils.parseModel(modelChange.getSerializedModel(), modelChange.getSerializationFormat());

        // Get the changeset for the current request
        AuditChangeSet auditChangeset = new AuditChangeSet();
        Model additions = auditChangeset.getAddedModel(modelChange.getGraphURI());
        
            String userId = modelChange.getUserId();
            if (StringUtils.isBlank(userId)) {
                Exception e = new Exception();
                log.debug("User id is not provided.", e);
                userId = AuditVocabulary.RESOURCE_UNKNOWN;
            }
        auditChangeset.setUserId(userId);
        
        // Is the change adding or removing statements?
        if (modelChange.getOperation() == ModelChange.Operation.REMOVE) {
            // If we are removing statements, make sure we don't retain them in the additions
            additions.remove(changes);

            // Record all of the changes in the Model of removed statements
            Model removed = auditChangeset.getRemovedModel(modelChange.getGraphURI());
            removed.add(changes);
        } else {
            // Record all of the changes in the Model of added statements
            additions.add(changes);
        }
        if (!auditChangeset.isEmpty()) {
            // Write the changes to the audit store
            AuditDAOFactory.getAuditDAO().write(auditChangeset);
        }
    }

    @Override
    public void notifyEvent(String graphURI, Object event) {
    }
}
