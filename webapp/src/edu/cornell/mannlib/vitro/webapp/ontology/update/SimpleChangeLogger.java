/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleChangeLogger implements ChangeLogger {

	private Writer logWriter;
	private Writer errorWriter;
	
	private boolean errorsWritten = false;
	
	public SimpleChangeLogger( String logPath, String errorPath ) {
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
		//String methodName = ((StackTraceElement)elements[1]).getMethodName();
		//logWriter.write(className + "." + methodName +  ":  " + logMessage + "\n\n");
		
		Date now = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		logWriter.write(formatter.format(now) + " " + className + ":  " + logMessage + "\n\n");
		logWriter.flush();		
	}

	public void logWithDate(String logMessage) throws IOException {
		
		Exception e = new Exception();
		StackTraceElement[] elements = e.getStackTrace();
		String className = ((StackTraceElement)elements[1]).getClassName();
		className = className.substring(className.lastIndexOf('.') + 1 );
		Date now = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy hh:mm a z");
		logWriter.write(formatter.format(now) + " " + className + ":  " + logMessage + "\n\n");
		logWriter.flush();		
	}

	public void logError(String errorMessage) throws IOException {

		errorsWritten = true;
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
	
	public boolean errorsWritten() {
		return errorsWritten;
	}
}
