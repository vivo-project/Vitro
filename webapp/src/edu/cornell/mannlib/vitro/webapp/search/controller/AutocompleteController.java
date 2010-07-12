/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreeMarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.flags.PortalFlag;
import edu.cornell.mannlib.vitro.webapp.search.SearchException;
import edu.cornell.mannlib.vitro.webapp.search.beans.Searcher;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroHighlighter;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQuery;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQueryFactory;
import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneIndexer;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneSetup;
import edu.cornell.mannlib.vitro.webapp.utils.FlagMathUtils;
import freemarker.template.Configuration;

/**
 * AutocompleteController is used to generate autocomplete and select element content
 * through a Lucene search. The search logic is copied from PagedSearchController.
 */

/* rjy7 We should have a SearchController that is subclassed by both PagedSearchController 
 * and AjaxSearchController, so the methods don't all have to be copied into both places.
 * The parent SearchController should extend FreeMarkerHttpServlet. Can only be done
 * once PagedSearchController has been moved to FreeMarker.
 */
public class AutocompleteController extends FreeMarkerHttpServlet implements Searcher{

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(AutocompleteController.class);

    private static String QUERY_PARAMETER_NAME = "term";
    private static String EXCLUDE_URI_PARAMETER_NAME = "excludeUri";
    
    private IndexSearcher searcher = null;
    String NORESULT_MSG = "";    
    private int defaultMaxSearchSize= 1000;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        LuceneIndexer indexer=(LuceneIndexer)getServletContext()
        .getAttribute(LuceneIndexer.class.getName());
        indexer.addSearcher(this);

        try{
            String indexDir = getIndexDir(getServletContext());        
            getIndexSearcher(indexDir);
        }catch(Exception ex){

        }                                           
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        doGet(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
        
        // String templateName = request.getServletPath().equals("/autocomplete") ? "autocompleteResults.ftl" : "selectResults.ftl";  
        String templateName = "autocompleteResults.ftl";
        Map<String, Object> map = new HashMap<String, Object>();

        VitroRequest vreq = new VitroRequest(request);
        Configuration config = getConfig(vreq);
        PortalFlag portalFlag = vreq.getPortalFlag();
        
        try {
 
            // make sure an IndividualDao is available
            if( vreq.getWebappDaoFactory() == null 
                    || vreq.getWebappDaoFactory().getIndividualDao() == null ){
                log.error("makeUsableBeans() could not get IndividualDao ");
                doSearchError(templateName, map, config, response);
                return;
            }
            IndividualDao iDao = vreq.getWebappDaoFactory().getIndividualDao();                       
            
            int maxHitSize = defaultMaxSearchSize;

            String indexDir = getIndexDir(getServletContext());
            
            String qtxt = vreq.getParameter(QUERY_PARAMETER_NAME);
            Analyzer analyzer = getAnalyzer(getServletContext());
            
            // Get the list of individual uris that should be excluded from the search
            String filters[] = vreq.getParameterValues(EXCLUDE_URI_PARAMETER_NAME);
            List<String> urisToExclude = null; 
            if (filters != null) {
                urisToExclude = Arrays.asList(filters);
            }
            
            //boolean tokenize = "true".equals(vreq.getParameter("tokenize"));
            
            Query query = getQuery(vreq, portalFlag, analyzer, indexDir, qtxt, urisToExclude);             
            log.debug("query for '" + qtxt +"' is " + query.toString());

            if (query == null ) {
                doNoQuery(templateName, map, config, response);
                return;
            }
            
            IndexSearcher searcherForRequest = getIndexSearcher(indexDir);
                                                
            TopDocs topDocs = null;
            try{
                topDocs = searcherForRequest.search(query,null,maxHitSize);
            }catch(Throwable t){
                log.error("in first pass at search: " + t);
                // this is a hack to deal with odd cases where search and index threads interact
                try{
                    wait(150);
                    topDocs = searcherForRequest.search(query,null,maxHitSize);
                }catch (Exception ex){
                    log.error(ex);
                    doFailedSearch(templateName, map, config, response);
                    return;
                }
            }

            if( topDocs == null || topDocs.scoreDocs == null){
                log.error("topDocs for a search was null");                
                doFailedSearch(templateName, map, config, response);
                return;
            }
            
            int hitsLength = topDocs.scoreDocs.length;
            if ( hitsLength < 1 ){                
                doFailedSearch(templateName, map, config, response);
                return;
            }            
            log.debug("found "+hitsLength+" hits"); 

            List<SearchResult> results = new ArrayList<SearchResult>();
            for(int i=0; i<topDocs.scoreDocs.length ;i++){
                try{                     
                    Document doc = searcherForRequest.doc(topDocs.scoreDocs[i].doc);                    
                    String uri = doc.get(Entity2LuceneDoc.term.URI);
                    Individual ind = iDao.getIndividualByURI(uri);
                    if (ind != null) {
                        String name = ind.getName();
                        SearchResult result = new SearchResult(name, uri);
                        results.add(result);
                    }
                } catch(Exception e){
                    log.error("problem getting usable Individuals from search " +
                            "hits" + e.getMessage());
                }
            }   

            Collections.sort(results);
            map.put("results", results);
            writeTemplate(templateName, map, config, response);
   
        } catch (Throwable e) {
            log.error("AutocompleteController(): " + e);            
            doSearchError(templateName, map, config, response);
            return;
        }
    }

