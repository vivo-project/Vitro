/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api;

/**
 * When you try to use an API that you aren't authorized for, we don't redirect
 * you to the login page. We complain.
 */
public class NotAuthorizedToUseApiException extends Exception {
	public NotAuthorizedToUseApiException(String message) {
		super(message);
	}

	public NotAuthorizedToUseApiException(Throwable cause) {
		super(cause);
	}

	public NotAuthorizedToUseApiException(String message, Throwable cause) {
		super(message, cause);
	}

}
