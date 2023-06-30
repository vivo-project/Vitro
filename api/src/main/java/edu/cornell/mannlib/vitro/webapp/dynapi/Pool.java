package edu.cornell.mannlib.vitro.webapp.dynapi;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Poolable;

public interface Pool<K, C extends Poolable<K>> {

    public C get(K key);

    public void printKeys();

    public void add(String uri, C component);

    public void remove(String uri, K key);

    public void reload();

    public void reload(String uri);

    public void init(ServletContext ctx);

    public long obsoleteCount();

    public long count();

}
