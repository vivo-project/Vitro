/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

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
	
	private final static Log log = LogFactory.getLog(LuceneIndexer.class);
	
    LinkedList<Obj2DocIface> obj2DocList = new LinkedList<Obj2DocIface>();
    String baseIndexDir = null;
    String liveIndexDir = null;
    Analyzer analyzer = null;
    List<Searcher> searchers = Collections.EMPTY_LIST;
    IndexWriter writer = null;
    boolean indexing = false;
    boolean fullRebuild = false;
    HashSet<String> urisIndexed;
    private LuceneIndexFactory luceneIndexFactory;
    private String currentOffLineDir;
    

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

    public LuceneIndexer(String baseIndexDir, String liveIndexDir,  List<Searcher> searchers, Analyzer analyzer ) throws IOException{
        this.baseIndexDir = baseIndexDir;
        this.liveIndexDir = liveIndexDir;
        this.analyzer = analyzer;
        if( searchers != null )
            this.searchers = searchers;

        updateTo1p2();
        makeEmptyIndexIfNone();        
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
    
    @Override
    public synchronized void prepareForRebuild() throws IndexingException {
        if( this.indexing )
            log.error("Only an update will be performed, must call prepareForRebuild() before startIndexing()");
        else
            this.fullRebuild = true;        
    }
    
    /**
     * Checks to see if indexing is currently happening.
     */
    public synchronized boolean isIndexing(){
        return indexing;
    }

    public synchronized void startIndexing() throws IndexingException{
        while( indexing ){ //wait for indexing to end.
            log.debug("LuceneIndexer.startIndexing() waiting...");
            try{ wait(); } catch(InterruptedException ex){}
        }
        checkStartPreconditions();
        try {
            log.debug("Starting to index");        
            if( this.fullRebuild ){
                String offLineDir = getOffLineBuildDir();
                this.currentOffLineDir = offLineDir;
                writer = new IndexWriter(offLineDir, analyzer, true, MAX_FIELD_LENGTH);
            }else{                
                writer = getLiveIndexWriter(false);                
            }
            indexing = true;
            urisIndexed = new HashSet<String>();
        } catch(Throwable th){
            throw new IndexingException("startIndexing() unable " +
                         "to make IndexWriter:" + th.getMessage());
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
            log.debug("ending index");
            if( writer != null )
                writer.optimize();
                     
            if( this.fullRebuild )
                bringRebuildOnLine();
            
            //close the searcher so it will find the newly indexed documents
            for( Searcher s : searchers){
                s.close();
            }
            //this is the call that replaces Searcher.close()
            luceneIndexFactory.forceNewIndexSearcher();
            
        } catch (IOException e) {
            log.error("LuceneIndexer.endIndexing() - "
                    + "unable to optimize lucene index: \n" + e);
        }finally{            
            fullRebuild = false;
            closeWriter();
            indexing = false;
            notifyAll();
        }
    }

    public void setLuceneIndexFactory(LuceneIndexFactory lif) {
        luceneIndexFactory = lif;    
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
        	}else{
        		urisIndexed.add(ind.getURI());
        	    log.debug("indexing " + ind.getURI());
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
                        	log.debug("could not translate, removing from index " + ind.getURI());
                        	writer.deleteDocuments((Term)obj2doc.getIndexId(ind));
                        }
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
     *  This will make a new directory and create a lucene index in it.
     */
    private synchronized void makeNewIndex() throws  IOException{
     
    }
    
    private synchronized void closeWriter(){
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

    private synchronized void bringRebuildOnLine() {
        closeWriter();
        deleteDir(new File(liveIndexDir));
        File offLineDir = new File(currentOffLineDir);
        File liveDir = new File(liveIndexDir);
        boolean success =  offLineDir.renameTo( liveDir );
        if( ! success ){
            log.error("could not move off line index at " 
                    + offLineDir.getAbsolutePath() + " to live index directory " 
                    + liveDir.getAbsolutePath());
        }else{            
            deleteDir(new File(currentOffLineDir));
            currentOffLineDir = null;
        }
    }  
    
    private synchronized String getOffLineBuildDir(){
        File baseDir = new File(baseIndexDir);
        baseDir.mkdirs();
        File tmpDir = new File( baseIndexDir + File.separator + "tmp" );
        tmpDir.mkdir();        
        File offLineBuildDir = new File( baseIndexDir + File.separator + "tmp"  + File.separator + "offLineRebuild" + System.currentTimeMillis());
        offLineBuildDir.mkdir();
        String dirName = offLineBuildDir.getAbsolutePath();
        if( ! dirName.endsWith(File.separator) )
            dirName = dirName + File.separator;
        return dirName;            
    }
    
    public long getModified() {
        long rv = 0;
        try{
            FSDirectory d = FSDirectory.getDirectory(liveIndexDir);
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

    private void checkStartPreconditions() {        
        if( this.writer != null )
            log.error("it is expected that the writer would " +
                    "be null but it isn't");
        if( this.currentOffLineDir != null)
            log.error("it is expected that the current" +
                    "OffLineDir would be null but it is " + currentOffLineDir);      
        if( indexing )
            log.error("indexing should not be set to true just yet");        
    }

    private IndexWriter getLiveIndexWriter(boolean createNew) throws CorruptIndexException, LockObtainFailedException, IOException{
        return new IndexWriter(this.liveIndexDir, analyzer, createNew, MAX_FIELD_LENGTH);
    }

    private synchronized void makeEmptyIndexIfNone() throws IOException {        
        if( !liveIndexExists() ){
            log.debug("Making new index dir and initially empty lucene index  at " + liveIndexDir);
            closeWriter();
            makeIndexDirs();            
            writer = getLiveIndexWriter(true);
            closeWriter();
        }                                   
    }

    private synchronized void makeIndexDirs() throws IOException{            
        File baseDir = new File(baseIndexDir);
        if( ! baseDir.exists())
            baseDir.mkdirs();
        
        File dir = new File(liveIndexDir);
        if( ! dir.exists() )
            dir.mkdirs();        
    }
    
    private boolean liveIndexExists(){        
        return indexExistsAt(liveIndexDir);        
    }    

    private boolean indexExistsAt(String dirName){
        Directory fsDir = null;
        try{
            fsDir = FSDirectory.getDirectory(dirName);
            return IndexReader.indexExists(fsDir);            
        }catch(Exception ex){
            return false;
        }finally{
            try{
                if( fsDir != null)
                    fsDir.close();
            }catch(Exception ex){}
        }
    }
    
    /*
     * In needed, create new 1.2 style index directories and copy old index to new dirs.
     */
    private synchronized void updateTo1p2() throws IOException {
        //check if live index directory exists, don't check for a lucene index.
        File liveDirF = new File(this.liveIndexDir);
        if( ! liveDirF.exists() && indexExistsAt(baseIndexDir)){
            log.info("Updating to vitro 1.2 search index directory structure");
            makeIndexDirs();            
            File live = new File(liveIndexDir);
            
            //copy existing index to live index directory
            File baseDir = new File(baseIndexDir);
            for( File file : baseDir.listFiles()){                                
                if( ! file.isDirectory() && ! live.getName().equals(file.getName() ) ){
                    FileUtils.copyFile(file, new File(liveIndexDir+File.separator+file.getName()));
                    boolean success = file.delete();
                    if( ! success )
                        log.error("could not delete "+ baseIndexDir + file.getName());
                }
            }
            log.info("Done updating to vitro 1.2 search index directory structure.");
        }        
    }
    
    public boolean isIndexEmpty() throws CorruptIndexException, IOException{                
        IndexWriter writer = null;
        try{
            writer = getLiveIndexWriter(false);            
            return  writer.numDocs() == 0;
        }finally{
            if (writer != null) writer.close();
        }
    }

    public boolean isIndexCorroupt(){
        //if it is clear it out but don't rebuild.
        return false;
    }    
}
