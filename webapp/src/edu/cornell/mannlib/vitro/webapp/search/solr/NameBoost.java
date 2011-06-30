/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;

public class NameBoost implements DocumentModifier {

    static VitroSearchTermNames term = new VitroSearchTermNames();
    String[] fieldsToBoost = {term.NAME_RAW,term.NAME_LOWERCASE,term.NAME_UNSTEMMED,term.NAME_STEMMED};
    
    static final float NAME_BOOST = (float) 1.2;
    
    @Override
    public void modifyDocument(Individual individual, SolrInputDocument doc,
            StringBuffer addUri) {
        
        for( String fieldName : fieldsToBoost){
            SolrInputField field = doc.getField(fieldName);
            if( field != null )
                field.setBoost(field.getBoost() * NAME_BOOST);            
        }        
    }

    @Override
    public void shutdown() {
        // do nothing.
    }

}
