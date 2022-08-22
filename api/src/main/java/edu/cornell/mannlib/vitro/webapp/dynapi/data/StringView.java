package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.List;


public class StringView {

    public static String getFirstStringValue(DataStore dataStore, String name) {
    	Data data = dataStore.getData(name);
    	if (data.getParam().isArray()) {
    		List array = (List) data.getObject();
    		Object first = array.get(0);
    		return first.toString();
    	}
    	return data.getObject().toString();
    }

}
