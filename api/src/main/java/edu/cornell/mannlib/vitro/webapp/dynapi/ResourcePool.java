package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_UNION;

import javax.servlet.ServletContext;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader;

public class ResourcePool {

	private static ResourcePool INSTANCE;
	private ServletContext ctx;
	private ContextModelAccess modelAccess;
	private OntModel dynamicAPIModel;
	private ConfigurationBeanLoader loader;

	private ResourcePool() {
		INSTANCE = this;
	}
	public static ResourcePool getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ResourcePool();
		}
		return INSTANCE;
	}

	public void init(ServletContext ctx) {
			this.ctx = ctx;
			modelAccess = ModelAccess.on(ctx);
			dynamicAPIModel = modelAccess.getOntModel(FULL_UNION);
			loader = new ConfigurationBeanLoader(	dynamicAPIModel, ctx);
			//loadActions();
	}

}
