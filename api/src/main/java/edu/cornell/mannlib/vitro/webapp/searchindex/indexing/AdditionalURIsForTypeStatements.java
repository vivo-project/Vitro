/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.indexing;

import java.util.Collections;
import java.util.List;

import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.searchindex.indexing.IndexingUriFinder;

/**
 * Adds URIs to index for type statement changes on individuals. 
 */
public class AdditionalURIsForTypeStatements implements IndexingUriFinder {

    @Override
    public List<String> findAdditionalURIsToIndex(Statement stmt) {        
        if( stmt != null && RDF.type.getURI().equals( stmt.getPredicate().getURI() )){
            return Collections.singletonList( stmt.getSubject().getURI() );
        }else{
            return Collections.emptyList();
        }
    }

    @Override
    public void startIndexing() { /* nothing to prepare */ }

    @Override
    public void endIndexing() { /* nothing to do */ }

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
    
}
