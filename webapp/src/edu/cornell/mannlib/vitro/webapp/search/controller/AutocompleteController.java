/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseBasicAjaxControllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.VitroAjaxController;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.search.solr.SolrSetup;

/**
 * AutocompleteController generates autocomplete content
 * via the search index. 
 */

public class AutocompleteController extends VitroAjaxController {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(AutocompleteController.class);
    
    //private static final String TEMPLATE_DEFAULT = "autocompleteResults.ftl";
    
    private static final String PARAM_QUERY = "term";
    private static final String PARAM_RDFTYPE = "type";
    private static final String PARAM_MULTIPLE_RDFTYPE = "multipleTypes";

    
    String NORESULT_MSG = "";    
    private static final int DEFAULT_MAX_HIT_COUNT = 1000; 

    public static final int MAX_QUERY_LENGTH = 500;
    
    @Override
    protected Actions requiredActions(VitroRequest vreq) {
    	return new Actions(new UseBasicAjaxControllers());
    }
    
    @Override
    protected void doRequest(VitroRequest vreq, HttpServletResponse response)
        throws IOException, ServletException {
        
        try {
            
            String qtxt = vreq.getParameter(PARAM_QUERY);
            
            SolrQuery query = getQuery(qtxt, vreq);             
            if (query == null ) {
                log.debug("query for '" + qtxt +"' is null.");
                doNoQuery(response);
                return;
            }
            log.debug("query for '" + qtxt +"' is " + query.toString());
                        
            SolrServer solr = SolrSetup.getSolrServer(getServletContext());
            QueryResponse queryResponse = solr.query(query);

            if ( queryResponse == null) {
                log.error("Query response for a search was null");                
                doNoSearchResults(response);
                return;
            }
            
            SolrDocumentList docs = queryResponse.getResults();

            if ( docs == null) {
                log.error("Docs for a search was null");                
                doNoSearchResults(response);
                return;
            }
            
            long hitCount = docs.getNumFound();
            log.debug("Total number of hits = " + hitCount);
            if ( hitCount < 1 ) {                
                doNoSearchResults(response);
                return;
            }            

            List<SearchResult> results = new ArrayList<SearchResult>();
            for (SolrDocument doc : docs) {
                try {                                      
                    String uri = doc.get(VitroSearchTermNames.URI).toString();
                    // RY 7/1/2011
                    // Comment was: VitroSearchTermNames.NAME_RAW is a multivalued field, so doc.get() returns a list.
                    // Changed to: VitroSearchTermNames.NAME_RAW is a multivalued field, so doc.get() could return a list
                    // But in fact: I'm no longer seeing any lists returned for individuals with multiple labels. Not sure
                    // if this is new behavior or what. ???
                    Object nameRaw = doc.get(VitroSearchTermNames.NAME_RAW);
                    String name = null;
                    if (nameRaw instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<String> nameRawList = (List<String>) nameRaw;
                        name = nameRawList.get(0);
                    } else {
                        name = (String) nameRaw;
                    }
                    SearchResult result = new SearchResult(name, uri);
                    results.add(result);
                } catch(Exception e){
                    log.error("problem getting usable individuals from search " +
                            "hits" + e.getMessage());
                }
            }   

            Collections.sort(results);
            
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

    private SolrQuery getQuery(String queryStr, VitroRequest vreq) {
       
        if ( queryStr == null) {
            log.error("There was no parameter '"+ PARAM_QUERY            
                +"' in the request.");                
            return null;
        } else if( queryStr.length() > MAX_QUERY_LENGTH ) {
            log.debug("The search was too long. The maximum " +
                    "query length is " + MAX_QUERY_LENGTH );
            return null;
        }
                   
        SolrQuery query = new SolrQuery();
        query.setStart(0)
             .setRows(DEFAULT_MAX_HIT_COUNT);  
        
        setNameQuery(query, queryStr, vreq);
        
        // Filter by type
        String typeParam = (String) vreq.getParameter(PARAM_RDFTYPE);
        String multipleTypesParam = (String) vreq.getParameter(PARAM_MULTIPLE_RDFTYPE);
        if (typeParam != null) {
        	addFilterQuery(query, typeParam,  multipleTypesParam);
        }   
        
        query.setFields(VitroSearchTermNames.NAME_RAW, VitroSearchTermNames.URI); // fields to retrieve
       
        // Can't sort on multivalued field, so we sort the results in Java when we get them.
        // query.setSortField(VitroSearchTermNames.NAME_LOWERCASE, SolrQuery.ORDER.asc);
        
        return query;
    }
    
    private void addFilterQuery(SolrQuery query, String typeParam, String multipleTypesParam) {
    	if(multipleTypesParam == null || multipleTypesParam.equals("null") || multipleTypesParam.isEmpty()) {
    		//Single type parameter, process as usual
            query.addFilterQuery(VitroSearchTermNames.RDFTYPE + ":\"" + typeParam + "\"");
    	} else {
    		//Types should be comma separated
    		String[] typeParams = typeParam.split(",");
    		int len = typeParams.length;
    		int i;
    		List<String> filterQueries = new ArrayList<String>();
    		
    		for(i = 0; i < len; i++) {
    			filterQueries.add(VitroSearchTermNames.RDFTYPE + ":\"" + typeParams[i] + "\" ");
    		}
    		String filterQuery = StringUtils.join(filterQueries, " OR ");
    		query.addFilterQuery(filterQuery);
    	}

		
	}

	private void setNameQuery(SolrQuery query, String queryStr, HttpServletRequest request) {

        if (StringUtils.isBlank(queryStr)) {
            log.error("No query string");
        }
        
        String tokenizeParam = (String) request.getParameter("tokenize"); 
        boolean tokenize = "true".equals(tokenizeParam);
        
        // Note: Stemming is only relevant if we are tokenizing: an untokenized name
        // query will not be stemmed. So we don't look at the stem parameter until we get to
        // setTokenizedNameQuery().
        if (tokenize) {
            setTokenizedNameQuery(query, queryStr, request);
        } else {
            setUntokenizedNameQuery(query, queryStr);
        }
    }
    
    private void setTokenizedNameQuery(SolrQuery query, String queryStr, HttpServletRequest request) {
 
        /* We currently have no use case for a tokenized, unstemmed autocomplete search field, so the option
         * has been disabled. If needed in the future, will need to add a new field and field type which
         * is like AC_NAME_STEMMED but doesn't include the stemmer.
        String stemParam = (String) request.getParameter("stem"); 
        boolean stem = "true".equals(stemParam);
        if (stem) {
            String acTermName = VitroSearchTermNames.AC_NAME_STEMMED;
            String nonAcTermName = VitroSearchTermNames.NAME_STEMMED;
        } else {
            String acTermName = VitroSearchTermNames.AC_NAME_UNSTEMMED;
            String nonAcTermName = VitroSearchTermNames.NAME_UNSTEMMED;        
        }
        */
        
        String acTermName = VitroSearchTermNames.AC_NAME_STEMMED;
        String nonAcTermName = VitroSearchTermNames.NAME_STEMMED;
        String acQueryStr;
        
        if (queryStr.endsWith(" ")) {
            acQueryStr = makeTermQuery(nonAcTermName, queryStr, true);    
        } else {
            int indexOfLastWord = queryStr.lastIndexOf(" ") + 1;
            List<String> terms = new ArrayList<String>(2);
            
            String allButLastWord = queryStr.substring(0, indexOfLastWord);
            if (StringUtils.isNotBlank(allButLastWord)) {
                terms.add(makeTermQuery(nonAcTermName, allButLastWord, true));
            }
            
            String lastWord = queryStr.substring(indexOfLastWord);
            if (StringUtils.isNotBlank(lastWord)) {
                terms.add(makeTermQuery(acTermName, lastWord, false));
            }
            
            acQueryStr = StringUtils.join(terms, " AND ");
        }
        
        log.debug("Tokenized name query string = " + acQueryStr);
        query.setQuery(acQueryStr);

    }

    private void setUntokenizedNameQuery(SolrQuery query, String queryStr) {        
        queryStr = queryStr.trim();       
        queryStr = makeTermQuery(VitroSearchTermNames.AC_NAME_UNTOKENIZED, queryStr, true);
        query.setQuery(queryStr);
    }
    
    private String makeTermQuery(String term, String queryStr, boolean mayContainWhitespace) {
        if (mayContainWhitespace) {
            queryStr = "\"" + escapeWhitespaceInQueryString(queryStr) + "\"";
        }
        return term + ":" + queryStr;
    }
    
    private String escapeWhitespaceInQueryString(String queryStr) {
        // Solr wants whitespace to be escaped with a backslash
        return queryStr.replaceAll("\\s+", "\\\\ ");
    }
            
    private void doNoQuery(HttpServletResponse response) throws IOException  {
        // For now, we are not sending an error message back to the client because 
        // with the default autocomplete configuration it chokes.
        doNoSearchResults(response);
    }

    private void doSearchError(HttpServletResponse response) throws IOException {
        // For now, we are not sending an error message back to the client because 
        // with the default autocomplete configuration it chokes.
        doNoSearchResults(response);
    }

    private void doNoSearchResults(HttpServletResponse response) throws IOException {
        response.getWriter().write("[]");
    }
    
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
