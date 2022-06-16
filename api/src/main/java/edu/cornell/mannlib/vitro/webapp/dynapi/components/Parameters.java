package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ArrayParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ObjectParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ParameterType;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;

public class Parameters implements Removable {

    private static final String ANY_URI="http://www.w3.org/2001/XMLSchema#anyURI";

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

    public Map<String, List<Literal>> getLiteralsMap(OperationData input){
        return getUrisParamStream(false)
        .collect(Collectors.toMap(
                param -> param.getName(),
                param -> Arrays.asList(
                        ResourceFactory.createTypedLiteral(input.get(param.getName())[0], param.getRDFDataType()))));
    }
    
    public Map<String, List<String>> getUrisMap(OperationData input){
        return getUrisParamStream(true)
        .collect(Collectors.toMap(
                param -> param.getName(), 
                param -> Arrays.asList(input.get(param.getName()))));
    }

    private Stream<Parameter> getUrisParamStream(boolean isUri) {
        return params.values().stream()
        .filter(value->value.getRDFDataType().getURI().equals(ANY_URI) == isUri);
    }
    
    public Parameter get(String name) {
        if (!name.contains(".")) {
            return params.get(name);
        } else {
            String fieldNameFirstPart = name.substring(0, name.indexOf("."));
            String fieldNameSecondPart = name.substring(name.indexOf(".") + 1);
            Parameter parameter = params.get(fieldNameFirstPart);
            if (parameter.getType() instanceof ObjectParameterType) {
                return ((ObjectParameterType) parameter.getType()).getInternalElements().get(fieldNameSecondPart);
            } else if (parameter.getType() instanceof ArrayParameterType) {
                String fieldNameOtherPart = fieldNameSecondPart.substring(fieldNameSecondPart.indexOf(".") + 1);
                if (StringUtils.isEmpty(fieldNameOtherPart))
                    return parameter;
                else {
                    ParameterType internalArrayParameterType = ((ArrayParameterType) parameter.getType()).getElementsType();
                    if (internalArrayParameterType instanceof ObjectParameterType)
                        return ((ObjectParameterType) internalArrayParameterType).getInternalElements().get(fieldNameOtherPart);
                }
            }
        }
        return null;
    }
    
    public int size() {
        return params.size();
    }
    
    public boolean contains(String name) {
        return params.containsKey(name);
    }

    // Substitute IRI parameters with their values in specific request
    public Map<String, List<String>> substituteIRIVariables(OperationData input){
        return params.values().stream()
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
