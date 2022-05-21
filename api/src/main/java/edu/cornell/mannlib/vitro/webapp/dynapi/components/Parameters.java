package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;

public class Parameters implements Removable {

    private static final String ANY_URI="http://www.w3.org/2001/XMLSchema#anyURI";

    private Map<String, Parameter> params;

    public Parameters() {
        params = new HashMap<String, Parameter>();
    }

    public void add(Parameter param) {
        params.put(param.getName(), param);
    }

    public Set<String> getNames() {
        return params.keySet();
    }

    public Map<String, Parameter> getParameters() { return params; }

    public Parameter get(String name) {
        return params.get(name);
    }

    // Substitute IRI parameters with their values in specific request
    public Map<String, List<String>> substituteIRIVariables(OperationData input){
        return getParameters().values().stream()
                .filter(value->value.getRDFDataType().getURI().equals(ANY_URI))
                .collect(Collectors.toMap(param -> param.getName(), param -> Arrays.asList(input.get(param.getName()))));
    }

    // Substitute parameters that represent RDF literals with their values in specific request
    public Map<String, List<Literal>> substituteLiteralVariables(OperationData input){
        return params.values().stream()
                .filter(value->!value.getRDFDataType().getURI().equals(ANY_URI))
                .collect(Collectors.toMap(
                        param -> param.getName(),
                        param -> Arrays.asList(ResourceFactory.createTypedLiteral(
                                input.get(param.getName())[0],
                                param.getRDFDataType()
                                )
                        )));
    }

    @Override
    public void dereference() {
        for (String name : params.keySet()) {
            params.get(name).dereference();
        }
        params = null;
    }

}
