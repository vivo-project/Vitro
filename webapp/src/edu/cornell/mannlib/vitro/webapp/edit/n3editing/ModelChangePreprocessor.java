/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import com.hp.hpl.jena.rdf.model.Model;

public interface ModelChangePreprocessor {

	public abstract void preprocess ( Model retractionsModel, Model additionsModel );
	
}
