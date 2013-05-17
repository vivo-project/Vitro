/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

public class ModelContext {
    
    private static final Log log = LogFactory.getLog(ModelContext.class);
	
	private static final String ONT_MODEL_SELECTOR = "ontModelSelector";
	private static final String UNION_ONT_MODEL_SELECTOR = "unionOntModelSelector";
	private static final String BASE_ONT_MODEL_SELECTOR = "baseOntModelSelector";
	private static final String INFERENCE_ONT_MODEL_SELECTOR = "inferenceOntModelSelector";
	
	private static final String INFERENCE_ONT_MODEL = "inferenceOntModel";

	public ModelContext() {}
	
	public static OntModelSelector getOntModelSelector(ServletContext ctx) {
		return (OntModelSelector) ctx.getAttribute(ONT_MODEL_SELECTOR);
	}
	
	public static void setOntModelSelector(OntModelSelector oms, ServletContext ctx) {
		ctx.setAttribute(ONT_MODEL_SELECTOR, oms); 
	}
	
	public static OntModelSelector getUnionOntModelSelector(ServletContext ctx) {
		return (OntModelSelector) ctx.getAttribute(UNION_ONT_MODEL_SELECTOR);
	}
	
	public static void setUnionOntModelSelector(OntModelSelector oms, ServletContext ctx) {
		ctx.setAttribute(UNION_ONT_MODEL_SELECTOR, oms); 
	}
 	
	public static OntModelSelector getBaseOntModelSelector(ServletContext ctx) {
		return (OntModelSelector) ctx.getAttribute(BASE_ONT_MODEL_SELECTOR);
	}
	
	public static void setBaseOntModelSelector(OntModelSelector oms, ServletContext ctx) {
		ctx.setAttribute(BASE_ONT_MODEL_SELECTOR, oms); 
	}
	
	public static OntModelSelector getInferenceOntModelSelector(ServletContext ctx) {
		return (OntModelSelector) ctx.getAttribute(INFERENCE_ONT_MODEL_SELECTOR);
	}
	
	public static void setInferenceOntModelSelector(OntModelSelector oms, ServletContext ctx) {
		ctx.setAttribute(INFERENCE_ONT_MODEL_SELECTOR, oms); 
	}
	
	public static OntModel getInferenceOntModel(ServletContext ctx) {
		return (OntModel) ctx.getAttribute(INFERENCE_ONT_MODEL);
	}
	
	public static void setInferenceOntModel(OntModel ontModel, ServletContext ctx) {
		ctx.setAttribute(INFERENCE_ONT_MODEL, ontModel);
	}
	
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
            RDFServiceUtils.getRDFServiceFactory(ctx).registerListener(
                    new JenaChangeListener(ml));
        } catch (RDFServiceException e) {
            log.error(e,e);
        }
        
	}
	
}
