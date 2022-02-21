package edu.cornell.mannlib.vitro.webapp.dynapi.components;

public interface Poolable extends Removable {

    public String getName();

    public boolean isValid();

    public void addClient();

    public void removeClient();

    public void removeDeadClients();

    public boolean hasClients();

}
