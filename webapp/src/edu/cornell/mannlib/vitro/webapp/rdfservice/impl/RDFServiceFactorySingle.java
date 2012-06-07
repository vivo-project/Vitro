package edu.cornell.mannlib.vitro.webapp.rdfservice.impl;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;

/**
 * An RDFServiceFactory that always returns the same RDFService object
 * @author bjl23
 *
 */
public class RDFServiceFactorySingle implements RDFServiceFactory {

    private RDFService rdfService;
    
    public RDFServiceFactorySingle(RDFService rdfService) {
        this.rdfService = rdfService;
    }
    
    @Override
    public RDFService getRDFService() {
        return this.rdfService;
    }
    
    @Override
    public void registerListener(ChangeListener listener) throws RDFServiceException {
        this.rdfService.registerListener(listener);
    }
    
    @Override
    public void unregisterListener(ChangeListener listener) throws RDFServiceException {
        this.rdfService.unregisterListener(listener);
    }

}
