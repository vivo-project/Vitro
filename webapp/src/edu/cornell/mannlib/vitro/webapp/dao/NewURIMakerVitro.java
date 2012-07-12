/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualDaoJena;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.NewURIMaker;

public class NewURIMakerVitro implements NewURIMaker {
    private static final Log log = LogFactory.getLog(NewURIMakerVitro.class.getName());

    private static final int MAX_ATTEMPTS = 20;
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
        log.debug("Before starting : Made URIs contains " + madeURIs.toString());
       
        while( ! goodNewURI && attempts < MAX_ATTEMPTS ){
            attempts++;
            
            if( attempts > 2 && prefixURI != null && !prefixURI.isEmpty() ) 
            {
            	log.debug("Attempts: " + attempts  + " and prefix not null and prefix not empty " + prefixURI);
                ind.setURI(prefixURI + random.nextInt() );
            }
            else {
            	log.debug("Attempts:" + attempts + " and setting uri to " + prefixURI);
                ind.setURI( prefixURI );
            }
            newURI = wdf.getIndividualDao().getUnusedURI( ind );
            log.debug("Created new uri " + newURI + " and does madeURIs contain it?" + madeURIs.contains(newURI));
            if( newURI != null && ! newURI.isEmpty() && ! madeURIs.contains( newURI) ){
            	log.debug("new URI is not null and new URI is empty and madeURIs does not containt new URI");
                goodNewURI = true;                
                madeURIs.add( newURI );                
            } 
            log.debug("Made URIs contains " + madeURIs.toString());
        }
        if(goodNewURI && newURI != null && !newURI.isEmpty()) {
        	log.debug("Decided on this URI " + newURI);
        
            return newURI;
        }
        else {
            log.error("An error occurred and URI could not be created for prefix " + prefixURI);
        	throw new InsertException("Could not get a new URI for the prefix " + prefixURI );
        	}
    }

}
