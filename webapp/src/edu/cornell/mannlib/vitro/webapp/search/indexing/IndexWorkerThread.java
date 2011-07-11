/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;
import edu.cornell.mannlib.vitro.webapp.search.solr.IndividualToSolrDocument;

class IndexWorkerThread extends Thread{
	
    protected final int threadNum;
	protected IndividualToSolrDocument individualToSolrDoc;
	protected final IndexerIface indexer;
	protected final Iterator<Individual> individualsToIndex;
	protected boolean stopRequested = false;
	
	private Log log = LogFactory.getLog(IndexWorkerThread.class);
	private static long count=0;		
	private static long starttime = 0;		
	
	public IndexWorkerThread(IndexerIface indexer, int threadNum , Iterator<Individual> individualsToIndex){
	    super("IndexWorkerThread"+threadNum);
		this.indexer = indexer;
		this.threadNum = threadNum;
		this.individualsToIndex = individualsToIndex;		
	}							
	
	public void requestStop(){
	    stopRequested = true;
	}
	
	public void run(){	    
	    
	    while( ! stopRequested ){	        	       
	        
            //do the actual indexing work
            log.debug("work found for Woker number " + threadNum);	            
            addDocsToIndex();
                
            // done so shut this thread down.
            stopRequested = true;            	          
	    }  			    
		log.debug("Worker number " + threadNum + " exiting.");
	}
	
	protected void addDocsToIndex() {

		while( individualsToIndex.hasNext() ){		    
		    //need to stop right away if requested to 
		    if( stopRequested ) return;		    
		    
	        //build the document and add it to the index
		    Individual ind = null;
	        try {
	            ind = individualsToIndex.next();	            
	            indexer.index( ind );
            } catch (IndexingException e) {
                if( stopRequested )
                    return;
                
                if( ind != null )
                    log.error("Could not index individual " + ind.getURI() , e );
                else
                    log.warn("Could not index, individual was null");
            }
		    
			
			synchronized(this){
				count++;
				if( log.isInfoEnabled() ){            
					if( (count % 100 ) == 0 && count > 0 ){
						long dt = (System.currentTimeMillis() - starttime);
						log.info("individuals indexed: " + count + " in " + dt + " msec " +
								" time per individual = " + (dt / count) + " msec" );                          
					}                
				} 
			}
		}
	}
	
	public static void resetCount(){
		count = 0;
	}
	
	public static void setStartTime(long startTime){
		starttime = startTime;
	}
	
}
