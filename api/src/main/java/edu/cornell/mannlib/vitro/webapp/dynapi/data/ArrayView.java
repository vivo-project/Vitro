package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;

public class ArrayView {

	public static Map<String,List> getSingleDimensionalArrays(Parameters params) {
		Map<String,List> result = new HashMap<>();
		for (String name : params.getNames()) {
			Parameter param = params.get(name);
			if (param.isArray() && !param.getType().getValuesType().isArray()) {
				result.put(name, new LinkedList());
			}
		}
		return result;
	}

	public static boolean isMultiValuedArray(DataStore dataStore, String propertyVar) {
		if (!isArray(dataStore, propertyVar)) {
			return false;
		}
		Data data = dataStore.getData(propertyVar);
		List list = (List) data.getObject();
		if (list.size() > 1) {
			return true;
		}
		return false;
	}

	public static boolean isArray(DataStore dataStore, String propertyVar) {
		Data data = dataStore.getData(propertyVar);
		return data.getParam().isArray();
	}
	
	public static List getArray(Data data) {
		return (List) data.getObject();
	}

}
