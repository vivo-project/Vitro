package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_UNION;

import java.util.HashMap;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Resource;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader;

public class ResourcePool {

 	private static final Log log = LogFactory.getLog(ResourcePool.class);
	private static ResourcePool INSTANCE;
	private ContextModelAccess modelAccess;
	private OntModel dynamicAPIModel;
	private ConfigurationBeanLoader loader;
	private HashMap<String, Resource> resources;
	private ServletContext ctx;

	private ResourcePool() {
		this.resources = new HashMap<String,Resource>();
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
			loadResources();
	}
	
	private void loadResources() {
		Set<Resource> resources = loader.loadEach(Resource.class);
		log.debug("Context Initialization. resources created: " + resources.size());
		for (Resource resource : resources) {
			add(resource);
		}
		log.debug("Context Initialization finished. " + resources.size() + " resources loaded.");
	}

	private void add(Resource resource) {
		resources.put(resource.getName(), resource);
	}
}
