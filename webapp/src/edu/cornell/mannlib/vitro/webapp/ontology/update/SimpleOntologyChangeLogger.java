package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class SimpleOntologyChangeLogger implements OntologyChangeLogger {

	private Writer logWriter;
	private Writer errorWriter;
	
	public SimpleOntologyChangeLogger( String logPath, 
									   String errorPath ) {
		File logFile = new File(logPath);
		File errorFile = new File(errorPath);
		try {
			logWriter = new BufferedWriter(new FileWriter(logFile));
			errorWriter = new BufferedWriter(new FileWriter(errorFile));
		} catch (IOException ioe) {
			throw new RuntimeException ("Unable to open ontology change log " +
										"files for writing", ioe);
		}
	}
					
	
	public void log(String logMessage) throws IOException {
		//TODO get calling method info from stack and include in message
		logWriter.write(logMessage + "\n");
	}

	public void logError(String errorMessage) throws IOException {
		//TODO get calling method info from stack and include in message
		errorWriter.write(errorMessage + "\n");
	}
	
	public void closeLogs() throws IOException {
		logWriter.flush();
		logWriter.close();
		errorWriter.flush();
		errorWriter.close();
	}

}
