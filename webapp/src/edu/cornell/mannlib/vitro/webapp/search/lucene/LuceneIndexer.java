/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;
import edu.cornell.mannlib.vitro.webapp.search.beans.Searcher;
import edu.cornell.mannlib.vitro.webapp.search.docbuilder.Obj2DocIface;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexerIface;

/**
 *
 * @author bdc34
 *
 */
public class LuceneIndexer implements IndexerIface {
	
	private final static Log log = LogFactory.getLog(LuceneIndexer.class.getName());	
    LinkedList<Obj2DocIface> obj2DocList = new LinkedList<Obj2DocIface>();
    String indexDir = null;
    Analyzer analyzer = null;
    List<Searcher> searchers = null;
    IndexWriter writer = null;
    boolean indexing = false;
    HashSet<String> urisIndexed;

    //JODA timedate library can use java date format strings.
    //http://java.sun.com/j2se/1.3/docs/api/java/text/SimpleDateFormat.html
    public static String MODTIME_DATE_FORMAT = "YYYYMMDDHHmmss";

    //date format for use with entity sunrise AND sunset
    //don't let that SUNSET in the name fool you.
    //controls the time resolution of the search.
    // "YYYYMMDDHHmm" would have minute resolution
    // "YYYYMMDD" would have day resolution;
    public static String DATE_FORMAT = "YYYYMMDD";

    /**
     * Used for the sunrise to indicate that
     * the entity has an very early sunrise
     */
    public static String BEGINNING_OF_TIME = "00000000";
    /**
     * used for the sunset to indicate that
     * the entity has a very late sunset
     */
    public static String END_OF_TIME = "ZZZ_END_OF_TIME";

    private static final IndexWriter.MaxFieldLength MAX_FIELD_LENGTH =
        IndexWriter.MaxFieldLength.UNLIMITED;

    public LuceneIndexer(String indexDir, List<Searcher> searchers, Analyzer analyzer ) throws IOException{
        this.indexDir = indexDir;
        this.analyzer = analyzer;
        this.searchers = searchers;
        makeIndexIfNone();
    }
    
    private synchronized void makeIndexIfNone() throws IOException {        
        if( !indexExists( indexDir ) )
            makeNewIndex();                           
    }

    private boolean indexExists(String dir){
        Directory fsDir = null;
        IndexSearcher isearcher = null ;
        try{
            fsDir = FSDirectory.getDirectory(indexDir);
            isearcher = new IndexSearcher(fsDir);
            return true;
        }catch(Exception ex){
            return false;
        }finally{
            try{
                if( isearcher != null )
                    isearcher.close();
                if( fsDir != null)
                    fsDir.close();
            }catch(Exception ex){}
        }
    }

    public synchronized void setIndexDir(String dirName) {
        indexDir = dirName;
    }

    public synchronized void addObj2Doc(Obj2DocIface o2d) {
        if (o2d != null)
            obj2DocList.add(o2d);
    }

    public synchronized List<Obj2DocIface> getObj2DocList() {
        return obj2DocList;
    }

    public synchronized void addSearcher(Searcher s){
        if( searchers == null ){
            searchers = new ArrayList<Searcher>();             
        }
        searchers.add( s );
    }
    
    /**
     * Checks to see if indexing is currently happening.
     * @return
     */
    public synchronized boolean isIndexing(){
        return indexing;
    }

    public synchronized void startIndexing() throws IndexingException{
        while( indexing ){ //wait for indexing to end.
            log.info("LuceneIndexer.startIndexing() waiting...");
            try{ wait(); } catch(InterruptedException ex){}
        }
        try {
            log.info("Starting to index");
            if( writer == null )
                writer =  
                    new IndexWriter(indexDir,analyzer,false, MAX_FIELD_LENGTH);
            indexing = true;
            urisIndexed = new HashSet<String>();
        } catch(Throwable ioe){
            try{
                makeNewIndex();
                indexing = true;
            }catch(Throwable ioe2){
                throw new IndexingException("LuceneIndexer.startIndexing() unable " +
                        "to make indexModifier " + ioe2.getMessage());
            }        
        }finally{
            notifyAll();
        }
    }

    public synchronized void endIndexing() {
        if( ! indexing ){ 
            notifyAll();
            return;
        }            
        try {
        	urisIndexed = null;
            log.info("ending index");
            if( writer != null )
                writer.optimize();
            
            //close the searcher so it will find the newly indexed documents
            for( Searcher s : searchers){
                s.close();
            }
        } catch (IOException e) {
            log.error("LuceneIndexer.endIndexing() - "
                    + "unable to optimize lucene index: \n" + e);
        }finally{
            closeModifier();
            indexing = false;
            notifyAll();
        }
    }

