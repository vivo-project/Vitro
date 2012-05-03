/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice;

/*
 * 
 * A listener that filters all its listening down to the single-statement level. Users of
 * this class override addedStatement(statement) and removedStatement(statement).
 *  
 */

public interface ChangeListener {
		
	/**
	 * Override this to listen to all statements added to the RDF store. 
	 * 
	 * @param String serializedTriple - the added statement
	 * @param RDFService.ModelSerializationFormat format - RDF format of serializedTriple
	 * @param String graphURI - the graph to which the statement was added
	 */
	public void addedStatement(String serializedTriple, RDFService.ModelSerializationFormat format, String graphURI);
	
	/**
	 * Override this to listen to all statements removed from the RDF store. 
	 * 
	 * @param String serializedTriple - the removed statement
	 * @param RDFService.ModelSerializationFormat format - RDF format of serializedTriple
	 * @param String graphURI - the graph from which the statement was removed
	 */
	public void removedStatement(String serializedTriple, RDFService.ModelSerializationFormat format, String graphURI);
	
	/**
	 * Override this to listen to events pertaining to the given graphURI. 
	 * 
	 * @param String graphURI - the graph to which the event pertains
	 * @param Object event - the event which occurred. 
	 */
	public void notifyEvent(String graphURI, Object event);
}
