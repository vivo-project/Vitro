package edu.cornell.mannlib.vitro.webapp.dynapi;

public class NullValidator implements ModelValidator {

    public boolean isValid(String uri){
        return true;
    }

}
