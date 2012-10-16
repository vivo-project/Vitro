/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.solr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListQueryResults;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController.SearchException;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.search.solr.SolrSetup;

/**
 * Some static method to help in constructing Solr queries and parsing the
 * results.
 */

public class SolrQueryUtils {
	private static final Log log = LogFactory.getLog(SolrQueryUtils.class.getName());

	public enum Conjunction {
		AND, OR;

		public String joiner() {
			return " " + this.name() + " ";
		}
	}

	/**
	 * Create an AutoCompleteWords object that can be used to build an
	 * auto-complete query.
	 */
	public static AutoCompleteWords parseForAutoComplete(String searchTerm,
			String delimiterPattern) {
		return new AutoCompleteWords(searchTerm, delimiterPattern);
	}

	/**
	 * Create a builder object that can assemble a map of Solr field names to
	 * JSON field names.
	 */
	public static FieldMap fieldMap() {
		return new FieldMap();
	}

	/**
	 * Parse a response into a list of maps, one map for each document.
	 * 
	 * The Solr field names in the document are replaced by json field names in
	 * the result, according to the fieldMap.
	 */
	public static List<Map<String, String>> parseResponse(
			QueryResponse queryResponse, FieldMap fieldMap) {
		return new SolrResultsParser(queryResponse, fieldMap).parse();
	}

	/**
	 * Parse a response into a list of maps, accepting only those maps that pass
	 * a filter, and only up to a maximum number of records.
	 * 
	 * The Solr field names in the document are replaced by json field names in
	 * the result, according to the fieldMap.
	 */
	public static List<Map<String, String>> parseAndFilterResponse(
			QueryResponse queryResponse, FieldMap fieldMap,
			SolrResponseFilter filter, int maxNumberOfResults) {
		return new SolrResultsParser(queryResponse, fieldMap)
				.parseAndFilterResponse(filter, maxNumberOfResults);
	}

	/**
	 * Break a string into a list of words, according to a RegEx delimiter. Trim
	 * leading and trailing white space from each word.
	 */
	public static List<String> parseWords(String typesString,
			String wordDelimiter) {
		List<String> list = new ArrayList<String>();
		String[] array = typesString.split(wordDelimiter);
		for (String word : array) {
			String trimmed = word.trim();
			if (!trimmed.isEmpty()) {
				list.add(trimmed);
			}
		}
		return list;
	}

	/**
	 * Glue these words together into a query on a given field, joined by either
	 * AND or OR.
	 */
	public static String assembleConjunctiveQuery(String fieldName,
			Collection<String> words, Conjunction c) {
		List<String> terms = new ArrayList<String>();
		for (String word : words) {
			terms.add(buildTerm(fieldName, word));
		}
		String q = StringUtils.join(terms, c.joiner());
		return q;
	}

	private static String buildTerm(String fieldName, String word) {
		return fieldName + ":\"" + word + "\"";
	}
	
	/**
	 * Methods that can be used in multiple places, such as IndividualListController and SolrIndividualsDataGetter
	 */
	
	public static String getAlphaParameter(VitroRequest request){
        return request.getParameter("alpha");
    }
    
    public static int getPageParameter(VitroRequest request) {
        String pageStr = request.getParameter("page");
        if( pageStr != null ){
            try{
                return Integer.parseInt(pageStr);                
            }catch(NumberFormatException nfe){
                log.debug("could not parse page parameter");
                return 1;
            }                
        }else{                   
            return 1;
        }
    }
	
	//Get count of individuals without actually getting the results
    public static long getIndividualCount(List<String> vclassUris, IndividualDao indDao, ServletContext context) {    	    	       
       SolrQuery query = new SolrQuery(makeMultiClassQuery(vclassUris));
       query.setRows(0);
    	try {    	              
            SolrServer solr = SolrSetup.getSolrServer(context);
            QueryResponse response = null;                      
            response = solr.query(query);            
            return response.getResults().getNumFound();                        
    	} catch(Exception ex) {
    		log.error("An error occured in retrieving individual count", ex);
    	}
    	return 0;
    }
	
	/**
     * builds a query with a type clause for each type in vclassUris, NAME_LOWERCASE filetred by
     * alpha, and just the hits for the page for pageSize.
     */
    public static SolrQuery getQuery(List<String> vclassUris, String alpha, int page, int pageSize){
        String queryText = "";
        
        try {            
            queryText = makeMultiClassQuery(vclassUris);
            
        	 // Add alpha filter if applicable
            if ( alpha != null && !"".equals(alpha) && alpha.length() == 1) {      
                queryText += VitroSearchTermNames.NAME_LOWERCASE + ":" + alpha.toLowerCase() + "*";
            }     
            
            SolrQuery query = new SolrQuery(queryText);

            //page count starts at 1, row count starts at 0
            int startRow = (page-1) * pageSize ;            
            query.setStart( startRow ).setRows( pageSize );
            
            // Need a single-valued field for sorting
            query.setSortField(VitroSearchTermNames.NAME_LOWERCASE_SINGLE_VALUED, SolrQuery.ORDER.asc);

            log.debug("Query is " + query.toString());
            return query;
            
        } catch (Exception ex){
            log.error("Could not make Solr query",ex);
            return new SolrQuery();        
        }      
    }    

    public static String makeMultiClassQuery( List<String> vclassUris){
        List<String> queryTypes = new ArrayList<String>();  
        try {            
            // query term for rdf:type - multiple types possible
            for(String vclassUri: vclassUris) {
                queryTypes.add(VitroSearchTermNames.RDFTYPE + ":\"" + vclassUri + "\" ");
            }           
			return StringUtils.join(queryTypes, " AND ");
        } catch (Exception ex){
            log.error("Could not make Solr query",ex);
            return "";
        }            
    }
    
    public static IndividualListQueryResults buildAndExecuteVClassQuery(
			List<String> vclassURIs, String alpha, int page, int pageSize,
			ServletContext context, IndividualDao indDao)
			throws SolrServerException {
		 SolrQuery query = SolrQueryUtils.getQuery(vclassURIs, alpha, page, pageSize);
		 IndividualListQueryResults results = IndividualListQueryResults.runQuery(query, indDao, context);
		 log.debug("Executed solr query for " + vclassURIs);
		 if (results.getIndividuals().isEmpty()) { 
			 log.debug("entities list is null for vclass " + vclassURIs);
		 }
		return results;
	}

}
