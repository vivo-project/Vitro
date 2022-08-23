package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;

public class ModelView {

	private static final String ORG_APACHE_JENA_RDF_MODEL_MODEL = "org.apache.jena.rdf.model.Model";
	private static final String MODEL_CANONICAL_NAME = "org.apache.jena.rdf.model.Model";

	public static Model getModel(DataStore dataStore, Parameter param) {
		String name = param.getName();
		Data data = dataStore.getData(name);
		if (isModel(data)) {
			return (Model) data.getObject();
		}
		String dataStoreNames = String.join(",", dataStore.keySet());
		throw new ObjectNotFoundException("Model " + name + "not found in data store."
				+ dataStoreNames + "\n" + " Parameter: " + name);
	}

	public static boolean isModel(Data object) {
		Parameter param = object.getParam();
		return isModel(param);
	}
	
	public static boolean isModel(Parameter param) {
		String name = param.getType().getImplementationType().getClassName().getCanonicalName();
		return MODEL_CANONICAL_NAME.equals(name);
	}

	public static boolean hasModel(DataStore input, Parameter param) {
		String name = param.getName();
			if (!input.contains(name)) {
				return false;
			}
			Data data = input.getData(name);
			String className = data.getParam().getType().getImplementationType().getClassName().getCanonicalName();
			if (className.equals(ORG_APACHE_JENA_RDF_MODEL_MODEL)) {
				return true;
			}
		return false;
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

}
