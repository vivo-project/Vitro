package edu.cornell.mannlib.vitro.webapp.rdfservice.impl;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;

public class RDFServiceUtils {

    private static final String RDFSERVICEFACTORY_ATTR = 
            RDFServiceUtils.class.getName() + ".RDFServiceFactory";
    
    public RDFServiceFactory getRDFServiceFactory(ServletContext context) {
        Object o = context.getAttribute(RDFSERVICEFACTORY_ATTR);
        return (o instanceof RDFServiceFactory) ? (RDFServiceFactory) o : null;
    }
    
    public void setRDFServiceFactory(ServletContext context, 
            RDFServiceFactory factory) {
        context.setAttribute(RDFSERVICEFACTORY_ATTR, factory);
    }
    
}
