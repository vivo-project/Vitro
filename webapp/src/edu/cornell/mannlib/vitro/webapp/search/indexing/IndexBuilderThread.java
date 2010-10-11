/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Thread that executes the methods in IndexBuilder.  
 * 
 * @author bdc34
 *
 */
public class IndexBuilderThread extends Thread{
	private IndexBuilder indexBuilder;
	protected boolean stopRequested = false;
	protected long reindexInterval = 1000 * 60 /* msec */ ;
	
	private static final Log log = LogFactory.getLog(IndexBuilderThread.class.getName());
	
	public IndexBuilderThread(IndexBuilder ib){
		super("IndexBuilderThread");
		if( ib == null )
		    log.error("IndexBuilderThread needs an IndexBuilder, search is not configured.");
		this.indexBuilder = ib;
	}
	
	@Override
	public void run() {
		while(true){
			if( stopRequested ){
				log.info("Stopping IndexBuilderThread ");		
				return;
			}
			
			if( indexBuilder == null )
			    log.warn("IndexBuilderThread needs a IndexBuilder, search may not be configured.");
			
			try{
				if( indexBuilder != null && indexBuilder.isReindexRequested() ){
					log.debug("full re-index requested");
					indexBuilder.indexRebuild();
				}else{
					if( indexBuilder != null && indexBuilder.isThereWorkToDo() ){						
						Thread.sleep(250); //wait a bit to let a bit more work to come into the queue
						log.debug("work found for IndexBuilder, starting update");
						indexBuilder.updatedIndex();
					}
				}
			}catch (Throwable e) {
				log.error(e,e);
			}
			
			if( indexBuilder != null && ! indexBuilder.isThereWorkToDo() ){
				log.debug("there is no indexing working to do, going to sleep");
				try {
					synchronized (this) {
						this.wait(reindexInterval);	
					}			
				} catch (InterruptedException e) {
					log.debug(" woken up",e);
				}
			}
		}
	}

	public synchronized void kill(){
		log.debug("Attempting to kill IndexBuilderThread ");
		stopRequested = true;
		this.notifyAll();
	}
}
