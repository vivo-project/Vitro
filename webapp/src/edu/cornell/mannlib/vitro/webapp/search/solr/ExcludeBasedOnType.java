/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.search.solr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;

/**
 * Exclude individual from search index if
 * it is a member of any of the the types.
 * @author bdc34
 *
 */
public class ExcludeBasedOnType implements SearchIndexExcluder {

    List<String> typeURIs;
    
    public ExcludeBasedOnType(String ... typeURIs) {    
        setExcludedTypes( typeURIs );
    }

    @Override
    public String checkForExclusion(Individual ind) { 
        if( ind != null ) {                    
            List<VClass> vclasses = ind.getVClasses();
            if( vclasses != null && ! Collections.disjoint(vclasses, typeURIs)  ){
                return("skipping due to type.");             
            }        
        }
        return null;
    }
        
    public void setExcludedTypes(String ... typeURIs){        
        setExcludedTypes(Arrays.asList(typeURIs));         
    }
    
    public void setExcludedTypes(List<String> typeURIs){
        synchronized(this){
            this.typeURIs = new ArrayList<String>(typeURIs);            
        }
    }
    
    protected void addTypeToExclude(String typeURI){
        if( typeURI != null && !typeURI.isEmpty()){
            synchronized(this){
                typeURIs.add(typeURI);
            }
        }
    }
    
    protected void removeTypeToExclude(String typeURI){        
        synchronized(this){
            typeURIs.remove(typeURI);
        }
    }
}
