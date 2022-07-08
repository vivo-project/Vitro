package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;

public class RawData {

    Set<String> types = null;
    String string = null;
    Object object = null;
    Parameter param = null;

    public RawData(Parameter param){
        types = new HashSet<>();
        this.param = param;
    }
    
    protected void setObject(Object object) {
        this.object = object;
    }
    
    protected Object getObject() {
        return object;
    }
    
    public void setRawString(String raw) {
        this.string = raw;
    }
    
    protected String getRawString() {
        return string;
    }
}
