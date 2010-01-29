package edu.cornell.mannlib.vitro.webapp.dao.jena;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import com.hp.hpl.jena.rdf.model.Statement;

public interface ExternalTripleStoreInterface {

	public abstract void addToExternalStore(Statement stmt) throws TripleStoreUnavailableException;
	
	public abstract void removeFromExternalStore(Statement stmt) throws TripleStoreUnavailableException; 

}
