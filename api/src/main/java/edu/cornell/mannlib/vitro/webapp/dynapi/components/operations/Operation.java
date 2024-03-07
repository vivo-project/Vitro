package edu.cornell.mannlib.vitro.webapp.dynapi.components.operations;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.DependencyInfo;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ParameterInfo;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.RunnableComponent;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;

public interface Operation extends RunnableComponent, ParameterInfo, DependencyInfo {

    public boolean isInputValid(DataStore dataStore);

    public boolean isOutputValid(DataStore dataStore);

    public boolean isValid();
}
