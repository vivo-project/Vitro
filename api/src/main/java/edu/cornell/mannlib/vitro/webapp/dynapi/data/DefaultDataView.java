package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;

public class DefaultDataView {

    public static void createDefaultOutput(DataStore dataStore, Parameters params) {
        for (String name : params.getNames()) {
            if (dataStore.contains(name)) {
                continue;
            }
            createData(params.get(name), dataStore);
        }
    }

    private static void createData(Parameter parameter, DataStore dataStore) {
        Data data = new Data(parameter);
        data.initializeDefault();
        dataStore.addData(parameter.getName(), data);
    }

}