    private String getIndexDir(ServletContext servletContext) throws SearchException {
        Object obj = servletContext.getAttribute(LuceneSetup.INDEX_DIR);
        if( obj == null || !(obj instanceof String) )
            throw new SearchException("Could not get IndexDir for lucene index");
        else
            return (String)obj;
    }

    private Analyzer getAnalyzer(ServletContext servletContext) throws SearchException {
        Object obj = servletContext.getAttribute(LuceneSetup.ANALYZER);
        if( obj == null || !(obj instanceof Analyzer) )
            throw new SearchException("Could not get anlyzer");
        else
            return (Analyzer)obj;        
    }

    private Query getQuery(VitroRequest request, PortalFlag portalState,
                       Analyzer analyzer, String indexDir, String querystr, List<String> urisToExclude) throws SearchException{
        
        Query query = null;
        try {
            if( querystr == null){
                log.error("There was no Parameter '"+ QUERY_PARAMETER_NAME            
                    +"' in the request.");                
                return null;
            }else if( querystr.length() > MAX_QUERY_LENGTH ){
                log.debug("The search was too long. The maximum " +
                        "query length is " + MAX_QUERY_LENGTH );
                return null;
            } 

            query = makeNameQuery(querystr, request);
            

            // Filter by type
            {
                BooleanQuery boolQuery = new BooleanQuery(); 
                String typeParam = (String) request.getParameter("type");
                boolQuery.add(  new TermQuery(
                        new Term(Entity2LuceneDoc.term.RDFTYPE, 
                                typeParam)),
                    BooleanClause.Occur.MUST);
                boolQuery.add(query, BooleanClause.Occur.MUST);
                query = boolQuery;
            }
            
            // Uris that should be excluded from the results
            if (urisToExclude != null) {
                for (String uri : urisToExclude) {
                    BooleanQuery boolQuery = new BooleanQuery();
                    boolQuery.add( query, BooleanClause.Occur.MUST);
                    boolQuery.add( new TermQuery(
                        new Term(Entity2LuceneDoc.term.URI, uri)),
                        BooleanClause.Occur.MUST_NOT);     
                    query = boolQuery;       
                }
            }

            //if we have a flag/portal query then we add
            //it by making a BooelanQuery.
            Query flagQuery = makeFlagQuery( portalState );
            if( flagQuery != null ){
                BooleanQuery boolQuery = new BooleanQuery();
                boolQuery.add( query, BooleanClause.Occur.MUST);
                boolQuery.add( flagQuery, BooleanClause.Occur.MUST);
                query = boolQuery;
            }
            
        } catch (Exception ex){
            throw new SearchException(ex.getMessage());
        }

        return query;
    }
    
