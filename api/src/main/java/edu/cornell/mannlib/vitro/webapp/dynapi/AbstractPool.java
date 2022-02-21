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

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Poolable;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader;

public abstract class AbstractPool<P extends Pool<C>, C extends Poolable> implements Pool<C> {

    private final Log log = LogFactory.getLog(this.getClass());

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
            log.debug("C in pool: '" + entry.getKey() + "'");
        }
    }

    public synchronized void reload() {
        if (ctx == null) {
            log.error("Context is null. Can't reload component pool.");
            return;
        }
        if (loader == null) {
            log.error("Loader is null. Can't reload component pool.");
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
        log.debug("Context Initialization. components loaded: " + components.size());
        for (C component : newActions) {
            if (component.isValid()) {
                components.put(component.getName(), component);
            } else {
                log.error("C with rpcName " + component.getName() + " is invalid.");
            }
        }
        log.debug("Context Initialization finished. " + components.size() + " components loaded.");
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

}
