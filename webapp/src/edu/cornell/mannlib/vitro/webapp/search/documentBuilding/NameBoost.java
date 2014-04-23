/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.documentBuilding;

import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.NAME_LOWERCASE;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.NAME_RAW;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.NAME_STEMMED;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.NAME_UNSTEMMED;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputField;

public class NameBoost implements DocumentModifier {

    /** 
     * These are the fields in the search Document that
     * are related to the name.  If you modify the schema,
     * please consider if you need to change this list
     * of name fields to boost. 
     */
    String[] fieldsToBoost = {NAME_RAW,NAME_LOWERCASE,NAME_UNSTEMMED,NAME_STEMMED};
    
    
    final float boost;
    
    public NameBoost(float boost){
        this.boost = boost;
    }
    
    @Override
    public void modifyDocument(Individual individual, SearchInputDocument doc,
            StringBuffer addUri) {
        
        for( String fieldName : fieldsToBoost){
            SearchInputField field = doc.getField(fieldName);
            if( field != null ){                                
                field.setBoost(field.getBoost() + boost);
            }
        }        
    }

    @Override
    public void shutdown() {
        // do nothing.
    }

}
