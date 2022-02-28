package edu.cornell.mannlib.vitro.webapp.dynapi.components;

public interface Versionable<V extends Versioned> extends Poolable<V> {

    public String getVersionMin();

    public String getVersionMax();

}
