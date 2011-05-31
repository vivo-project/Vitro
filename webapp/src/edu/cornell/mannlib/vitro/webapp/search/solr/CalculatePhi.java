/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import org.apache.solr.common.SolrInputDocument;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;

public class CalculatePhi implements DocumentModifier{
    CalculateBeta betas;

    // maybe Phi needs Beta?
    public CalculatePhi(CalculateBeta betas){
        this.betas = betas;        
    }
    
    @Override
    public void modifyDocument(Individual individual, SolrInputDocument doc) {
        // TODO Auto-generated method stub        
    }

}
