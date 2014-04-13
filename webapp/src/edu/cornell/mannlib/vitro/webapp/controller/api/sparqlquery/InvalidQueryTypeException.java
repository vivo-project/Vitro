/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery;

/**
 * Indicates that the API can't process this type of query.
 */
public class InvalidQueryTypeException extends Exception {
	public InvalidQueryTypeException(String message) {
		super(message);
	}
}
