package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.RDF_TYPE;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_UNION;
import static edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader.toJavaUri;
import static java.lang.String.format;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Poolable;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;

public abstract class AbstractPool<K, C extends Poolable<K>, P extends Pool<K, C>> implements Pool<K, C> {

    private final Log log = LogFactory.getLog(this.getClass());

    private static final Object mutex = new Object();

    private ConcurrentNavigableMap<K, C> components;
    private ServletContext ctx;
    private ConfigurationBeanLoader loader;
    private OntModel dynamicAPIModel;
    private ConcurrentLinkedQueue<C> obsoleteComponents;

    protected AbstractPool() {
        components = new ConcurrentSkipListMap<>();
        obsoleteComponents = new ConcurrentLinkedQueue<>();
    }

    protected ConcurrentNavigableMap<K, C> getComponents() {
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

    public void remove(String uri, K key) {
        log.info(format("Removing component %s with URI %s", key, uri));
        if (!isInModel(uri)) {
            synchronized (mutex) {
                C oldComponent = components.remove(key);
                if (oldComponent != null) {
                    obsoleteComponents.add(oldComponent);
                    unloadObsoleteComponents();
                }
            }
        } else {
            throw new RuntimeException(format("%s %s with URI %s still exists in model. Not removing from pool.",
                    getType().getName(), key, uri));
        }
    }

    private boolean isInModel(String uri) {
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

    public void reload(String uri) {
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
        ConcurrentNavigableMap<K, C> newActions = new ConcurrentSkipListMap<>();
        loadComponents(newActions);
        ConcurrentNavigableMap<K, C> oldActions = this.components;
        components = newActions;
        for (Map.Entry<K, C> component : oldActions.entrySet()) {
            obsoleteComponents.add(component.getValue());
            oldActions.remove(component.getKey());
        }
        unloadObsoleteComponents();
    }

    public void init(ServletContext ctx) {
        this.ctx = ctx;
        ContextModelAccess modelAccess = ModelAccess.on(ctx);
        dynamicAPIModel = modelAccess.getOntModel(FULL_UNION);
        loader = new ConfigurationBeanLoader(dynamicAPIModel, ctx);
        log.debug("Context Initialization ...");
        loadComponents(components);
    }

    private void loadComponents(ConcurrentNavigableMap<K, C> components) {
        Set<C> newActions = loader.loadEach(getType());
        log.debug(format("Context Initialization. %s %s(s) currently loaded.", components.size(), getType().getName()));
        for (C component : newActions) {
            if (component.isValid()) {
                components.put(component.getKey(), component);
            } else {
                log.error(format("%s with rpcName %s is invalid.", getType().getName(), component.getKey()));
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
