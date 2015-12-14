/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.ModelChangedListener;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

public class ModelContext {
    private static final Log log = LogFactory.getLog(ModelContext.class);
	
	/**
	 * Register a listener to the models needed to get changes to:
	 *   Basic abox statemetns:
	 *      abox object property statements
	 *      abox data property statements
	 *      abox rdf:type statements
	 *      inferred types of individuals in abox
	 *      class group membership of individuals in abox
	 *      rdfs:labe annotations of things in abox.            
	 *   
	 *   Basic application annotations:
	 *       changes to annotations on classes
	 *       changes to annotations on class gorups
	 *       changes to annotations on properties
	 *       
	 *   Changes to application model
	 */
	public static void registerListenerForChanges(ServletContext ctx, ModelChangedListener ml){
	    
        try {
            RDFServiceUtils.getRDFServiceFactory(ctx).registerJenaModelChangedListener(ml);
        } catch (RDFServiceException e) {
            log.error(e,e);
        }
        
	}
	
}
