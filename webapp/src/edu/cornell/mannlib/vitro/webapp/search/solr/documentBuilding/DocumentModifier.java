/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
/**
 * This interface represents an object that can add to a SearchInputDocument.
 */
public interface DocumentModifier {
    public void modifyDocument(Individual individual, SearchInputDocument doc, StringBuffer addUri) throws SkipIndividualException;
    
    //called to inform the DocumentModifier that the system is shutting down
    public void shutdown();
   
}
