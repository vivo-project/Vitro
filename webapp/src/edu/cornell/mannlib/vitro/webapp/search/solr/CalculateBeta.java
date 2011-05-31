/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import org.apache.solr.common.SolrInputDocument;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;

public class CalculateBeta implements DocumentModifier{

    @Override
    public void modifyDocument(Individual individual, SolrInputDocument doc) {
        // TODO Auto-generated method stub
        
        /*
          may want to do something like:
          
          Field f = doc.getField(termALLTEXTUSTEMMED);
          f.setBoost( beta * f.getBoost() )
                    
          
         */
    }

}
