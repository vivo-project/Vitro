package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.BufferedWriter;
import java.io.File;
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
		
		Exception e = new Exception();
		StackTraceElement[] elements = e.getStackTrace();
		String className = ((StackTraceElement)elements[1]).getClassName();
		className = className.substring(className.lastIndexOf('.') + 1 );
		String methodName = ((StackTraceElement)elements[1]).getMethodName();
		
		logWriter.write(className + "." + methodName +  ":  " + logMessage + "\n");
		logWriter.flush();
	}

	public void logError(String errorMessage) throws IOException {

		Exception e = new Exception();
		StackTraceElement[] elements = e.getStackTrace();
		String className = ((StackTraceElement)elements[1]).getClassName();
		className = className.substring(className.lastIndexOf('.') + 1 );
		String methodName = ((StackTraceElement)elements[1]).getMethodName();
		int lineNumber = ((StackTraceElement)elements[1]).getLineNumber(); 

		errorWriter.write(className + "." + methodName +  " line " + lineNumber + ":  " + errorMessage + "\n");
		errorWriter.flush();
	}
	
	public void closeLogs() throws IOException {
		logWriter.flush();
		logWriter.close();
		errorWriter.flush();
		errorWriter.close();
	}
}
