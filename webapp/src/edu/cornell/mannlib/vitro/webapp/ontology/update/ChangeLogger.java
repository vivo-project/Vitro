/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.IOException;

public interface ChangeLogger {

	public void log(String logMessage) throws IOException;
	
	public void logWithDate(String logMessage) throws IOException;
	
	public void logError(String errorMessage) throws IOException;
	
	public void closeLogs() throws IOException;
	
	public boolean errorsWritten();
	
}
