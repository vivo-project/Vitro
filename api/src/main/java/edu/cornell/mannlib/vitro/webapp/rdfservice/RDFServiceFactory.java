/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice;

import com.hp.hpl.jena.rdf.model.ModelChangedListener;

public interface RDFServiceFactory {

    /**
     * @return RDFService - an RDFService instance
     */
    public RDFService getRDFService();
    
    /**
     * Returns an instance of RDFService that may not support being left idle
     * for long periods of time.  RDFService instances returned by this method
     * should be immediately used and closed, not stored in (for example) session
     * or context attributes.
     * 
     * This method exists to enable performance improvements resulting from a
     * lack of need to handle database connection or other service timeouts and
     * reconnects.
     * 
     * The results provided by RDFService instances returned by this method must 
     * be identical to those provided by instances returned by getRDFService().  
     *   
     * @return RDFService - an RDFService instance
     */
    public RDFService getShortTermRDFService();
    
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
     * Any RDFService objects returned by this factory should no longer notify
     * this listener of changes.
     * 
     * @param changeListener - the change listener
     */
    public void unregisterListener(ChangeListener changeListener) throws RDFServiceException;
    
    /**
     * Registers a Jena ModelChangedListener to listen to changes in any graph in
     * the RDF store.  Any RDFService objects returned by this factory
     * should notify this listener of changes.
     * 
     * @param changeListener - the change listener
     */
    public void registerJenaModelChangedListener(ModelChangedListener changeListener) throws RDFServiceException;
    
    /**
     * Unregisters a Jena ModelChangedListener from listening to changes in the RDF store.
     * Any RDFService objects returned by this factory should no longer notify
     * this listener of changes.
     * 
     * @param changeListener - the change listener
     */
    public void unregisterJenaModelChangedListener(ModelChangedListener changeListener) throws RDFServiceException;
    
}
