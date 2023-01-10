package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

public class NullParameterType extends ParameterType{
    
    private static NullParameterType INSTANCE = new NullParameterType();
    
    public static NullParameterType getInstance() {
        return INSTANCE;
    }
    
    private NullParameterType() {}

}
