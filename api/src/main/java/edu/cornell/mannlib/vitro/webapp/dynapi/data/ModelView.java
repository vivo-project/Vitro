package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;

public class ModelView {

	private static final String MODEL_CANONICAL_NAME = Model.class.getCanonicalName();

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
		final ParameterType type = param.getType();
		final ImplementationType implementationType = type.getImplementationType();
		String name = implementationType.getClassName().getCanonicalName();
		return MODEL_CANONICAL_NAME.equals(name);
	}

	public static boolean hasModel(DataStore input, Parameter param) {
		String name = param.getName();
			if (!input.contains(name)) {
				return false;
			}
			Data data = input.getData(name);
			String className = data.getParam().getType().getImplementationType().getClassName().getCanonicalName();
			if (className.equals(MODEL_CANONICAL_NAME)) {
				return true;
			}
		return false;
	}
	
	public static List<Model> getExistingModels( Parameters params, DataStore dataStore ) {
		List<Model> list = new LinkedList<>();
		for (String name : params.getNames()) {
			if (!dataStore.contains(name)) {
				continue;
			}
			Data data = dataStore.getData(name);
			if (isModel(data)) {
				Model model = (Model) data.getObject();
				list.add(model);
			}
		}
		return list;
	}

	public static void addModel(DataStore dataStore, Model model, Parameter outputParam) {
		Data modelData = new Data(outputParam);
		modelData.setObject(model);
		dataStore.addData(outputParam.getName(), modelData);
	}

}
