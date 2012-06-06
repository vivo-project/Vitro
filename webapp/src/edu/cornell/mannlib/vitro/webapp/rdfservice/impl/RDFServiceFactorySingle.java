package edu.cornell.mannlib.vitro.webapp.rdfservice.impl;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
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
    public RDFService getRDFService(VitroRequest vreq) {
        return this.rdfService;
    }

}
