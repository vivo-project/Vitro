/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;
import edu.cornell.mannlib.vitro.webapp.search.beans.IndexerIface;
import edu.cornell.mannlib.vitro.webapp.search.solr.IndividualToSolrDocument;

class IndexWorkerThread extends Thread{
	
    protected final int threadNum;
	protected IndividualToSolrDocument individualToSolrDoc;
	protected final IndexerIface indexer;
	protected final Iterator<Individual> individualsToIndex;
	protected boolean stopRequested = false;
	
	private Log log = LogFactory.getLog(IndexWorkerThread.class);
	private static AtomicLong countCompleted= new AtomicLong();		
	private static AtomicLong countToIndex= new AtomicLong();		
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
		    try{
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
    		    
    			
				long countNow = countCompleted.incrementAndGet();
				if( log.isInfoEnabled() ){            
					if( (countNow % 100 ) == 0 ){
						long dt = (System.currentTimeMillis() - starttime);
						log.info("individuals indexed: " + countNow + " in " + dt + " msec " +
								" time per individual = " + (dt / countNow) + " msec" );                          
					}                
				} 
		    }catch(Throwable th){
		        //on tomcat shutdown odd exceptions get thrown and log can be null
		        if( log != null )
		            log.debug("Exception during index building",th);		            
		    }
		}
	}

	public static void resetCounters(long time, long workload) {
		IndexWorkerThread.starttime = time;
		IndexWorkerThread.countToIndex.set(workload);
		IndexWorkerThread.countCompleted.set(0);
	}
	
	public static long getCount() {
		return countCompleted.get();
	}
	
	public static long getCountToIndex() {
		return countToIndex.get();
	}
}
