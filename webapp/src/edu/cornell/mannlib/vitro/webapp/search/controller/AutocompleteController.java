/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreeMarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.flags.PortalFlag;
import edu.cornell.mannlib.vitro.webapp.search.SearchException;
import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneIndexFactory;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneSetup;
import edu.cornell.mannlib.vitro.webapp.utils.FlagMathUtils;
import freemarker.template.Configuration;

/**
 * AutocompleteController is used to generate autocomplete and select element content
 * through a Lucene search. 
 */

public class AutocompleteController extends FreeMarkerHttpServlet{

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(AutocompleteController.class);

    private static String QUERY_PARAMETER_NAME = "term";
    
    private IndexSearcher searcher = null;
    String NORESULT_MSG = "";    
    private int defaultMaxSearchSize= 1000;

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
            
            String qtxt = vreq.getParameter(QUERY_PARAMETER_NAME);
            Analyzer analyzer = getAnalyzer(getServletContext());
            
            Query query = getQuery(vreq, portalFlag, analyzer,  qtxt);             
            log.debug("query for '" + qtxt +"' is " + query.toString());

            if (query == null ) {
                doNoQuery(templateName, map, config, response);
                return;
            }
            
            IndexSearcher searcherForRequest = LuceneIndexFactory.getIndexSearcher(getServletContext());
                                                
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
                       Analyzer analyzer, String querystr) throws SearchException{
        
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

            query = makeNameQuery(querystr, analyzer, request);
            
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

            //if we have a flag/portal query then we add
            //it by making a BooelanQuery.
            // RY 7/24/10 Temporarily commenting out for now because we're suddenly getting portal:2
            // thrown onto the query. Will need to investigate post-launch of NIHVIVO 1.1.
//            Query flagQuery = makeFlagQuery( portalState );
//            if( flagQuery != null ){
//                BooleanQuery boolQuery = new BooleanQuery();
//                boolQuery.add( query, BooleanClause.Occur.MUST);
//                boolQuery.add( flagQuery, BooleanClause.Occur.MUST);
//                query = boolQuery;
//            }
            
        } catch (Exception ex){
            throw new SearchException(ex.getMessage());
        }

        return query;
    }
    
    private Query makeNameQuery(String querystr, Analyzer analyzer, HttpServletRequest request) {

        String tokenizeParam = (String) request.getParameter("tokenize"); 
        boolean tokenize = "true".equals(tokenizeParam);
        
        // Note: Stemming is only relevant if we are tokenizing: an untokenized name
        // query will not be stemmed. So we don't look at the stem parameter until we get to
        // makeTokenizedNameQuery().
        if (tokenize) {
            return makeTokenizedNameQuery(querystr, analyzer, request);
        } else {
            return makeUntokenizedNameQuery(querystr);
        }
    }
    
    private Query makeTokenizedNameQuery(String querystr, Analyzer analyzer, HttpServletRequest request) {
 
        String stemParam = (String) request.getParameter("stem"); 
        boolean stem = "true".equals(stemParam);
        String termName = stem ? Entity2LuceneDoc.term.NAME : Entity2LuceneDoc.term.NAMEUNSTEMMED;

        BooleanQuery boolQuery = new BooleanQuery();
        
        // Use the query parser to analyze the search term the same way the indexed text was analyzed.
        // For example, text is lowercased, and function words are stripped out.
        QueryParser parser = getQueryParser(termName, analyzer);
        
        // The wildcard query doesn't play well with stemming. Query term name:tales* doesn't match
        // "tales", which is indexed as "tale", while query term name:tales does. Obviously we need 
        // the wildcard for name:tal*, so the only way to get them all to match is use a disjunction 
        // of wildcard and non-wildcard queries. The query will look have only an implicit disjunction
        // operator: e.g., +(name:tales name:tales*)
        try {
            log.debug("Adding non-wildcard query for " + querystr);
            Query query = parser.parse(querystr);  
            boolQuery.add(query, BooleanClause.Occur.SHOULD);

            // Prevent ParseException here when adding * after a space.
            // If there's a space at the end, we don't need the wildcard query.
            if (! querystr.endsWith(" ")) {
                log.debug("Adding wildcard query for " + querystr);
                Query wildcardQuery = parser.parse(querystr + "*");            
                boolQuery.add(wildcardQuery, BooleanClause.Occur.SHOULD);
            }
            
            log.debug("Name query is: " + boolQuery.toString());
        } catch (ParseException e) {
            log.warn(e, e);
        }
        
        
        return boolQuery;
        
/*       
        Query query = null;
        
        // The search index is lowercased
        querystr = querystr.toLowerCase();
        
        List<String> terms = Arrays.asList(querystr.split("[, ]+"));
        for (Iterator<String> i = terms.iterator(); i.hasNext(); ) {
            String term = (String) i.next();
            BooleanQuery boolQuery = new BooleanQuery(); 
            // All items but last get a regular term query
            if (i.hasNext()) {                
                boolQuery.add( 
                        new TermQuery(new Term(termName, term)),
                        BooleanClause.Occur.MUST);  
                if (query != null) {
                    boolQuery.add(query, BooleanClause.Occur.MUST);
                }
                query = boolQuery;                          
            }
            // Last term
            else {
                // If the last token of the query string ends in a word-delimiting character
                // it should not get a wildcard query term.
                // E.g., "Dickens," should match "Dickens" but not "Dickenson"
                Pattern p = Pattern.compile("\\W$");
                Matcher m = p.matcher(querystr);
                boolean lastTermIsWildcard = !m.find();
                
                if (lastTermIsWildcard) {
                    log.debug("Adding wildcard query on last term");
                    boolQuery.add( 
                            new WildcardQuery(new Term(termName, term + "*")),
                            BooleanClause.Occur.MUST);                 
                } else {
                    log.debug("Adding term query on last term");
                    boolQuery.add( 
                            new TermQuery(new Term(termName, term)),
                            BooleanClause.Occur.MUST);                    
                }
                if (query != null) {
                    boolQuery.add(query, BooleanClause.Occur.MUST); 
                }
                query = boolQuery;
            }
        }
        return query;
*/        
    }

    private Query makeUntokenizedNameQuery(String querystr) {
        
        querystr = querystr.toLowerCase();
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

    private QueryParser getQueryParser(String searchField, Analyzer analyzer){
        // searchField indicates which field to search against when there is no term
        // indicated in the query string.
        // The analyzer is needed so that we use the same analyzer on the search queries as
        // was used on the text that was indexed.
        QueryParser qp = new QueryParser(searchField,analyzer);
        //this sets the query parser to AND all of the query terms it finds.
        qp.setDefaultOperator(QueryParser.AND_OPERATOR);
        return qp;
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

}