    private Query makeNameQuery(String querystr, HttpServletRequest request) {
        String stemParam = (String) request.getParameter("stem"); 
        boolean stem = "true".equals(stemParam);
        
        String tokenizeParam = (String) request.getParameter("stem"); 
        boolean tokenize = "true".equals(tokenizeParam);
        
        // The search index is lowercased
        querystr = querystr.toLowerCase();
        
        // If the last token of the query string ends in a word-delimiting character
        // it should not get a wildcard query term.
        // E.g., "Dickens," should match "Dickens" but not "Dickenson"
        // This test might need to be moved to makeNameQuery().
        Pattern p = Pattern.compile("\\W$");
        Matcher m = p.matcher(querystr);
        boolean lastTermIsWildcard = !m.find();
        
        // Stemming is only relevant if we are tokenizing. An untokenized name
        // query will not stem.
        if (tokenize) {
            return makeTokenizedNameQuery(querystr, stem, lastTermIsWildcard);
        } else {
            return makeUntokenizedNameQuery(querystr);
        }
    }
    
    private Query makeTokenizedNameQuery(String querystr, boolean stem, boolean lastTermIsWildcard) {
    
        Query query = null;
  
        String termName = stem ? Entity2LuceneDoc.term.NAME : Entity2LuceneDoc.term.NAMEUNSTEMMED;

        List<String> terms = Arrays.asList(querystr.split("[, ]+"));
        for (Iterator<String> i = terms.iterator(); i.hasNext(); ) {
            String term = (String) i.next();
            // All items but last get a regular term query
            if (i.hasNext()) {
                BooleanQuery boolQuery = new BooleanQuery(); 
                boolQuery.add( 
                        new TermQuery(new Term(termName, term)),
                        BooleanClause.Occur.MUST);  
                if (query != null) {
                    boolQuery.add(query, BooleanClause.Occur.MUST);
                }
                query = boolQuery;                          
            }
            // Last item goes on to next block
            else {
                querystr = term;
            }
        }
 
        // Last term
        {
            BooleanQuery boolQuery = new BooleanQuery();            
            if (lastTermIsWildcard) {
                log.debug("Adding wildcard query on last term");
                boolQuery.add( 
                        new WildcardQuery(new Term(termName, querystr + "*")),
                        BooleanClause.Occur.MUST);                 
            } else {
                log.debug("Adding term query on last term");
                boolQuery.add( 
                        new TermQuery(new Term(termName, querystr)),
                        BooleanClause.Occur.MUST);                    
            }
            if (query != null) {
                boolQuery.add(query, BooleanClause.Occur.MUST); 
            }
            query = boolQuery;
        }

        return query;
    }

    private Query makeUntokenizedNameQuery(String querystr) {
        
        String termName = Entity2LuceneDoc.term.NAMEUNANALYZED;
        BooleanQuery query = new BooleanQuery();
        log.debug("Adding wildcard query on unanalyzed name");
        query.add( 
                new WildcardQuery(new Term(termName, querystr + "*")),
                BooleanClause.Occur.MUST);   
        
        return query;
    }
            
