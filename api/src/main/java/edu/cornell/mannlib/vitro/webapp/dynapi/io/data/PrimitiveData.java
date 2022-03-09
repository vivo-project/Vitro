package edu.cornell.mannlib.vitro.webapp.dynapi.io.data;

import java.util.ArrayList;
import java.util.List;

public abstract class PrimitiveData<T> implements Data {

    protected T value;

    protected PrimitiveData(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public List<String> getAsString() {
        List<java.lang.String> retVal = new ArrayList<String>();
        retVal.add((value != null) ? value.toString() : "");
        return retVal;
    }

    @Override
    public String toString() {
        return value.toString();
    }

}
