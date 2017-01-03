/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONTENT;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroModelSource;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;

public class IdModelSelector implements ModelSelector {

	private final String name;

	public IdModelSelector(String name) {
		if (name == null)
			throw new IllegalArgumentException(
					"Name of model must not be null.");
		this.name = name;
	}

	@Override
	public Model getModel(HttpServletRequest request, ServletContext context) {
		ModelMaker modelMaker = ModelAccess.on(context).getModelMaker(CONTENT);
		VitroModelSource mSource = new VitroModelSource(modelMaker, context);
		return mSource.getModel(name);
	}

}
