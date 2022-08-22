package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;

public class RdfView {

	public static Map<String, List<Literal>> getLiteralsMap(DataStore dataStore, Parameters params) {
		Map<String, List<Literal>> result = new HashMap<>();
		for (String name : params.getNames()) {
			Parameter param = params.get(name);
			if (param.getType().isLiteral()) {
				Data data = dataStore.getData(name);
				Literal literal = ResourceFactory.createTypedLiteral(data.getObject().toString(),
						param.getType().getRdfType().getRDFDataType());
				List<Literal> list = Collections.singletonList( literal );
				result.put(name, list);
			} else if( param.isArray() && param.getType().getValuesType().isLiteral()) {
				Data data = dataStore.getData(name);
				List objList = (List) data.getObject();
				List<Literal> list = new LinkedList<>();
				for (Object object : objList) {
					Literal literal = ResourceFactory.createTypedLiteral(object.toString(),
							param.getType().getValuesType().getRdfType().getRDFDataType());
					list.add(literal);
				}
				result.put(name, list);
			}
		}
		return result;
	}

	public static Map<String, List<String>> getUrisMap(DataStore dataStore, Parameters params) {
		Map<String, List<String>> result = new HashMap<>();
		for (String name : params.getNames()) {
			Parameter param = params.get(name);
			if (param.getType().isUri()) {
				Data data = dataStore.getData(name);
				List<String> list = Collections.singletonList( data.getObject().toString() );
				result.put(name, list);
			} else if( param.isArray() && param.getType().getValuesType().isUri()) {
				Data data = dataStore.getData(name);
				List objList = (List) data.getObject();
				List<String> list = new LinkedList<>();
				for (Object object : objList) {
					list.add(object.toString());
				}
				result.put(name, list);
			}
		}
		return result;
	}
	
	public static List<String> getLiteralNames(Parameters params){
		List<String> result = new LinkedList<>();
		for (String name : params.getNames()) {
			if (params.get(name).getType().isLiteral()) {
				result.add(name);
			}
		}
		return result;
	}

	public static List<String> getUriNames(Parameters params) {
		List<String> result = new LinkedList<>();
		for (String name : params.getNames()) {
			if (params.get(name).getType().isUri()) {
				result.add(name);
			}
		}
		return result;
	}

}
