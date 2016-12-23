/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.indexing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ContextModelsUser;

/**
 * if a class's classgroup changes, reindex all individuals in that class.  
 */
public class URIsForClassGroupChange  implements IndexingUriFinder, ContextModelsUser {
    
    IndividualDao indDao;

    @Override
	public void setContextModels(ContextModelAccess models) {
    	this.indDao = models.getWebappDaoFactory().getIndividualDao();
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
    public void endIndexing() {
        // Do nothing
        
    }

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
