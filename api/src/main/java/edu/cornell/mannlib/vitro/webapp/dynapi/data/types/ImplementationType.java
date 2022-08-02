package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ImplementationType {

	 private Class<?> className;

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#className", minOccurs = 1, maxOccurs = 1)
	    public void setName(String className) throws ClassNotFoundException {
	        this.className = Class.forName(className);
	    }

	public Class<?> getClassName() {
		return className;
	}
}
