package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.LinkedList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;

public class SimpleDataView {

	public static List<String> getNames(Parameters params) {
		List<String> result = new LinkedList<>();
		for (String name : params.getNames()) {
			Parameter param = params.get(name);
			if (!param.isArray() && !param.isJsonObject()) {
				result.add(name);
			}
		}
		return result;
	}
	
	public static String getStringRepresentation(String name, DataStore store){
		final Data data = store.getData(name);
		return data.getObject().toString();
	}
	
	public static String getStringRepresentation( Data data){
		return data.getObject().toString();
	}

}
