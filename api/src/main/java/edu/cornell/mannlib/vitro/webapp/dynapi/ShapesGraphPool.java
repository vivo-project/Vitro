package edu.cornell.mannlib.vitro.webapp.dynapi;

import static java.lang.String.format;

import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ShapesGraphPool {
    
    private final Log log = LogFactory.getLog(this.getClass());
    private static ShapesGraphPool INSTANCE = new ShapesGraphPool();
    private static final Object mutex = new Object();
    
    public static ShapesGraphPool getInstance() {
        return INSTANCE;
    }
    
    private ConcurrentSkipListMap<String,ShapesGraphComponent> components = new ConcurrentSkipListMap<>();
    
    public ShapesGraphComponent get(String key) {
        if (key == null) {
            return null;
        }
        ShapesGraphComponent component = components.get(key);
        if (component == null) {
            return null;
        }
        return component;
    }
    
    public void add(String name, ShapesGraphComponent component) {
        log.info(format("Adding ShapesGraph with name %s", name));
        synchronized (mutex) {
            ShapesGraphComponent oldComponent = components.put(name, component);
            if (oldComponent != null) {
                log.info(format("Updated ShapesGraph with name %s", name));
            }
        }
    }

    public void remove(String name) {
        log.info(format("Removing ShapesGraph with name %s", name));
        synchronized (mutex) {
            components.remove(name);
        }
    }
    
    public void clear() {
        synchronized (mutex) {
            components.clear();
        }
    }
    
    public int count() {
        return components.size();
    }
}
