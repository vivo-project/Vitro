package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Parameters implements Removable {

	private Map<String, Parameter> params;

	public Parameters() {
		params = new HashMap<String, Parameter>();
	}

	public void add(Parameter param) {
		params.put(param.getName(), param);
	}

	public void addAll(Parameters newParams) {
		Set<String> names = newParams.getNames();
		for (String name : names) {
			add(newParams.get(name));
		}
	}

	public void removeAll(Parameters toRemove) {
		Set<String> names = toRemove.getNames();
		for (String name : names) {
			params.remove(name);
		}
	}

	public Set<String> getNames() {
		return params.keySet();
	}

	public Parameter get(String name) {
		return params.get(name);
	}

	public int size() {
		return params.size();
	}

	public boolean contains(String name) {
		return params.containsKey(name);
	}

	@Override
	public void dereference() {
		for (String name : params.keySet()) {
			params.get(name).dereference();
		}
		params = null;
	}

    public boolean contains(Parameter param) {
         if (!params.containsKey(param.getName())) {
             return false;
         }
         Parameter storedParam = params.get(param.getName());
         return storedParam.equals(param);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Parameters)) {
            return false;
        }
        if (object == this) {
            return true;
        }
        Parameters compared = (Parameters) object;

        return new EqualsBuilder()
                .append(params, compared.params)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(59, 103)
                .append(params)
                .toHashCode();
    }
}
