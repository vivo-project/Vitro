/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.distribute;

import java.io.OutputStream;

/**
 * <pre>
 * Life-cycle of a DataDistributor:
 * -- instantiated
 * -- init()
 * -- getActionName(), writeOutput(), getContentType()
 *      In any order. Might not be called. Never called more than once.
 * -- close()
 * -- garbage-collected 
 *      (probably)
 * </pre>
 */
public interface DataDistributor {
	void init(DataDistributorContext ddContext) throws DataDistributorException;

	String getActionName();

	String getContentType() throws DataDistributorException;

	void writeOutput(OutputStream output) throws DataDistributorException;

	void close() throws DataDistributorException;

	public class DataDistributorException extends Exception {
		public DataDistributorException() {	super(); }
		public DataDistributorException(String message) { super(message); }
		public DataDistributorException(Throwable cause) { super(cause); }
		public DataDistributorException(String message, Throwable cause) { super(message, cause); }
	}

	public class NoSuchActionException extends DataDistributorException {
		public NoSuchActionException() { super(); }
		public NoSuchActionException(String message) { super(message); }
		public NoSuchActionException(Throwable cause) { super(cause); }
		public NoSuchActionException(String message, Throwable cause) { super(message, cause); }
	}

	public class NotAuthorizedException extends DataDistributorException {
		public NotAuthorizedException() { super(); }
		public NotAuthorizedException(String message) { super(message); }
		public NotAuthorizedException(Throwable cause) { super(cause); }
		public NotAuthorizedException(String message, Throwable cause) { super(message, cause); }
	}

	public class MissingParametersException extends DataDistributorException {
		public MissingParametersException() { super(); }
		public MissingParametersException(String message) { super(message); }
		public MissingParametersException(Throwable cause) { super(cause); }
		public MissingParametersException(String message, Throwable cause) { super(message, cause); }
	}

	public class ActionFailedException extends DataDistributorException {
		public ActionFailedException() { super(); }
		public ActionFailedException(String message) { super(message); }
		public ActionFailedException(Throwable cause) { super(cause); }
		public ActionFailedException(String message, Throwable cause) { super(message, cause); }
	}
}
