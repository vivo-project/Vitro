package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ProcedureDescriptor implements ParameterInfo{

    private String uri;
    private Parameters inputParams = new Parameters();
    private Parameters outputParams = new Parameters();
    private Parameters expectedParams = new Parameters();
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter")
    public void addInputParameter(Parameter param) {
        inputParams.add(param);
    }
    
    //TODO:SHACL validate that it has at least one parameter if used in context of inputProcedure or outputProcedure
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#providesParameter")
    public void addOutputParameter(Parameter param) {
        outputParams.add(param);
    }
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#expectedParameter")
    public void addExpectedParameter(Parameter param) {
        expectedParams.add(param);
    }
    
    public Parameters getExpectedParams() {
        return expectedParams;
    }
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#call", minOccurs = 1, maxOccurs = 1, asString = true)
    public void setCallUri(String uri) {
        this.uri = uri;
    }
    
    @Override
    public Parameters getInputParams() {
        return inputParams;
    }

    @Override
    public Parameters getOutputParams() {
        return outputParams;
    }
    
    public String getUri() {
        return uri;
    }
}