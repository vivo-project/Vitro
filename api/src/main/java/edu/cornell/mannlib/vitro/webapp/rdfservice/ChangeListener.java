/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice;

/**
 * An interface for listening to triples that are added to or removed
 * from the triple store, and other miscellaneous events.   
 */

public interface ChangeListener {		
	/**
	 * Override this to listen to each model change 
	 * 
	 * @param modelChange - the object representing the model change
	 */
	public void notifyModelChange(ModelChange modelChange);
		
	/**
	 * Override this to listen to events pertaining to the given graphURI. 
	 * 
	 * @param graphURI - the graph to which the event pertains
	 * @param event - the event that occurred. 
	 */
	public void notifyEvent(String graphURI, Object event);
}
