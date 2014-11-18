/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.documentBuilding;

import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.NAME_LOWERCASE;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.NAME_RAW;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.NAME_STEMMED;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.NAME_UNSTEMMED;

import java.util.Arrays;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputField;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Validation;

public class NameBoost implements DocumentModifier {

    /** 
     * These are the fields in the search Document that
     * are related to the name.  If you modify the schema,
     * please consider if you need to change this list
     * of name fields to boost. 
     */
    private String[] fieldsToBoost = {NAME_RAW,NAME_LOWERCASE,NAME_UNSTEMMED,NAME_STEMMED};
    
    private Float boost;
    
    @Property(uri="http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasBoost")
    public void setBoost(float boost) {
    	this.boost = boost;
    }
    
    @Validation
    public void validate() {
		if (boost == null) {
			throw new IllegalStateException(
					"Configuration did not include a boost value.");
		}

    }
    
    @Override
    public void modifyDocument(Individual individual, SearchInputDocument doc) {
        
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

	@Override
	public String toString() {
		return "NameBoost[fieldsToBoost=" + Arrays.toString(fieldsToBoost)
				+ ", boost=" + boost + "]";
	}

}
