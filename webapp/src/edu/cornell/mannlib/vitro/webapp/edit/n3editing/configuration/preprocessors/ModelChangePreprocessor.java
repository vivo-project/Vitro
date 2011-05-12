/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors;

import com.hp.hpl.jena.rdf.model.Model;

import javax.servlet.http.HttpServletRequest;

public interface ModelChangePreprocessor {

	public abstract void preprocess ( Model retractionsModel, Model additionsModel, HttpServletRequest request );
	
}
