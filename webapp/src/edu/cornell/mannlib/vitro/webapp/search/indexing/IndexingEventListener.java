/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.search.indexing;

/**
 * Classes that implement this interface can get informed of 
 * events that happen to the IndexBuilder. 
 */
public interface IndexingEventListener {

    public enum EventTypes {
        START_UPDATE,
        FINISHED_UPDATE,
        START_FULL_REBUILD,
        FINISH_FULL_REBUILD        
    }

    
    public void notifyOfIndexingEvent(EventTypes ie);    
}
