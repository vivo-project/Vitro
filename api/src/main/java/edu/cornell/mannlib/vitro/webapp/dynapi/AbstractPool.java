package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.RDF_TYPE;
import static edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader.toJavaUri;
import static java.lang.String.format;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Poolable;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;

public abstract class AbstractPool<K, C extends Poolable<K>, P extends Pool<K, C>> implements Pool<K, C> {

    private final Log log = LogFactory.getLog(this.getClass());

    private static final Object mutex = new Object();

    private MultiAccessComponents<K, C> components;
    private ServletContext ctx;
    private ConfigurationBeanLoader loader;
    private ConcurrentLinkedQueue<C> obsoleteComponents;

    protected AbstractPool() {
        components = new MultiAccessComponents<>();
        obsoleteComponents = new ConcurrentLinkedQueue<>();
    }

    protected MultiAccessComponents<K, C> getComponents() {
        return components;
    }

    public abstract P getPool();

    public abstract C getDefault();

    public abstract Class<C> getType();

    public C get(K key) {
        C component = components.get(key);

        if (component == null) {
            return getDefault();
        }

        component.addClient();
        return component;
    }
    
    public C getByUri(String uri) {
        C component = components.getByUri(uri);
        if (component == null) {
            return getDefault();
        }
        component.addClient();
        return component;
    }

    public void printKeys() {
        for (Map.Entry<K, C> entry : components.entrySet()) {
            log.debug(format("%s in pool: '%s'", getType().getName(), entry.getKey()));
        }
    }

    public void add(String uri, C component) {
        K key = component.getKey();
        log.info(format("Adding component %s with URI %s", key, uri));
        if (isInModel(uri)) {
            synchronized (mutex) {
                K oldKey = components.putUriMapping(uri, key);
                if (oldKey != null && !oldKey.equals(key)) {
                	C oldComponent = components.get(oldKey);
                	if (oldComponent != null) {
                        obsoleteComponents.add(oldComponent);
                        unloadObsoleteComponents();
                    }
                }
                C oldComponent = components.put(key, component);
                if (oldComponent != null) {
                    obsoleteComponents.add(oldComponent);
                    unloadObsoleteComponents();
                }
            }
        } else {
            throw new RuntimeException(format("%s %s with URI %s not found in model. Not adding to pool.",
                    getType().getName(), key, uri));
        }
    }

    public void remove(K key) {
        log.info(format("Removing component with key %s", key));
		synchronized (mutex) {
			C oldComponent = components.remove(key);
			if (oldComponent != null) {
				obsoleteComponents.add(oldComponent);
				unloadObsoleteComponents();
			}
		}
    }
    
	public void unload(String uri) {
		log.info(format("Removing component with URI %s", uri));
		synchronized (mutex) {
			C oldComponent = components.removeByUri(uri);
			if (oldComponent != null) {
				obsoleteComponents.add(oldComponent);
				unloadObsoleteComponents();
			}
		}
	}
	
    public void unload() {
        List<String> uris = getLoadedUris();
        for (String uri : uris) {
            unload(uri);
        }
    }

    private boolean isInModel(String uri) {
    	Model dynamicAPIModel = DynapiModelProvider.getInstance().getModel();
        Resource s = dynamicAPIModel.getResource(uri);
        Property p = dynamicAPIModel.getProperty(RDF_TYPE);

        String javaUri = toJavaUri(getType());

        NodeIterator nit = dynamicAPIModel.listObjectsOfProperty(s, p);
        while (nit.hasNext()) {
            RDFNode node = nit.next();
            if (node.isResource() && node.toString().replace("#", ".").equals(javaUri)) {
                return true;
            }
        }

        return false;
    }
    
    public void load(String uri) {
        try {
            add(uri, loader.loadInstance(uri, getType()));
        } catch (ConfigurationBeanLoaderException e) {
            throw new RuntimeException(format("Failed to reload %s with URI %s.", getType().getName(), uri));
        }
    }

    public synchronized void reload() {
        if (ctx == null) {
            log.error(format("Context is null. Can't reload %s.", this.getClass().getName()));
            return;
        }
        if (loader == null) {
            log.error(format("Loader is null. Can't reload %s.", this.getClass().getName()));
            return;
        }
        MultiAccessComponents<K, C> newComponents = new MultiAccessComponents<>();
        loadComponents(newComponents);
        MultiAccessComponents<K, C> oldComponents = this.components;
        components = newComponents;
        for (Map.Entry<K, C> component : oldComponents.entrySet()) {
            obsoleteComponents.add(component.getValue());
            oldComponents.remove(component.getKey());
        }
        unloadObsoleteComponents();
    }
    
    public boolean isInPool(String uri) {
    	return components.containsUri(uri);
    }
    
    public List<String> getLoadedUris(){
    	return components.getUris();
    }

    public void init(ServletContext ctx) {
        this.ctx = ctx;
        loader = new ConfigurationBeanLoader(DynapiModelProvider.getInstance().getModel(), ctx);
        log.debug("Context Initialization ...");
        loadComponents(components);
    }

    private void loadComponents(MultiAccessComponents<K, C> components) {
        Map<String, C> uriToCompMap = loader.loadEach(getType());
        log.debug(format("Context Initialization. %s %s(s) currently loaded.", components.size(), getType().getName()));
        for (Map.Entry<String, C> entry : uriToCompMap.entrySet()) {
        	String uri = entry.getKey();
        	C component = entry.getValue();
			K key = component.getKey();
			if (component.isValid()) {
                components.put(key, component);
                components.putUriMapping(uri, key);
            } else {
                log.error(format("Component uri '%s', name %s with rpcName %s is invalid.", uri, getType().getName(), key));
            }
        }
        log.debug(format("Context Initialization finished. %s %s(s) loaded.", components.size(), getType().getName()));
    }

    private void unloadObsoleteComponents() {
        for (C component : obsoleteComponents) {
            if (!isComponentInUse(component)) {
                component.dereference();
                obsoleteComponents.remove(component);
            }
        }
    }

    private boolean isComponentInUse(C component) {
        if (!component.hasClients()) {
            return false;
        }
        component.removeDeadClients();
        if (!component.hasClients()) {
            return false;
        }
        return true;
    }

    public long obsoleteCount() {
        return obsoleteComponents.size();
    }

    public long count() {
        return components.size();
    }

}
