/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import org.apache.solr.common.SolrInputDocument;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;

/**
 * This interface represents an object that can add to a SolrInputDocument.
 */
public interface DocumentModifier {
    public void modifyDocument(Individual individual, SolrInputDocument doc, StringBuffer addUri) throws SkipIndividualException;
    
    //called to inform the DocumentModifier that the system is shutting down
    public void shutdown();
   
}
