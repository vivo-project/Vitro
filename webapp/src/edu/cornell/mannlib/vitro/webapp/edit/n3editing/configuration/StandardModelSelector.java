/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;

public class StandardModelSelector implements ModelSelector {

    private static final Log log = LogFactory.getLog(StandardModelSelector.class);
    
    public OntModel getModel(HttpServletRequest request, ServletContext context) {
    	return ModelAccess.on(request.getSession()).getOntModel(ModelNames.ABOX_UNION);
    }
    
    public static final ModelSelector selector = new StandardModelSelector();

}
