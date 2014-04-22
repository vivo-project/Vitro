/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.search.documentBuilding;

import java.util.Arrays;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;

/**
 * Skip individual if its URI is from any of the excludeNamepsaces
 * 
 */
public class ExcludeBasedOnNamespace implements SearchIndexExcluder {

    List<String> excludeNamepsaces;
    
    
    public ExcludeBasedOnNamespace(String ... excludeNamepsaces) {
        super();
        this.excludeNamepsaces = Arrays.asList(excludeNamepsaces);
    }


    @Override
    public String checkForExclusion(Individual ind) {
        for( String ns: excludeNamepsaces){
            if( ns.equals( ind.getNamespace() ) ){
                return "skipping because of namespace " ;
            }                
        }
        return null;
    } 

}
