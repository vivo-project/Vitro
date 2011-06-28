/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;
import edu.cornell.mannlib.vitro.webapp.search.docbuilder.Obj2DocIface;

/**
 * IntexerIface is for objects that will be used by the IndexBuilder.  The
 * IndexBuilder will manage getting lists of object to index and then use
 * an object that implements IndexerIface to stuff the backend index.
 *
 * An example is LuceneIndexer which is set up and associated with a
 * IndexBuilder in LuceneSetup.
 *
 * @author bdc34
 *
 */
public interface IndexerIface {

    public void addObj2Doc(Obj2DocIface o2d);
    public List<Obj2DocIface> getObj2DocList();

    /**
     * Check if indexing is currently running in a different thread.
     * @return
     */
    public boolean isIndexing();

    /**
     * Index a document.  This should do an update of the
     * document in the index of the semantics of the index require it.
     *
     * @param doc
     * @param newDoc - if true, just insert doc, if false attempt to update.
     * @throws IndexingException
     */
    public void index(Individual ind)throws IndexingException;


    /**
     * Remove a document from the index.
     * @param obj
     * @throws IndexingException
     */
    public void removeFromIndex(String uri) throws IndexingException;

    public void prepareForRebuild() throws IndexingException;
    
    public void startIndexing() throws IndexingException;
    public void endIndexing();

    public long getModified();
    
    /**
     * Ends the indexing and removes any temporary files.
     * This may be called instead of endIndexing()
     */
    public void abortIndexingAndCleanUp();
}
