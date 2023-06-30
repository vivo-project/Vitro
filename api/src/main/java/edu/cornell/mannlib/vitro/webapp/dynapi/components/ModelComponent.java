package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ModelComponent implements Removable {

	private String name;
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#name", minOccurs = 1, maxOccurs = 1)
	public void setName(String name) {
		this.name = ModelNames.namesMap.get(name);
	}

	@Override
	public void dereference() {
		this.name = null;
	}

	public String getName() {
		return name;
	}

}
