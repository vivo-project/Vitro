/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.indexing;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_ASSERTIONS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;
import org.apache.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ContextModelsUser;

/**
 * If a class changes classgroups, then all members of that class
 * will have to be update in the search since the serach include
 * the clasgroup membership of all individuals.
 * 
 * Ex. when a statement like: 
 * sub='http://vivoweb.org/ontology/core#Summer&#39; 
 * pred='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#inClassGroup&#39; 
 * obj='http://vivoweb.org/ontology#vitroClassGrouppeople&#39; 
 * changes, all members of the class core:Summer need to be update so they get the new classgroup values. 
 */
public class AdditionalURIsForClassGroupChanges implements IndexingUriFinder, ContextModelsUser {

    private OntModel model;

	@Override
	public void setContextModels(ContextModelAccess models) {
		model = models.getOntModel(TBOX_ASSERTIONS);
	}

    @Override
    public List<String> findAdditionalURIsToIndex(Statement stmt) {
        if( stmt != null 
            && VitroVocabulary.IN_CLASSGROUP.equals( stmt.getPredicate().getURI() ) 
            && stmt.getSubject() != null ){
            // its a classgroup membership change for a class, 
            // update all individuals from the class.
            List<String> uris = new ArrayList<String>();
            model.enterCriticalSection(Lock.READ);
            try{
                StmtIterator iter = model.listStatements(null, RDF.type, stmt.getSubject());
                while( iter.hasNext() ){
                    Statement typeStmt = iter.nextStatement();
                    if( typeStmt != null && typeStmt.getSubject().isURIResource() ){
                        uris.add(typeStmt.getSubject().getURI());
                    }
                }
            }finally{
                model.leaveCriticalSection();
            }
            return uris;
        }else{
            return Collections.emptyList();
        }
    }
    
    @Override
    public void startIndexing() { /* nothing to prepare */ }

    @Override
    public void endIndexing() { /* nothing to do */ }

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
