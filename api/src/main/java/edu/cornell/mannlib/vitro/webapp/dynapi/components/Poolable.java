package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.List;

public interface Poolable<K> extends Removable {

    public K getKey();

    public boolean isValid();

    public void addClient();

    public void removeClient();

    public void removeDeadClients();

    public boolean hasClients();

    public void setUri(String uri);

    public String getUri();

    public List<String> getClients();

}
