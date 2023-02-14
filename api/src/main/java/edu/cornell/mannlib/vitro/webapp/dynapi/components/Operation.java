package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;

public interface Operation extends RunnableComponent, ParameterInfo, DependencyInfo {

    public boolean isInputValid(DataStore dataStore);
    
    public boolean isOutputValid(DataStore dataStore);
    
    public boolean isValid();
}
