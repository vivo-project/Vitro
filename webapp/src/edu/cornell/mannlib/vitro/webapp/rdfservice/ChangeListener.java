/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice;

/**
 * An interface for listening to triples that are added to or removed
 * from the triple store, and other miscellaneous events.   
 */

public interface ChangeListener {		
	/**
	 * Override this to listen to all statements added to the RDF store. 
	 * 
	 * @param serializedTriple - the added statement in n3 format
	 * @param graphURI - the graph to which the statement was added
	 */
	public void addedStatement(String serializedTriple, String graphURI);
	
	/**
	 * Override this to listen to all statements removed from the RDF store. 
	 * 
	 * @param serializedTriple - the removed statement in n3 format
	 * @param graphURI - the graph from which the statement was removed
	 */
	public void removedStatement(String serializedTriple, String graphURI);
	
	/**
	 * Override this to listen to events pertaining to the given graphURI. 
	 * 
	 * @param graphURI - the graph to which the event pertains
	 * @param event - the event that occurred. 
	 */
	public void notifyEvent(String graphURI, Object event);
}
