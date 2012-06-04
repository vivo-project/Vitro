package edu.cornell.mannlib.vitro.webapp.rdfservice;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public interface RDFServiceFactory {

    public RDFService getRDFService();
    
    public RDFService getRDFService(VitroRequest vreq);
    
}
