package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ParameterConverter;

public class RawData {

    private Set<String> types = null;
    private String string = null;
    private Object object = null;
    private Parameter param = null;

    public RawData(Parameter param){
        types = new HashSet<>();
        this.param = param;
    }
    
    protected void setObject(Object object) {
        this.object = object;
    }
    
    protected Object getObject() {
        return object;
    }
    
    protected Parameter getParam() {
    	return param;
    }
    
    public void setRawString(String raw) {
        this.string = raw;
    }
    
    protected String getRawString() {
        return string;
    }

	public void earlyInitialization() throws ConversionException {
		object = ParameterConverter.deserialize(param, string);
	}

	public Object getJsonValue() {
		return "";
	}
}
