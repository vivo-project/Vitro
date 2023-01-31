package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ProcedureDescriptor implements ParameterInfo {

    private String uri;
    private Parameters inputParams = new Parameters();
    private Parameters outputParams = new Parameters();
    private Parameter uriParam;

    public Parameter getUriParam() {
        return uriParam;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#callParameter", maxOccurs = 1)
    public void setUriParam(Parameter uriParam) {
        this.uriParam = uriParam;
        inputParams.add(uriParam);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter")
    public void addInputParameter(Parameter param) {
        inputParams.add(param);
    }

    // TODO:SHACL validate that it has at least one parameter if used in context
    // of inputProcedure or outputProcedure
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#providesParameter")
    public void addOutputParameter(Parameter param) {
        outputParams.add(param);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#call", minOccurs = 0, maxOccurs = 1, asString = true)
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

    public boolean isValid() {
        if (StringUtils.isBlank(uri) || uriParam != null) {
            return true;
        }
        if (!StringUtils.isBlank(uri) || uriParam == null) {
            return true;
        }
        return false;
    }

    public String toString() {
        if (uri == null) {
            return "Described procedure uri parameter:" + uriParam.getName() 
                    + " input parameters:" + String.join(",", inputParams.getNames())
                    + " output parameters:" + String.join(",", outputParams.getNames());
        }
        return "Described procedure uri:" + uri 
                + " input parameters:" + String.join(",", inputParams.getNames())
                + " output parameters:" + String.join(",", outputParams.getNames());
    }

    public boolean hasUriParam() {
        return uriParam != null;
    }

    public String getUri(DataStore dataStore) throws ConversionException {
        String name = uriParam.getName();
        Data uriData = dataStore.getData(name);
        if (uriData == null) {
            throw new ConversionException("Uri parameter is not found in DataStore.");
        }
        return uriData.getSerializedValue();      
    }
}