package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.Map;

import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;

public class ModelView implements View {

	private static final String ORG_APACHE_JENA_RDF_MODEL_MODEL = "org.apache.jena.rdf.model.Model";
	private Map<String, RawData> dataMap;

	public ModelView(Map<String, RawData> dataMap) {
		this.dataMap = dataMap;
	}

	public Model get(String name) {
		RawData object = dataMap.get(name);
		if (isModel(object)) {
			return null;
		} else {
			return null;
		}
	}

	private boolean isModel(RawData object) {
		Parameter param = object.getParam();
		return false;
	}

	public static Model getFirstModel(DataStore input, Parameters params) {
		for (String name : params.getNames()) {
			RawData data = input.getData(name);
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
}
