package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Removable;

public interface Validator extends Removable {

	public boolean isValid(String name, String[] values);

	public String getName();

	public void setName(String getName);

}
