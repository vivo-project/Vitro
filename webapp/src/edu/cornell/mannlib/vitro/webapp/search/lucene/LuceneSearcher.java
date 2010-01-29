package edu.cornell.mannlib.vitro.webapp.search.lucene;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.cornell.mannlib.vitro.webapp.search.SearchException;
import edu.cornell.mannlib.vitro.webapp.search.beans.Searcher;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroHighlighter;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQuery;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQueryFactory;
import edu.cornell.mannlib.vitro.webapp.search.docbuilder.Obj2DocIface;

//
//NOTES ABOUT SEARCHING AND INDEXING AT THE SAME TIME
//from the lucene cvs commit logs:
// cvs commit: jakarta-lucene/src/test/org/apache/lucene
// ThreadSafetyTest.java
// Thu, 27 Sep 2001 09:01:08 -0700
//
// cutting 01/09/27 09:27:02
//
// Modified: src/java/org/apache/lucene/index IndexReader.java
// IndexWriter.java SegmentReader.java
// src/java/org/apache/lucene/store Directory.java
// FSDirectory.java RAMDirectory.java
// src/test/org/apache/lucene ThreadSafetyTest.java
// Added: src/java/org/apache/lucene/store Lock.java
// Log:
// Added index lock files. Indexing and search are now not just
// thread
// safe, but also "process safe": multiple processes may may now
// search
// an index while it is being updated from another process.
//
// Two lock files are used in an index. One is "commit.lock". This
// is
// used to synchronize commits [IndexWriter.close()] with opens
// [IndexReader.open()]. Since these actions are short-lived,
// attempts
// to obtain this lock will block for up to ten seconds, which
// should be
// plenty of time, before an exception is thrown.
//
// The second lock file is "write.lock". This is used to enforce the
// restriction that only one process should be adding documents to
// an
// index at a time. This is created when an IndexWriter is
// constructed
// and removed when it is closed. If index writing is aborted then
// this
// file must be manually removed. Attempts to index from another
// process
// will immediately throw an exception.
// so a check if indexing is running, is not needed
// try {
// IndexBuilder builder =
// (IndexBuilder)getServletContext().getAttribute(IndexBuilder.class.getName());
// if( builder.isIndexing() ){
// //System.out.println("location 1");
// doIndexingRunningErrorPage(request, response);
// return;
// }
// } catch (Throwable e) {
// System.out.println("here at the first chunk of code");
// e.printStackTrace();
// }

public class LuceneSearcher implements Searcher {
    private VitroQueryFactory QFactory = null;
    protected String indexDir = "index directory has not be set";
    private IndexSearcher indexSearcher = null;
    private static final Log log = LogFactory.getLog(LuceneSearcher.class.getName());
    /**
     * Caching Filter for default time window.  It is in this
     * class so that when the searcher gets closed and reopened
     * this cache can be thrown out and a new cache started.
     */
    private CachingWrapperFilter timeWindowCachingFilter = null;
    private long timeWinFilterBorn = 0L;

    //Obj2Docs are used to convert hits to objects
    LinkedList obj2DocList = new LinkedList();

    public LuceneSearcher(LuceneQueryFactory fact, String indexDir){
        this.QFactory = fact;
        this.indexDir = indexDir;
        this.indexSearcher = getIndexSearcher();
    }

    public VitroQueryFactory getQueryFactory() {
        return QFactory;
    }

    public void addObj2Doc(Obj2DocIface o2d) {
        if (o2d != null)
            obj2DocList.add(o2d);
    }

    public List search(VitroQuery query) throws SearchException {
        if( ! (query instanceof LuceneQuery ) ){
            String queryObjMismatchMsg = "The LuceneSearcher needs a LuceneQuery " +
                "object when performing a search.\n"
                +"This should have been setup by the LuceneSetup when the servlet " +
                "context started.\n"
                +"The code in LuceneSetup can be run using a listener element in the " +
                "web.xml set the comments for LuceneSetup.";
            throw new SearchException( queryObjMismatchMsg );
        }

        Query luceneQuery = (Query)query.getQuery();

        //use the caching default time filter
        //bdc34 as of 2009-01-30, getting rid of time windows
//        if( ((LuceneQuery)query).defaultTimeWindow )
//            luceneQuery = new FilteredQuery(luceneQuery,
//                                             getTimeWindowCachingFilter());

        List results = null;
        //Hits hits = null;
        //HitsCollector collector = new HitsCollector();
        
        
//        final BitSet bits = new BitSet(indexSearcher.maxDoc());
//        searcher.search(query, new HitCollector() {
//            public void collect(int doc, float score) {
//              bits.set(doc);
//            }
//          });
        
        /*
        TopDocCollector collector = new TopDocCollector(hitsPerPage);
        *   searcher.search(query, collector);
        *   ScoreDoc[] hits = collector.topDocs().scoreDocs;
        *   for (int i = 0; i < hits.length; i++) {
        *     int docId = hits[i].doc;
        *     Document d = searcher.doc(docId);
        */

        
        IndexSearcher is = getIndexSearcher();        
        if( is == null )
            throw new SearchException("Unable to find a Search Index");
        
        try{
            final BitSet bits = new BitSet(is.maxDoc());
            is.search(luceneQuery,(HitCollector)new HitCollector() {
                public void collect(int doc, float score) {
                  bits.set(doc);
                }
              });
            results = hits2objs(is, bits);
        }catch(Throwable t){
            String msg = "There was an error executing your search: " + t;
            log.error(msg);
            t.printStackTrace();
            throw new SearchException( msg );
        }               
        return results;
    }

