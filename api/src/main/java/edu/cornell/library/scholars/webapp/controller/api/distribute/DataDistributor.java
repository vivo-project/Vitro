/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute;

import java.io.OutputStream;

/**
 * Life-cycle of a DataDistributor:
 * <ul>
 * <li>Instantiated to service a single HTTP request.</li>
 * <li>init() -- Called one time.</li>
 * <li>getContentType() -- Called one time.</li>
 * <li>writeOutput() -- Called one time.</li>
 * <li>close() -- Called exactly once.</li>
 * <li>Garbage-collected (probably)</li>
 * </ul>
 */
public interface DataDistributor {
    /**
     * Called exactly once, after instantiation is complete.
     * 
     * @param ddContext
     *            provides access to requrest parameters and triple-store
     *            content.
     * @throws DataDistributorException
     */
    void init(DataDistributorContext ddContext) throws DataDistributorException;

    /**
     * States the MIME type of the output from this instance. The MIME type may
     * be hardcoded, derived from the configuration, or derived from the request
     * parameters.
     * 
     * Called exactly once, after init().
     * 
     * @return the MIME type of the (expected) output.
     * @throws DataDistributorException
     */
    String getContentType() throws DataDistributorException;

    /**
     * Writes to the output stream (does not close it). This output will become
     * the body of the HTTP response.
     * 
     * Called no more than once, after getContentType().
     * 
     * Might not be called if a previous method throws an exception, or if the
     * content type is not acceptable to the requestor.
     * 
     * @param output
     * @throws DataDistributorException
     */
    void writeOutput(OutputStream output) throws DataDistributorException;

    /**
     * Called exactly once.
     * 
     * @throws DataDistributorException
     */
    void close() throws DataDistributorException;

    /**
     * A problem occurred while creating or running the DataDistributor.
     */
    public class DataDistributorException extends Exception {
        public DataDistributorException() {
            super();
        }

        public DataDistributorException(String message) {
            super(message);
        }

        public DataDistributorException(Throwable cause) {
            super(cause);
        }

        public DataDistributorException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * The controller could not find a DataDistributor configured for the
     * requested action.
     */
    public class NoSuchActionException extends DataDistributorException {
        public NoSuchActionException() {
            super();
        }

        public NoSuchActionException(String message) {
            super(message);
        }

        public NoSuchActionException(Throwable cause) {
            super(cause);
        }

        public NoSuchActionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * The user registered to the current HTTP session may not run the
     * DataDistributor for the requested action.
     * 
     * @see DataDistributorContext#isAuthorized(edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest)
     */
    public class NotAuthorizedException extends DataDistributorException {
        public NotAuthorizedException() {
            super();
        }

        public NotAuthorizedException(String message) {
            super(message);
        }

        public NotAuthorizedException(Throwable cause) {
            super(cause);
        }

        public NotAuthorizedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * The HTTP request did not contain one or more required parameters.
     */
    public class MissingParametersException extends DataDistributorException {
        public MissingParametersException() {
            super();
        }

        public MissingParametersException(String message) {
            super(message);
        }

        public MissingParametersException(Throwable cause) {
            super(cause);
        }

        public MissingParametersException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * A problem occurred while creating the output.
     */
    public class ActionFailedException extends DataDistributorException {
        public ActionFailedException() {
            super();
        }

        public ActionFailedException(String message) {
            super(message);
        }

        public ActionFailedException(Throwable cause) {
            super(cause);
        }

        public ActionFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
