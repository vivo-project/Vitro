package edu.cornell.mannlib.vitro.webapp.dynapi.components.conditions;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.DependencyInfo;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;

public interface Condition extends DependencyInfo {

    public boolean isSatisfied(DataStore input);

    public Parameters getInputParams();

}
