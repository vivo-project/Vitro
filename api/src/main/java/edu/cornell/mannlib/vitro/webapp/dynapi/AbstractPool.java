package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.RDF_TYPE;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_UNION;
import static edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader.toJavaUri;
import static java.lang.String.format;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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

public abstract class AbstractPool<P extends Pool<C>, C extends Poolable> implements Pool<C> {

    private final Log log = LogFactory.getLog(this.getClass());

    private static final Object mutex = new Object();

    private ConcurrentHashMap<String, C> components;
    private ServletContext ctx;
    private ConfigurationBeanLoader loader;
    private ContextModelAccess modelAccess;
    private OntModel dynamicAPIModel;
    private ConcurrentLinkedQueue<C> obsoleteComponents;

    protected AbstractPool() {
        components = new ConcurrentHashMap<>();
        obsoleteComponents = new ConcurrentLinkedQueue<>();
    }

    public abstract P getPool();

    public abstract C getDefault();

    public abstract Class<C> getType();

    public C getByName(String name) {
        C component = components.get(name);
        if (component == null) {
            component = getDefault();
        } else {
            component.addClient();
        }
        return component;
    }

    public void printNames() {
        for (Map.Entry<String, C> entry : components.entrySet()) {
            log.debug(format("%s in pool: '%s'", getType().getName(), entry.getKey()));
        }
    }

    public void add(String uri, C component) {
        String name = component.getName();
        log.info(format("Adding component %s with URI %s", name, uri));
        if (isInModel(uri)) {
            synchronized (mutex) {
                C oldComponent = components.put(name, component);
                if (oldComponent != null) {
                    obsoleteComponents.add(oldComponent);
                    unloadObsoleteComponents();
                }
            }
        } else {
            throw new RuntimeException(format("%s %s with URI %s not found in model. Not adding to pool.", getType().getName(), name, uri));
        }
    }

    public void remove(String uri, String name) {
        log.info(format("Removing component %s with URI %s", name, uri));
        if (!isInModel(uri)) {
            synchronized (mutex) {
                C oldComponent = components.remove(name);
                if (oldComponent != null) {
                    obsoleteComponents.add(oldComponent);
                    unloadObsoleteComponents();
                }
            }
        } else {
            throw new RuntimeException(format("%s %s with URI %s still exists in model. Not removing from pool.", getType().getName(), name, uri));
        }
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
        ConcurrentHashMap<String, C> newActions = new ConcurrentHashMap<>();
        loadComponents(newActions);
        ConcurrentHashMap<String, C> oldActions = this.components;
        components = newActions;
        for (Map.Entry<String, C> component : oldActions.entrySet()) {
            obsoleteComponents.add(component.getValue());
            oldActions.remove(component.getKey());
        }
        unloadObsoleteComponents();
    }

    public void init(ServletContext ctx) {
        this.ctx = ctx;
        modelAccess = ModelAccess.on(ctx);
        dynamicAPIModel = modelAccess.getOntModel(FULL_UNION);
        loader = new ConfigurationBeanLoader(dynamicAPIModel, ctx);
        log.debug("Context Initialization ...");
        loadComponents(components);
    }

    public long obsoleteCount() {
        return obsoleteComponents.size();
    }

    public long count() {
        return components.size();
    }

    private void loadComponents(ConcurrentHashMap<String, C> components) {
        Set<C> newActions = loader.loadEach(getType());
        log.debug(format("Context Initialization. %s %s(s) currently loaded.", components.size(), getType().getName()));
        for (C component : newActions) {
            if (component.isValid()) {
                components.put(component.getName(), component);
            } else {
                log.error(format("%s with rpcName %s is invalid.", getType().getName(), component.getName()));
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

}
