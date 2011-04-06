/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces;

/* Represents a request to perform an action.    */
public interface RequestedAction {
	/**
	 * In its most basic form, a RequestAction needs to have an identifier.
	 * Sometimes this will be enough. For example ServerStatusRequest.
	 * 
	 * @return
	 */
	public String getURI();

}
