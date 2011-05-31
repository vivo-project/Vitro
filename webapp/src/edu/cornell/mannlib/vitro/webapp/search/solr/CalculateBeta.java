/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import java.util.Hashtable;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.search.VitroTermNames;

public class CalculateBeta implements DocumentModifier{
    private static final String[] fieldsToAddBetaTo = {
        VitroTermNames.NAME_RAW,
        VitroTermNames.NAME_LOWERCASE,
        VitroTermNames.NAME_UNSTEMMED,
        VitroTermNames.NAME_STEMMED
    };
    
    private static final String[] fieldsToMultiplyBetaBy = {
        VitroTermNames.ALLTEXT,
        VitroTermNames.ALLTEXTUNSTEMMED,                
    };
    
    Model fullModel;
    int totalInd;
    public static Map<String,Float> betas = new Hashtable<String,Float>();
    
    public CalculateBeta(OntModel fullModel){
        this.fullModel=fullModel;
        this.totalInd = fullModel.listIndividuals().toList().size();
    }
    
    @Override
    public void modifyDocument(Individual individual, SolrInputDocument doc) {
        // TODO Auto-generated method stub
    
        // get beta value       
        float beta = 0;
        if(betas.containsKey(individual.getURI())){
            beta = betas.get(individual.getURI());
        }else{
            beta = calculateBeta(individual.getURI()); // or calculate & put in map
            betas.put(individual.getURI(), beta);
        }
        //doc.addField(term.BETA,beta);
        
        for(String term: fieldsToAddBetaTo){
            SolrInputField f = doc.getField( term );
            f.setBoost( beta + f.getBoost() );
        }
        
        for(String term: fieldsToMultiplyBetaBy){
            SolrInputField f = doc.getField( term );
            f.setBoost( beta * f.getBoost() );
        }
                
        doc.setDocumentBoost( beta * doc.getDocumentBoost() );
    }

    public float calculateBeta(String uri){
        float beta=0;
        RDFNode node = (Resource) fullModel.getResource(uri); 
        StmtIterator stmtItr = fullModel.listStatements((Resource)null, (Property)null,node);
        int Conn = stmtItr.toList().size();
        beta = (float)Conn/totalInd;
        beta *= 100;
        beta += 1;
        return beta; 
    }
    
    public Float getBeta(String uri){
        return betas.get(uri);
    }
}
