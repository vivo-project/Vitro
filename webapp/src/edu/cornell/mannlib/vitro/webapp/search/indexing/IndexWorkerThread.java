package edu.cornell.mannlib.vitro.webapp.search.indexing;


import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;
import edu.cornell.mannlib.vitro.webapp.search.solr.IndividualToSolrDocument;



class IndexWorkerThread extends Thread{
	
	protected IndividualToSolrDocument individualToSolrDoc;
	private IndexerIface indexer = null;
	private Log log = LogFactory.getLog(IndexWorkerThread.class);
	private static long count=0;
	private Queue<Individual> indQueue = new LinkedList<Individual>();
	private int threadNum;
	private static long starttime = 0;
	private boolean distributing;
	
	public IndexWorkerThread(IndexerIface indexer, int threadNum,boolean distributing){
		this.indexer = indexer;
		this.threadNum = threadNum;
		this.distributing = distributing;
		synchronized(this){
			if(starttime == 0)
				starttime = System.currentTimeMillis();
		}
	}
	
	public void addToQueue(Individual ind){
		synchronized(indQueue){
			indQueue.offer(ind);
			indQueue.notify();
		}
	}
	
	public boolean isQueueEmpty(){
		return indQueue.isEmpty();
	}
	
	public void setDistributing(boolean distributing){
		this.distributing = distributing;
	}
	
	public void run(){

		while(this.distributing){
			synchronized(indQueue){
				try{
					while(indQueue.isEmpty() && this.distributing){
						try{
							log.debug("Worker number " + threadNum + " waiting on some work to be alloted.");
							indQueue.wait(1000);
						}catch(InterruptedException ie){
							log.error(ie,ie);
						}
					}
					
					Thread.sleep(50); //wait a bit to let a bit more work to come into the queue
					log.debug("work found for Woker number " + threadNum);
					addDocsToIndex();

				} catch (InterruptedException e) {
					log.debug("Worker number " + threadNum + " woken up",e);
				}
				catch(Throwable e){
					log.error(e,e);
				}
			}
		}
		log.info("Worker number " + threadNum + " exiting.");
	}
	
	protected void addDocsToIndex() throws IndexingException{

		while(!indQueue.isEmpty()){
			indexer.index(indQueue.poll());
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
	
}
