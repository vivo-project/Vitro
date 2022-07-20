package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.Map;

import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;

public class ModelView {

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
}