    /**
     * Makes a flag based query clause.  This is where searches can filtered
     * by portal.
     *
     * If you think that search is not working correctly with protals and
     * all that kruft then this is a method you want to look at.
     *
     * It only takes into account "the portal flag" and flag1Exclusive must
     * be set.  Where does that stuff get set?  Look in vitro.flags.PortalFlag
     * 
     * One thing to keep in mind with portal filtering and search is that if
     * you want to search a portal that is different then the portal the user
     * is 'in' then the home parameter should be set to force the user into
     * the new portal.  
     * 
     * Ex.  Bob requests the search page for vivo in portal 3.  You want to
     * have a drop down menu so bob can search the all CALS protal, id 60.
     * You need to have a home=60 on your search form. If you don't set 
     * home=60 with your search query, then the search will not be in the
     * all portal AND the WebappDaoFactory will be filtered to only show 
     * things in portal 3.    
     * 
     * Notice: flag1 as a parameter is ignored. bdc34 2009-05-22.
     */
    @SuppressWarnings("static-access")
    private Query makeFlagQuery( PortalFlag flag){        
        if( flag == null || !flag.isFilteringActive() 
                || flag.getFlag1DisplayStatus() == flag.SHOW_ALL_PORTALS )
            return null;

        // make one term for each bit in the numeric flag that is set
        Collection<TermQuery> terms = new LinkedList<TermQuery>();
        int portalNumericId = flag.getFlag1Numeric();        
        Long[] bits = FlagMathUtils.numeric2numerics(portalNumericId);
        for (Long bit : bits) {
            terms.add(new TermQuery(new Term(Entity2LuceneDoc.term.PORTAL, Long
                    .toString(bit))));
        }

        // make a boolean OR query for all of those terms
        BooleanQuery boolQuery = new BooleanQuery();
        if (terms.size() > 0) {
            for (TermQuery term : terms) {
                    boolQuery.add(term, BooleanClause.Occur.SHOULD);
            }
            return boolQuery;
        } else {
            //we have no flags set, so no flag filtering
            return null;
        }
    }

    private synchronized IndexSearcher getIndexSearcher(String indexDir) {
        if( searcher == null ){
            try {                
                Directory fsDir = FSDirectory.getDirectory(indexDir);
                searcher = new IndexSearcher(fsDir);
            } catch (IOException e) {
                log.error("LuceneSearcher: could not make indexSearcher "+e);
                log.error("It is likely that you have not made a directory for the lucene index.  "+
                          "Create the directory indicated in the error and set permissions/ownership so"+
                          " that the tomcat server can read/write to it.");
                //The index directory is created by LuceneIndexer.makeNewIndex()
            }
        }
        return searcher;
    }
    

    private void doNoQuery(String templateName, Map<String, Object> map, Configuration config, HttpServletResponse response) {
        writeTemplate(templateName, map, config, response);
    }

    private void doFailedSearch(String templateName, Map<String, Object> map, Configuration config, HttpServletResponse response) {
        writeTemplate(templateName, map, config, response);
    }
 
    private void doSearchError(String templateName, Map<String, Object> map, Configuration config, HttpServletResponse response) {
        // For now, we are not sending an error message back to the client because with the default autocomplete configuration it
        // chokes.
        writeTemplate(templateName, map, config, response);
    }

    public static final int MAX_QUERY_LENGTH = 500;

    public class SearchResult implements Comparable<Object> {
        private String label;
        private String uri;
        
        SearchResult(String label, String value) {
            this.label = label;
            this.uri = value;
        }
        
        public String getLabel() {
            return label;
        }
        
        public String getUri() {
            return uri;
        }
        
        public String getJson() {
            return "{ \"label\": \"" + label + "\", " + "\"uri\": \"" + uri + "\" }";
        }

        public int compareTo(Object o) throws ClassCastException {
            if ( !(o instanceof SearchResult) ) {
                throw new ClassCastException("Error in SearchResult.compareTo(): expected SearchResult object.");
            }
            SearchResult sr = (SearchResult) o;
            return label.compareTo(sr.getLabel());
        }
    }
    
    
    /**
     * Need to accept notification from indexer that the index has been changed.
     */
    public void close() {
        searcher = null;        
    }

    public VitroHighlighter getHighlighter(VitroQuery q) {
        throw new Error("PagedSearchController.getHighlighter() is unimplemented");
    }

    public VitroQueryFactory getQueryFactory() {
        throw new Error("PagedSearchController.getQueryFactory() is unimplemented");
    }

    public List search(VitroQuery query) throws SearchException {
        throw new Error("PagedSearchController.search() is unimplemented");
    }

}
