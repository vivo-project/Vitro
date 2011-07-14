/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.beans;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Interface to use with IndexBuilder to find more URIs to index given a changed statement.
 * The stmt may have been added or removed from the model. 
 */
public interface AdditionalURIsToIndex {
    List<String> findAdditionalURIsToIndex(Statement stmt);
}
