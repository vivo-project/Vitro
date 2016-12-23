/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.jena.rdf.model.Model;

/**
 * Interface that is intended to be used with N3 Editing to 
 * allow a EditConfiguration to specify which models will be used
 * during editing.   
 * 
 *  With Jim's new ModelAccess it may be better to use ModelAccess
 *  identifiers and graph URIs.
 *  
 */
public interface ModelSelector {
    public Model getModel(HttpServletRequest request, ServletContext context);
}
