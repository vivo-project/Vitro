/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.beans;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Interface to use with IndexBuilder to find more URIs to index given a changed statement.
 * The statement may have been added or removed from the model.  
 */
public interface StatementToURIsToUpdate {
    
    /**
     * For the domain that is the responsibility of the given implementation,
     * calculate the URIs that need to be updated in the search index.  
     * The URIs in the list will be updated by the IndexBuilder, which will
     * handle URIs of new individuals, URIs of individuals that have changes,
     * and URIs of individuals that have been removed from the model.
     *   
     * @return List of URIs.
     */
    List<String> findAdditionalURIsToIndex(Statement stmt);
    
    void startIndexing();
    
    void endIndxing();
}
