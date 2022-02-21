package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_UNION;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.DefaultResource;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Resource;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader;

public class ResourcePool {

	private static final Log log = LogFactory.getLog(ResourcePool.class);

	private static ResourcePool INSTANCE = null;

	private static Object mutex = new Object();

	private ConcurrentHashMap<String, Resource> resources;
	private ServletContext ctx;
	private ConfigurationBeanLoader loader;
	private ContextModelAccess modelAccess;
	private OntModel dynamicAPIModel;
	private ConcurrentLinkedQueue<Resource> obsoleteResources;

	private ResourcePool() {
		resources = new ConcurrentHashMap<>();
		obsoleteResources = new ConcurrentLinkedQueue<>();
		INSTANCE = this;
	}

	public static ResourcePool getInstance() {
		ResourcePool result = INSTANCE;
		if (result == null) {
			synchronized (mutex) {
				result = INSTANCE;
				if (result == null) {
					INSTANCE = new ResourcePool();
					result = INSTANCE;
				}
			}
		}
		return result;
	}

	/**
	 * Returns a resource and registers current thread as resource client.
	 *
	 * @param name
	 * @return resource
	 */
	public Resource getByName(String name) {
		Resource resource = resources.get(name);
		if (resource == null) {
			resource = new DefaultResource();
		} else {
			resource.addClient();
		}
		return resource;
	}

	public void printResourceNames() {
		for (Map.Entry<String, Resource> entry : resources.entrySet()) {
			log.debug("Resource in pool: '" + entry.getKey() + "'");
		}
	}

	public synchronized void reload() {
		if (ctx == null ) {
			log.error("Context is null. Can't reload resource pool.");
			return;
		}
		if (loader == null ) {
			log.error("Loader is null. Can't reload resource pool.");
			return;
		}
		ConcurrentHashMap<String, Resource> newResources = new ConcurrentHashMap<>();
		loadResources(newResources);
		ConcurrentHashMap<String, Resource> oldResources = this.resources;
		this.resources = newResources;
		for (Map.Entry<String, Resource> resource : oldResources.entrySet()) {
			obsoleteResources.add(resource.getValue());
			oldResources.remove(resource.getKey());
		}
		unloadObsoleteResources();
	}

	public void init(ServletContext ctx) {
			this.ctx = ctx;
			modelAccess = ModelAccess.on(ctx);
			dynamicAPIModel = modelAccess.getOntModel(FULL_UNION);
			loader = new ConfigurationBeanLoader(dynamicAPIModel, ctx);
			loadResources(resources);
	}

	public long obsoleteResourcesCount() {
		return obsoleteResources.size();
	}
	
	public long resourcesCount() {
		return resources.size();
	}
	
	private void loadResources(ConcurrentHashMap<String, Resource> resources) {
		Set<Resource> newResources = loader.loadEach(Resource.class);
		log.debug("Context Initialization. resources created: " + resources.size());
		for (Resource resource : newResources) {
			resources.put(resource.getName(), resource);
		}
		log.debug("Context Initialization finished. " + resources.size() + " resources loaded.");
	}

	private void unloadObsoleteResources() {
		for (Resource resource : obsoleteResources) {
			if (!isResourceInUse(resource)) {
				resource.dereference();
				obsoleteResources.remove(resource);
			} 
		}
	}
	
	private boolean isResourceInUse(Resource resource) {
		if (!resource.hasClients()) {
			return false;
		}
		resource.removeDeadClients();
		if (!resource.hasClients()) {
			return false;
		}
		return true;
	}

}
