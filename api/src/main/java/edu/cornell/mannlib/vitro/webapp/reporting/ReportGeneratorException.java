/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reporting;

/**
 * A problem occurred while creating or running the ReportGenerator.
 */
public class ReportGeneratorException extends Exception {
    public ReportGeneratorException() {
        super();
    }

    public ReportGeneratorException(String message) {
        super(message);
    }

    public ReportGeneratorException(Throwable cause) {
        super(cause);
    }

    public ReportGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }
}
