package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Parameters implements Removable {

	private Map<String, Parameter> params;

	public Parameters() {
		params = new HashMap<String, Parameter>();
	}

	public void add(Parameter param) {
		params.put(param.getName(), param);
	}

	public void addAll(Parameters newParams) {
		Set<String> names = newParams.getNames();
		for (String name : names) {
			add(newParams.get(name));
		}
	}

	public void removeAll(Parameters toRemove) {
		Set<String> names = toRemove.getNames();
		for (String name : names) {
			params.remove(name);
		}
	}

	public Set<String> getNames() {
		return params.keySet();
	}

	public Parameter get(String name) {
		return params.get(name);
	}

	public int size() {
		return params.size();
	}

	public boolean contains(String name) {
		return params.containsKey(name);
	}

	/*
	 * // Substitute IRI parameters with their values in specific request public
	 * Map<String, List<String>> substituteIRIVariables(DataStore input) { return
	 * params.values().stream() .filter(value -> value.getType().isUri()).collect(
	 * Collectors.toMap(param -> param.getName(), param ->
	 * Arrays.asList(input.get(param.getName())))); }
	 * 
	 * // Substitute parameters that represent RDF literals with their values in //
	 * specific request public Map<String, List<Literal>>
	 * substituteLiteralVariables(DataStore input) { return params.values().stream()
	 * .filter(value -> value.getType().isLiteral()) .collect(Collectors.toMap(param
	 * -> param.getName(), param ->
	 * Arrays.asList(ResourceFactory.createTypedLiteral(input.get(param.getName())[0
	 * ], param.getType().getRdfType().getRDFDataType())))); }
	 */

	@Override
	public void dereference() {
		for (String name : params.keySet()) {
			params.get(name).dereference();
		}
		params = null;
	}

	public Parameter getFirst() {
		return params.entrySet().iterator().next().getValue();
	}

}
