/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import org.apache.lucene.util.Version;
import org.json.JSONArray;

import com.hp.hpl.jena.sparql.lib.org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseBasicAjaxControllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.VitroAjaxController;
import edu.cornell.mannlib.vitro.webapp.search.SearchException;
import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc.VitroLuceneTermNames;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneIndexFactory;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneSetup;

/**
 * AutocompleteController generates autocomplete content
 * through a Lucene search. 
 */
public class AutocompleteController extends VitroAjaxController {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(AutocompleteController.class);
    
    //private static final String TEMPLATE_DEFAULT = "autocompleteResults.ftl";
    
    private static String QUERY_PARAMETER_NAME = "term";
    
    String NORESULT_MSG = "";    
    private int defaultMaxSearchSize= 1000;

    @Override
    protected Actions requiredActions(VitroRequest vreq) {
    	return new Actions(new UseBasicAjaxControllers());
    }
    
    @Override
    protected void doRequest(VitroRequest vreq, HttpServletResponse response)
        throws IOException, ServletException {
        
        try {

            int maxHitSize = defaultMaxSearchSize;
            
            String qtxt = vreq.getParameter(QUERY_PARAMETER_NAME);
            Analyzer analyzer = getAnalyzer(getServletContext());
            
            Query query = getQuery(vreq, analyzer, qtxt);             
            if (query == null ) {
                log.debug("query for '" + qtxt +"' is null.");
                doNoQuery(response);
                return;
            }
            log.debug("query for '" + qtxt +"' is " + query.toString());
                        
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
                }catch (Exception e){
                    log.error(e, e);
                    doNoSearchResults(response);
                    return;
                }
            }

            if( topDocs == null || topDocs.scoreDocs == null){
                log.error("topDocs for a search was null");                
                doNoSearchResults(response);
                return;
            }
            
            int hitsLength = topDocs.scoreDocs.length;
            if ( hitsLength < 1 ){                
                doNoSearchResults(response);
                return;
            }            
            log.debug("found "+hitsLength+" hits"); 

            List<SearchResult> results = new ArrayList<SearchResult>();
            for(int i=0; i<topDocs.scoreDocs.length ;i++){
                try{                     
                    Document doc = searcherForRequest.doc(topDocs.scoreDocs[i].doc);                    
                    String uri = doc.get(VitroLuceneTermNames.URI);
                    String name = doc.get(VitroLuceneTermNames.NAMERAW);
                    SearchResult result = new SearchResult(name, uri);
                    results.add(result);
                } catch(Exception e){
                    log.error("problem getting usable Individuals from search " +
                            "hits" + e.getMessage());
                }
            }   

            Collections.sort(results);
            
            // map.put("results", results);
            // writeTemplate(TEMPLATE_DEFAULT, map, config, vreq, response);
            
            JSONArray jsonArray = new JSONArray();
            for (SearchResult result : results) {
                jsonArray.put(result.toMap());
            }
            response.getWriter().write(jsonArray.toString());

        
        } catch (Throwable e) {
            log.error(e, e);            
            doSearchError(response);
        }
    }

    private Analyzer getAnalyzer(ServletContext servletContext) throws SearchException {
        Object obj = servletContext.getAttribute(LuceneSetup.ANALYZER);
        if( obj == null || !(obj instanceof Analyzer) )
            throw new SearchException("Could not get analyzer");
        else
            return (Analyzer)obj;        
    }

    private Query getQuery(VitroRequest vreq, Analyzer analyzer, 
    			String querystr) throws SearchException{
        
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

            query = makeNameQuery(querystr, analyzer, vreq);
            
            // Filter by type
            {
                BooleanQuery boolQuery = new BooleanQuery(); 
                String typeParam = (String) vreq.getParameter("type");
                boolQuery.add(  new TermQuery(
                        new Term(VitroLuceneTermNames.RDFTYPE, 
                                typeParam)),
                    BooleanClause.Occur.MUST);
                boolQuery.add(query, BooleanClause.Occur.MUST);
                query = boolQuery;
            }
            
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
        String termName = stem ? VitroLuceneTermNames.NAME : VitroLuceneTermNames.NAMEUNSTEMMED;

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
    }

    private Query makeUntokenizedNameQuery(String querystr) {
        
        querystr = querystr.toLowerCase();
        String termName = VitroLuceneTermNames.NAMELOWERCASE;
        BooleanQuery query = new BooleanQuery();
        log.debug("Adding wildcard query on unanalyzed name");
        query.add( 
                new WildcardQuery(new Term(termName, querystr + "*")),
                BooleanClause.Occur.MUST);   
        
        return query;
    }
            
    private QueryParser getQueryParser(String searchField, Analyzer analyzer){
        // searchField indicates which field to search against when there is no term
        // indicated in the query string.
        // The analyzer is needed so that we use the same analyzer on the search queries as
        // was used on the text that was indexed.
        QueryParser qp = new QueryParser(Version.LUCENE_29, searchField,analyzer);
        //this sets the query parser to AND all of the query terms it finds.
        qp.setDefaultOperator(QueryParser.AND_OPERATOR);
        return qp;
    }

    private void doNoQuery(HttpServletResponse response) throws IOException  {
        // For now, we are not sending an error message back to the client because with the default autocomplete configuration it
        // chokes.
        doNoSearchResults(response);
    }

    private void doSearchError(HttpServletResponse response) throws IOException {
        // For now, we are not sending an error message back to the client because with the default autocomplete configuration it
        // chokes.
        doNoSearchResults(response);
    }

    private void doNoSearchResults(HttpServletResponse response) throws IOException {
        response.getWriter().write("[]");
    }
    
    public static final int MAX_QUERY_LENGTH = 500;

    public class SearchResult implements Comparable<Object> {
        private String label;
        private String uri;
        
        SearchResult(String label, String uri) {
            this.label = label;
            this.uri = uri;
        }
        
        public String getLabel() {
            return label;
        }
        
        public String getJsonLabel() {
            return JSONObject.quote(label);
        }
        
        public String getUri() {
            return uri;
        }
        
        public String getJsonUri() {
            return JSONObject.quote(uri);
        }
        
        Map<String, String> toMap() {
            Map<String, String> map = new HashMap<String, String>();
            map.put("label", label);
            map.put("uri", uri);
            return map;
        }

        public int compareTo(Object o) throws ClassCastException {
            if ( !(o instanceof SearchResult) ) {
                throw new ClassCastException("Error in SearchResult.compareTo(): expected SearchResult object.");
            }
            SearchResult sr = (SearchResult) o;
            return label.compareToIgnoreCase(sr.getLabel());
        }
    }

}
