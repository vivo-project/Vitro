package edu.cornell.mannlib.vitro.webapp.dynapi.io.data;

public abstract class ContainerData<T> implements Data {

    protected T container;

    protected ContainerData(T container) {
        this.container = container;
    }

    public T getContainer() {
        return container;
    }

    public void setContainer(T container) {
        this.container = container;
    }

    public abstract Data getElement(String fieldName);

    public abstract boolean setElement(String fieldName, Data newData);
}
