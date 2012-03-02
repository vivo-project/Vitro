/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.jena.rdf.model.Model;

public interface ModelSelector {
    public Model getModel(HttpServletRequest request, ServletContext context);
}