    /**
     * This is going to go through the list of hits and make objects for each hit.
     * After making the object, it attempts to invoke the methods setId and setName.
     * @param hits
     * @return
     * @throws IOException
     * @throws SearchException
     */
    private List hits2objs(IndexSearcher index, BitSet hits)throws IOException, SearchException{
        if( hits == null ) throw new SearchException("There was no hits object");
        List objs = new ArrayList(hits.cardinality());
        for (int i = 0; i < hits.length(); i++) {
            if( hits.get(i) ){
                Document doc = index.doc(i);
                Object obj = null;
                if( doc != null ){
                    obj = tryObj2Docs( doc );
                    /*log.debug(obj.getClass().getName() + doc.get("ID")
                       + " with a score of " + hits.score(i));*/
                    if( obj != null )
                        objs.add(obj);
                }
            }
        }
        return objs;
    }

    /**
     * go through all the obj2doc translators and attemp to untranslate the doc
     * into a vitro entity.  If there are no translators that work then just return the
     * hit Document.
     * @param hit
     * @return
     */
    private Object tryObj2Docs(Document hit){
        Object obj = hit;
        Iterator it = obj2DocList.iterator();
        while(it.hasNext()){
            Obj2DocIface trans = (Obj2DocIface) it.next();
            if( trans.canUnTranslate(hit) )
                obj = trans.unTranslate(hit);
        }
        return obj;
    }

    protected synchronized IndexSearcher getIndexSearcher() {
        if( indexSearcher == null ){
            try {
                Directory fsDir = FSDirectory.getDirectory(indexDir);
                indexSearcher = new IndexSearcher(fsDir);
                timeWindowCachingFilter = null;
            } catch (IOException e) {
                log.error("LuceneSearcher: could not make indexSearcher "+e);
                log.error("It is likely that you have not made a directory for the lucene index.  "+
                          "Create the directory indicated in the error and set permissions/ownership so"+
                          " that the tomcat server can read/write to it.");
                //The index directory is created by LuceneIndexer.makeNewIndex()
            }
        }
        return indexSearcher;
    }


    public synchronized void close() {
        if( indexSearcher != null ){
            try{
                indexSearcher.close();
            }catch(Throwable t){}
            indexSearcher = null;
        }
        timeWindowCachingFilter = null;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    //bdc34 as of 2009-01-30, getting rid of time windows
//    public synchronized Filter getTimeWindowCachingFilter(){
//        if( timeWindowCachingFilter == null || timeWinCacheStale() ){
//            Filter defaultFilter = new QueryFilter( makeDefaultTimeWindowQuery() );
//            timeWindowCachingFilter = new CachingWrapperFilter( defaultFilter );
//            timeWinFilterBorn = System.currentTimeMillis();
//            log.debug("getTimeWindowCachingFilter() made new cache " + timeWinFilterBorn);
//        }
//        return timeWindowCachingFilter;
//    }

    //bdc34 as of 2009-01-30, getting rid of time windows
//    private final long timeWinCacheShelfLife = ( 60 * 1000 ) * 1; //in msec
//    private boolean  timeWinCacheStale(){
//        long dt = (System.currentTimeMillis() - timeWinFilterBorn);
//        return dt > timeWinCacheShelfLife;
//    }


    //bdc34 as of 2009-01-30, getting rid of time windows
    /**
     * Adds a Query that will get doc where the
     * SUNSET is > NOW  and SUNRISE <= NOW. We'll do
     * this by creating two RangeQueries, one to
     * check that SUNRISE is between [BEGINNING_OF_TIME, NOW]
     * and that SUNSET is between [NOW, END_OF_TIME]
     * There don't seem to be any GraterThanQuery
     * or LessThanQuery classes in lucene.
     */
//    private BooleanQuery makeDefaultTimeWindowQuery(){
//        String nowStr = new DateTime().toString(LuceneIndexer.DATE_FORMAT);
//
//        Term BEGINNING_OF_TIME = null;
//        Term now = new Term(Entity2LuceneDoc.term.SUNRISE,nowStr );
//        RangeQuery sunriseBeforeNow = new RangeQuery(BEGINNING_OF_TIME,now, true);
//
//        Term END_OF_TIME = null;
//        now = new Term(Entity2LuceneDoc.term.SUNSET,nowStr);
//        RangeQuery sunsetAfterNow = new RangeQuery(now,END_OF_TIME, false);
//
//        BooleanQuery qRv = new BooleanQuery();
//        qRv.add( sunriseBeforeNow, BooleanClause.Occur.MUST);
//        qRv.add( sunsetAfterNow, BooleanClause.Occur.MUST);
//
//        return qRv;
//    }

    /**
     * We need access to the index to make a highligher because
     * we need to 'rewrite' the query.  That takes any wild cards
     * and replaces them will all terms that are found in the index.
     */
    public VitroHighlighter getHighlighter(VitroQuery queryIn){
        if( ! (queryIn instanceof LuceneQuery) ){
            log.error("LuceneSearcher expects to get a LuceneQuery");
            throw new Error("LuceneSearcher expects to get a LuceneQuery");
        }
        if( queryIn == null )
            return null;

        LuceneHighlighter highlighter = null;
        try {
            LuceneQuery lucQuery = (LuceneQuery) queryIn;
            Analyzer analyzer = lucQuery.getAnalyzer();
            Query query = (Query)lucQuery.getQuery();
            if( getIndexSearcher().getIndexReader() != null ){
                query = query.rewrite( getIndexSearcher().getIndexReader() );
            }
            highlighter = new LuceneHighlighter( query, analyzer );
        } catch (SearchException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }
        return   (VitroHighlighter)highlighter;
    }

}
