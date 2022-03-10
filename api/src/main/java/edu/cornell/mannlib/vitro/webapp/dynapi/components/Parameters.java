package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Parameters implements Removable {

    private Map<String, Parameter> params;

    public Parameters() {
        params = new HashMap<String, Parameter>();
    }

    public void add(Parameter param) {
        params.put(param.getName(), param);
    }

    public Set<String> getNames() {
        return params.keySet();
    }

    public Map<String, Parameter> getParameters() { return params; }

    public Parameter get(String name) {
        return params.get(name);
    }

    @Override
    public void dereference() {
        for (String name : params.keySet()) {
            params.get(name).dereference();
        }
        params = null;
    }

}
