package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Removable;

public interface Validator extends Removable {

    boolean isValid(String name, String value);

}
