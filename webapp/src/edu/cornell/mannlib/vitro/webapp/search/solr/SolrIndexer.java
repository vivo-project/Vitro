/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;
import edu.cornell.mannlib.vitro.webapp.search.docbuilder.Obj2DocIface;
import edu.cornell.mannlib.vitro.webapp.search.solr.IndividualToSolrDocument;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexerIface;

public class SolrIndexer implements IndexerIface {
    private final static Log log = LogFactory.getLog(SolrIndexer.class);
    
    protected SolrServer server;
    protected boolean indexing;    
    protected List<Obj2DocIface> obj2DocList;
    protected HashSet<String> urisIndexed;
    
    public SolrIndexer( SolrServer server, List<Obj2DocIface> o2d){
        this.server = server; 
        this.obj2DocList = o2d;        
    }
    
    @Override
    public synchronized void index(Individual ind, boolean newDoc) throws IndexingException {
        if( ! indexing )
            throw new IndexingException("SolrIndexer: must call " +
                    "startIndexing() before index().");
        
        if( ind == null )
            log.debug("Individual to index was null, ignoring.");
        
        try{
            if( urisIndexed.contains(ind.getURI()) ){
                log.debug("already indexed " + ind.getURI() );
                return;
            }else{
                urisIndexed.add(ind.getURI());
                log.debug("indexing " + ind.getURI());
                Iterator<Obj2DocIface> it = getObj2DocList().iterator();
                while (it.hasNext()) {
                    Obj2DocIface obj2doc = (Obj2DocIface) it.next();
                    if (obj2doc.canTranslate(ind)) {
                        SolrInputDocument solrDoc = (SolrInputDocument) obj2doc.translate(ind);
                        if( solrDoc != null){
                            //sending each doc individually is inefficient
                            Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
                            docs.add( solrDoc );
                            server.add( docs );
                   
//                            if( !newDoc ){  
//                                server.add( docs );
//                                log.debug("updated " + ind.getName() + " " + ind.getURI());
//                            }else{                 
//                                server.add( docs );
//                                log.debug("added " + ind.getName() + " " + ind.getURI());
//                            }
                        }else{
                            log.debug("removing from index " + ind.getURI());
                            //writer.deleteDocuments((Term)obj2doc.getIndexId(ind));
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new IndexingException(ex.getMessage());
        } catch (SolrServerException ex) {
            throw new IndexingException(ex.getMessage());
        }
        
    }

    @Override
    public boolean isIndexing() {     
        return indexing;
    }

    @Override
    public void prepareForRebuild() throws IndexingException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeFromIndex(Individual ind) throws IndexingException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public synchronized void startIndexing() throws IndexingException {        
        while( indexing ){ //wait for indexing to end.
            log.debug("LuceneIndexer.startIndexing() waiting...");
            try{ wait(); } catch(InterruptedException ex){}
        }
               
        log.debug("Starting to index");        
        indexing = true;
        urisIndexed = new HashSet<String>();        
        notifyAll();        
    }
    
    
    public synchronized void addObj2Doc(Obj2DocIface o2d) {
        if (o2d != null)
            obj2DocList.add(o2d);
    }

    public synchronized List<Obj2DocIface> getObj2DocList() {
        return obj2DocList;
    }
    
    @Override
    public void abortIndexingAndCleanUp() {
        endIndexing();        
    }
   
    @Override
    public synchronized void endIndexing() {
        try {
            server.commit();            
        } catch (Exception e) {
            log.error("Could not commit to solr server", e);
        }finally{
        	IndividualToSolrDocument.betas.clear();
        	IndividualToSolrDocument.betas = null;
        }
        
        try {
            server.optimize();
        } catch (Exception e) {
            log.error("Could not optimize solr server", e);
        }
        indexing = false;
        notifyAll();
    }

    @Override
    public long getModified() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean isIndexEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    
   
   

}
