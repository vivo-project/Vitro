/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * Exclude individuals with types from the Vitro namespace from the
 * search index. (Other than old vitro Flag types).
 */
public class ExcludeNonFlagVitro implements SearchIndexExcluder {

    @Override
    public String checkForExclusion(Individual ind) {            
        if( ind != null && ind.getVClasses() != null ) {                                
            String excludeMsg = skipIfVitro(ind,  ind.getVClasses() );
            if( excludeMsg != null)
                return excludeMsg;
        }
        return null;
    }

    String skipIfVitro(Individual ind, List<VClass> vclasses) {
        for( VClass type: vclasses ){
            if( type != null && type.getURI() != null ){                
                String typeURI = type.getURI();
                
                if(typeURI.startsWith( VitroVocabulary.vitroURI ) 
                   && ! typeURI.startsWith(VitroVocabulary.vitroURI + "Flag") ){
                    
                    return "Skipped " + ind.getURI()+" because in " 
                            + VitroVocabulary.vitroURI + " namespace";
                }
            }
        }   
        return null;
    }
    
}
