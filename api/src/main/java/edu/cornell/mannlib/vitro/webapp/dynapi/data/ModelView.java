package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;

public class ModelView implements View {

	private static final String ORG_APACHE_JENA_RDF_MODEL_MODEL = "org.apache.jena.rdf.model.Model";
	private static final String MODEL_CANONICAL_NAME = "org.apache.jena.rdf.model.Model";
	private Map<String, Data> dataMap;

	public ModelView(Map<String, Data> dataMap) {
		this.dataMap = dataMap;
	}

	public Model get(String name) {
		Data object = dataMap.get(name);
		if (isModel(object)) {
			return null;
		} else {
			return null;
		}
	}

	public static boolean isModel(Data object) {
		Parameter param = object.getParam();
		String name = param.getType().getImplementationType().getClassName().getCanonicalName();
		return MODEL_CANONICAL_NAME.equals(name);
	}
	
	public static boolean isModel(Parameter param) {
		String name = param.getType().getImplementationType().getClassName().getCanonicalName();
		return MODEL_CANONICAL_NAME.equals(name);
	}

	public static Model getFirstModel(DataStore input, Parameters params) {
		for (String name : params.getNames()) {
			Data data = input.getData(name);
			String className = data.getParam().getType().getImplementationType().getClassName().getCanonicalName();
			if (className.equals(ORG_APACHE_JENA_RDF_MODEL_MODEL)) {
				return (Model) data.getObject();
			}
		}
		String dataStoreNames = String.join(",", input.keySet());
		String parameterNames = String.join(",", params.getNames());
		throw new ObjectNotFoundException("Model not found in data store: "
				+ dataStoreNames + "\n" + " Parameters: " + parameterNames);
	}

	public static List<Model> getModels( Parameters params, DataStore dataStore ) {
		List<Model> list = new LinkedList<>();
		for (String name : params.getNames()) {
			Data data = dataStore.getData(name);
			if (isModel(data)) {
				Model model = (Model) data.getObject();
				list.add(model);
			}
		}
		return list;
	}

	public static Model getModel(Parameter param, DataStore dataStore) {
		String name = param.getName();
		Data data = dataStore.getData(name);
		return (Model) data.getObject();
	}
}
