/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.search.beans.StatementToURIsToUpdate;

/**
 * if a class's classgroup changes, reindex all individuals in that class.  
 */
public class URIsForClassGroupChange  implements StatementToURIsToUpdate {
    
    IndividualDao indDao;
    
    public URIsForClassGroupChange(IndividualDao individualDao){
        this.indDao = individualDao;
    }
    
    @Override
    public List<String> findAdditionalURIsToIndex(Statement stmt) {
        if( stmt == null || stmt.getPredicate() == null) 
            return Collections.emptyList();
        
        //if it is a change in classgroup of a class
        if( VitroVocabulary.IN_CLASSGROUP.equals( stmt.getPredicate().getURI() ) &&
            stmt.getSubject() != null && 
            stmt.getSubject().isURIResource() ){
            
            //get individuals in class
            List<Individual>indsInClass = 
                indDao.getIndividualsByVClassURI(stmt.getSubject().getURI());
            if( indsInClass == null )
                return Collections.emptyList();
            
            //convert individuals to list of uris
            List<String> uris = new ArrayList<String>();
            for( Individual ind : indsInClass){
                uris.add( ind.getURI() );
            }
            return uris;
        }else{
            return Collections.emptyList();
        }
    }

    @Override
    public void startIndexing() {
        // Do nothing
        
    }

    @Override
    public void endIndxing() {
        // Do nothing
        
    }

}
