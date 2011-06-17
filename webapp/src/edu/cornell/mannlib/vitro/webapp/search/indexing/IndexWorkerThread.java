package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;



class IndexWorkerThread implements Runnable{
	
	private IndexerIface indexer;
	private static Log log = LogFactory.getLog(IndexWorkerThread.class);
	private Queue<Individual> indQueue = new LinkedList<Individual>();
	
	public IndexWorkerThread(IndexerIface indexer){
		
		this.indexer = indexer;
	}
	
	public void addToQueue(Individual ind, boolean newDocs){
		
	}
	
	public void shutdown() {
		
		
	}
	
	
	public void run(){
		
		//check for work
		//if work found, 
		// translate
		// send to server
		//sleep (1000) 
	
	}
	
	/*protected void indexInd() throws AbortIndexing{
		long starttime = System.currentTimeMillis();         
        long count = 0;
		Iterator<Individual> individuals = firstList.iterator();
		 while(individuals.hasNext()){
	            if( stopRequested )
	                throw new AbortIndexing();
	            
	            Individual ind = null;
	            try{
	                ind = individuals.next();                                
	                indexer.index(ind, newDocs);                         
	            }catch(Throwable ex){
	                if( stopRequested || log == null){//log might be null if system is shutting down.
	                    throw new AbortIndexing();
	                }
	                String uri = ind!=null?ind.getURI():"null";
	                log.warn("Error indexing individual from separate thread" + uri + " " + ex.getMessage());
	            }
	            count++;
	            if( log.isDebugEnabled() ){            
	                if( (count % 100 ) == 0 && count > 0 ){
	                    long dt = (System.currentTimeMillis() - starttime);
	                    log.debug("individuals indexed from seperate thread: " + count + " in " + dt + " msec " +
	                             " time pre individual from seperate thread = " + (dt / count) + " msec" );                          
	                }                
	            }                
	        }
	}
	private class AbortIndexing extends Exception {
    	// Just a vanilla exception
    }  */
}
