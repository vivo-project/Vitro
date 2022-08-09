package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.List;


public class StringView implements View {

    public static String getFirstStringValue(DataStore dataStore, String name) {
    	RawData data = dataStore.getData(name);
    	if (data.getParam().isArray()) {
    		List array = (List) data.getObject();
    		Object first = array.get(0);
    		return first.toString();
    	}
    	return data.getObject().toString();
    	/*
		 * Map<String, RawData> submap = dataMap.entrySet().stream() .filter( value ->
		 * value.getValue() .getParam() .getType() .getImplementationType()
		 * .getClassName() .getCanonicalName() .equals("java.lang.String") )
		 * .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		 */
    }

}
