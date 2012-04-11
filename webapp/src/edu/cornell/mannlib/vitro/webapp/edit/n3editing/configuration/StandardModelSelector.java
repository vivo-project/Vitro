/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;

public class StandardModelSelector implements ModelSelector {

    private static final Log log = LogFactory.getLog(StandardModelSelector.class);
    
    public OntModel getModel(HttpServletRequest request, ServletContext context) {
        VitroRequest vreq = new VitroRequest( request );        
        
        Object sessionOntModel = null;
        if( vreq.getSession() != null) {
            OntModelSelector oms = (OntModelSelector) vreq.getSession()
            							.getAttribute("unionOntModelSelector");
            if (oms != null) {
            	sessionOntModel = oms.getABoxModel();
            }
        }
        if(sessionOntModel != null && sessionOntModel instanceof OntModel ) {
            log.info("++++++++++ using OntModelSelector from session");
            return (OntModel)sessionOntModel;
        } else if (vreq.getOntModelSelector() != null) {
            log.info("++++++++++ using OntModelSelector from request");
            return vreq.getOntModelSelector().getABoxModel();
        } else {
            log.info("++++++++++ using OntModelSelector from context");
            return ((OntModelSelector) context
            			.getAttribute("unionOntModelSelector")).getABoxModel();
        }
    }
    
    public static final ModelSelector selector = new StandardModelSelector();

}
