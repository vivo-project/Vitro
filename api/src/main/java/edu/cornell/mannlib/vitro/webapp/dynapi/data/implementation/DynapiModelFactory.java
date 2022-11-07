package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import javax.servlet.ServletContext;

import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;

public class DynapiModelFactory {

    private static DynapiModelFactory INSTANCE = new DynapiModelFactory();
	private static ContextModelAccess modelAccess;

	public static DynapiModelFactory getInstance() {
        return INSTANCE;
	}

	public void init(ServletContext ctx) {
		modelAccess = ModelAccess.on(ctx);		
	}

	public static Model getModel(String modelName) {
		String uri = ModelNames.namesMap.get(modelName);
		Model model = modelAccess.getOntModel(uri);
		return model;
	}
}
