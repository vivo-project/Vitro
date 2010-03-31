package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.IOException;

public interface OntologyChangeLogger {

	public void log(String logMessage) throws IOException;
	
	public void logError(String errorMessage) throws IOException;
	
	public void closeLogs() throws IOException;
	
}
