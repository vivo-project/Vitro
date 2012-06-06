package edu.cornell.mannlib.vitro.webapp.rdfservice.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;

public class RDFServiceUtils {

    private static final String RDFSERVICEFACTORY_ATTR = 
            RDFServiceUtils.class.getName() + ".RDFServiceFactory";
    
    public static RDFServiceFactory getRDFServiceFactory(ServletContext context) {
        Object o = context.getAttribute(RDFSERVICEFACTORY_ATTR);
        return (o instanceof RDFServiceFactory) ? (RDFServiceFactory) o : null;
    }
    
    public static void setRDFServiceFactory(ServletContext context, 
            RDFServiceFactory factory) {
        context.setAttribute(RDFSERVICEFACTORY_ATTR, factory);
    }
    
    public static InputStream toInputStream(String serializedRDF) {
        try {
            return new ByteArrayInputStream(serializedRDF.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
}
