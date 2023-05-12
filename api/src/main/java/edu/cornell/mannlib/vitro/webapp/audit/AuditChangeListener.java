/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.audit;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import org.apache.jena.rdf.listeners.StatementListener;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelChangedListener;

/**
 * Listener for changes in the RDFService
 */
public class AuditChangeListener extends StatementListener implements ModelChangedListener, ChangeListener {
    @Override
    public void notifyModelChange(ModelChange modelChange) {
        // Check this is a change that we want to track
        if(isABoxInferenceGraph(modelChange.getGraphURI()) || isTBoxGraph(modelChange.getGraphURI())) {
            return;
        }

        // Convert the serialized statements into a Jena Model
        Model changes = RDFServiceUtils.parseModel(modelChange.getSerializedModel(), modelChange.getSerializationFormat());

        // Get the changeset for the current request
        AuditChangeSet auditChangeset = AuditRequestListener.getAuditDatasetForContentModel();
        if (auditChangeset != null) {
            Model additions = auditChangeset.getAddedModel(modelChange.getGraphURI());

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
        }
    }

    @Override
    public void notifyEvent(String graphURI, Object event) {

    }

    private boolean isABoxInferenceGraph(String graphURI) {
        return ModelNames.ABOX_INFERENCES.equals(graphURI);
    }

    private boolean isTBoxGraph(String graphURI) {
        return ( ModelNames.TBOX_ASSERTIONS.equals(graphURI)
                || ModelNames.TBOX_INFERENCES.equals(graphURI)
                || (graphURI != null && graphURI.contains("tbox")) );
    }
}
