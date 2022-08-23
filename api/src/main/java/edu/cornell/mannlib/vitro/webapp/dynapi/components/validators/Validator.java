package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Removable;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;

public interface Validator extends Removable {

    boolean isValid(String name, Data data);

}
