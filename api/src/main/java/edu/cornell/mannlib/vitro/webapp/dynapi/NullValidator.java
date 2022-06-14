package edu.cornell.mannlib.vitro.webapp.dynapi;

public class NullValidator implements ModelValidator {

    private static NullValidator INSTANCE = new NullValidator();

    public static NullValidator getInstance() {
        return INSTANCE;
    }

    private NullValidator(){

    }

    @Override
    public boolean isValidResource(String uri){
        return true;
    }

    @Override
    public boolean isValidFile(String path){
        return true;
    }

}
