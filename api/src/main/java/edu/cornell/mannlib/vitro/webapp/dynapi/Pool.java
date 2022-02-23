package edu.cornell.mannlib.vitro.webapp.dynapi;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Poolable;

public interface Pool<C extends Poolable> { 

    public C getByName(String name);

    public void printNames();

    public void add(String uri, C component);

    public void remove(String uri, String name);

    public void reload();

    public void reload(String uri);

    public void init(ServletContext ctx);

    public long obsoleteCount();

    public long count();

}
