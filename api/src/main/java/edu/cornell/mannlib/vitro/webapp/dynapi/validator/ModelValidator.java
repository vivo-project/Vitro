package edu.cornell.mannlib.vitro.webapp.dynapi.validator;

public interface ModelValidator {

    boolean isValidResource(String uri, boolean deepCheck);

    boolean isValidFile(String path);
}
