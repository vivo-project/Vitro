/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.NewURIMaker;

public class NewURIMakerVitro implements NewURIMaker {

    private static final int MAX_ATTEMPTS = 10;
    WebappDaoFactory wdf;
    Set<String> madeURIs = new HashSet<String>();
    static Random random = new Random();
    
    public NewURIMakerVitro( WebappDaoFactory wdf){
        this.wdf = wdf;
    }
    
    @Override
    public String getUnusedNewURI(String prefixURI) throws InsertException {        
        
        Individual ind = new IndividualImpl();
        String newURI = null;
        int attempts = 0;
        boolean goodNewURI = false;
        while( ! goodNewURI && attempts < MAX_ATTEMPTS ){
            attempts++;
            
            if( attempts > 2 && prefixURI != null && !prefixURI.isEmpty() )
                ind.setURI(prefixURI + random.nextInt() );
            else
                ind.setURI( prefixURI );
            
            newURI = wdf.getIndividualDao().getUnusedURI( ind );
            if( newURI != null && ! newURI.isEmpty() && ! madeURIs.contains( newURI) ){
                goodNewURI = true;                
                madeURIs.add( newURI );                
            }         
        }
        if( newURI != null && !newURI.isEmpty())
            return newURI;
        else
            throw new InsertException("Could not get a new URI for the prefix " + prefixURI );
    }

}
