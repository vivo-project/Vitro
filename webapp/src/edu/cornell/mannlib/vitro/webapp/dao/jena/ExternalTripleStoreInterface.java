/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import com.hp.hpl.jena.rdf.model.Statement;

public interface ExternalTripleStoreInterface {

	public abstract void addToExternalStore(Statement stmt) throws TripleStoreUnavailableException;
	
	public abstract void removeFromExternalStore(Statement stmt) throws TripleStoreUnavailableException; 

}
