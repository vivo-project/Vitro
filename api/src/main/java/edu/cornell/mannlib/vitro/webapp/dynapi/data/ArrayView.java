package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;

public class ArrayView {

    public static boolean isMultiValuedArray(DataStore dataStore, String propertyVar) {
        if (!isArray(dataStore, propertyVar)) {
            return false;
        }
        Data data = dataStore.getData(propertyVar);
        JsonContainer array = JsonContainerView.getJsonContainer(dataStore, data.getParam());
        List list = array.getDataAsStringList();
        if (list.size() > 1) {
            return true;
        }
        return false;
    }

    public static boolean isArray(DataStore dataStore, String paramName) {
        Data data = dataStore.getData(paramName);
        return JsonContainerView.isJsonArray(data.getParam());
    }

    public static List<String> getArray(Data data) {
        JsonContainer array = (JsonContainer) data.getObject();
        List<String> list = array.getDataAsStringList();
        return list;
    }

}
