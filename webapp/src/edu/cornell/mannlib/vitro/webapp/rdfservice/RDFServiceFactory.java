package edu.cornell.mannlib.vitro.webapp.rdfservice;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public interface RDFServiceFactory {

    public RDFService getRDFService();
    
    /**
     * Register a listener to listen to changes in any graph in
     * the RDF store.  Any RDFService objects returned by this factory should notify
     * this listener of changes.
     * 
     * @param changeListener - the change listener
     */
    public void registerListener(ChangeListener changeListener) throws RDFServiceException;
    
    /**
     * Unregister a listener from listening to changes in
     * the RDF store.  Any RDFService objects returned by this factory should notify
     * this listener of changes.
     * 
     * @param changeListener - the change listener
     */
    public void unregisterListener(ChangeListener changeListener) throws RDFServiceException;
    
}
