/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice;

public interface RDFServiceFactory {

    /**
     * @return RDFService - an RDFService instance
     */
    public RDFService getRDFService();
    
    /**
     * Registers a listener to listen to changes in any graph in
     * the RDF store.  Any RDFService objects returned by this factory
     * should notify this listener of changes.
     * 
     * @param changeListener - the change listener
     */
    public void registerListener(ChangeListener changeListener) throws RDFServiceException;
    
    /**
     * Unregisters a listener from listening to changes in the RDF store.
     * Any RDFService objects returned by this factory should notify
     * this listener of changes.
     * 
     * @param changeListener - the change listener
     */
    public void unregisterListener(ChangeListener changeListener) throws RDFServiceException;
    
}