    public synchronized Analyzer getAnalyzer(){
           return analyzer;
    }

    /**
     * Indexes an object.  startIndexing() must be called before this method
     * to setup the modifier.
     *
     */
    public void index(Individual ind, boolean newDoc) throws IndexingException {
        if( ! indexing )
            throw new IndexingException("LuceneIndexer: must call " +
            		"startIndexing() before index().");
        if( writer == null )
            throw new IndexingException("LuceneIndexer: cannot build index," +
            		"IndexWriter is null.");
        if( ind == null )
        	log.debug("Individual to index was null, ignoring.");
        try {
        	if( urisIndexed.contains(ind.getURI()) ){
        		log.debug("already indexed " + ind.getURI() );
        		return;
        	}else
        		urisIndexed.add(ind.getURI());
        	
            Iterator<Obj2DocIface> it = getObj2DocList().iterator();
            while (it.hasNext()) {
                Obj2DocIface obj2doc = (Obj2DocIface) it.next();
                if (obj2doc.canTranslate(ind)) {
                	Document d = (Document) obj2doc.translate(ind);
                	if( d != null){                		                		                		
                		if( !newDoc ){                    	                    		
                			writer.updateDocument((Term)obj2doc.getIndexId(ind), d);
                			log.debug("updated " + ind.getName() + " " + ind.getURI());
                		}else{                    	
                    		writer.addDocument(d);
                    		log.debug("added " + ind.getName() + " " + ind.getURI());
                		}
                    }else{
                    	log.debug("could not translate " + ind.getURI());
                    }
                }
            }
        } catch (IOException ex) {
            throw new IndexingException(ex.getMessage());
        }
    }

    /**
     * Removes a single object from index. <code>obj</code> is translated
     * using the obj2DocList.
     */    
    public void removeFromIndex(Individual ind) throws IndexingException {
        if( writer == null )
            throw new IndexingException("LuceneIndexer: cannot delete from " +
            		"index, IndexWriter is null.");
        try {
            Iterator<Obj2DocIface> it = getObj2DocList().iterator();
            while (it.hasNext()) {
                Obj2DocIface obj2doc = (Obj2DocIface) it.next();
                if (obj2doc.canTranslate(ind)) {
                    writer.deleteDocuments((Term)obj2doc.getIndexId(ind));
                    log.debug("deleted " + ind.getName() + " " + ind.getURI());
                }
            }
        } catch (IOException ex) {            
            throw new IndexingException(ex.getMessage());
        }
    }

    /**
     * clear the index by deleting the directory and make a new empty index.
     */
    public synchronized void clearIndex() throws IndexingException{
//        if( indexing )
//            throw new IndexingException("Cannot clear search index because an" +
//            		"index rebuild in in progress.");        
        log.debug("Clearing the index at "+indexDir);
        closeModifier();
        deleteDir(new File(indexDir));
        
        //might not be thread safe since searchers can try to open a new index
//        for(LuceneSearcher s : searchers){
//            s.close();
//        }
        
        try {
            makeNewIndex();
            for(Searcher s : searchers){
                s.close();
            }            
        } catch (IOException e) {
            throw new IndexingException(e.getMessage());
        }
        notifyAll();
    }

    /**
     *  This will make a new directory and create a lucene index in it.
     */
    private synchronized void makeNewIndex() throws  IOException{
        log.debug("Making new index dir and initially empty lucene index  at " + indexDir);
        closeModifier();
        File dir = new File(indexDir);
        dir.mkdirs();
        //This will wipe out an existing index because of the true flag
        writer = new IndexWriter(indexDir,analyzer,true,MAX_FIELD_LENGTH);
    }

    private synchronized void closeModifier(){
        if( writer != null )try{
            writer.commit();
            writer.close();
        }catch(IOException ioe){
            log.error("LuceneIndexer.endIndexing() unable " +
                    "to close indexModifier " + ioe.getMessage());
        }catch(java.lang.IllegalStateException ise){
            //this is thrown when trying to close a closed index.
        }catch(Throwable t){//must not jump away from here
            log.error("in LuceneIndexer.closeModifier(): \n"+t);
        }
        writer = null;
    }   

    public long getModified() {
        long rv = 0;
        try{
            FSDirectory d = FSDirectory.getDirectory(indexDir);
            rv = IndexReader.lastModified(d);
        }catch(IOException ex){
            log.error("LuceneIndexer.getModified() - could not get modified time "+ ex);
        }
        return rv;
    }

   /** Deletes all files and subdirectories under dir.
    * Returns true if all deletions were successful.
    * If a deletion fails, the method stops attempting to delete 
    * and returns false. */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }


}
