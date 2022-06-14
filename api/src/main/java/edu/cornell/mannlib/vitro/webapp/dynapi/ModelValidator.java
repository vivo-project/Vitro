package edu.cornell.mannlib.vitro.webapp.dynapi;

public interface ModelValidator {

    boolean isValidResource(String uri);

    boolean isValidFile(String path);
}
