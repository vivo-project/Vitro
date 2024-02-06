package edu.cornell.mannlib.vitro.webapp.controller;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

public class VitroRequestStub extends VitroRequest {

    private RDFService defaultRdfService;

    public VitroRequestStub(HttpServletRequest _req) {
        super(_req);
    }

    @Override
    public RDFService getRDFService() {
        return defaultRdfService;
    }

    public void setRDFService(RDFService rdfService) {
        this.defaultRdfService = rdfService;
    }
}
