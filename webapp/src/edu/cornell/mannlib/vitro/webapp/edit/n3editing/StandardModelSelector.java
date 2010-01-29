package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class StandardModelSelector implements ModelSelector {

    public OntModel getModel(HttpServletRequest request, ServletContext context) {
        VitroRequest vreq = new VitroRequest( request );        
        
        Object sessionOntModel = null;
        if( vreq.getSession() != null)
            sessionOntModel = vreq.getSession().getAttribute("jenaOntModel");            
        
        if(sessionOntModel != null && sessionOntModel instanceof OntModel )
            return (OntModel)sessionOntModel;
        else 
            return (OntModel)context.getAttribute("jenaOntModel");              
    }
    
    public static final ModelSelector selector = new StandardModelSelector();

}
