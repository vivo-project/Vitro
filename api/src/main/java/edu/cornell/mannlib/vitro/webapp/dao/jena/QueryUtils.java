/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.atlas.io.StringWriterI;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;

/** 
 * Utilities for executing queries and working with query results. 
 * 
 */

public class QueryUtils {
    
    private static final Log log = LogFactory.getLog(QueryUtils.class);
    
    private QueryUtils() { }
    
    public static Map<String,Object> querySolutionToObjectValueMap( QuerySolution soln){
        Map<String,Object> map = new HashMap<String,Object>();
        Iterator<String> varNames = soln.varNames();
        while(varNames.hasNext()){
            String varName = varNames.next();
            Object value = nodeToObject( soln.get(varName));
            log.debug("Adding " + varName + " : " + value + " to query solution data.");            
            map.put(varName, value);
        }
        return map;
    }
 
    public static Map<String,String> querySolutionToStringValueMap( QuerySolution soln ){
        Map<String,String> map = new HashMap<String,String>();
        Iterator<String> varNames = soln.varNames();
        while(varNames.hasNext()){
            String varName = varNames.next();
            String value = nodeToString( soln.get(varName));
            log.debug("Adding " + varName + " : " + value + " to query solution data.");
            map.put(varName, value);           
        }
        return map;
    }
    
	/**
	 * If any pair of maps in the list has the same (non-null) value for any of
	 * these keys, call the maps duplicates and keep only the first of them.
	 */
	public static List<Map<String, String>> removeDuplicatesMapsFromList(
			List<Map<String, String>> rawList, String... keys) {
		List<Map<String, String>> filteredList = new ArrayList<>();
		outerLoop: for (Map<String, String> rawMap : rawList) {
			for (Map<String, String> filteredMap : filteredList) {
				for (String key : keys) {
					String rawValue = rawMap.get(key);
					if (rawValue != null) {
						if (rawValue.equals(filteredMap.get(key))) {
							if (log.isDebugEnabled()) {
								logDuplicateRows(rawMap, filteredMap, keys);
							}
							continue outerLoop;
						}
					}
				}
			}
			filteredList.add(rawMap);
		}
		return filteredList;
	}

	private static void logDuplicateRows(Map<String, String> rawMap,
			Map<String, String> filteredMap, String... keys) {
		log.debug("Found duplicate rows, by at least one of these keys: "
				+ Arrays.toString(keys) + ". Keeping " + filteredMap
				+ ". Discarding " + rawMap + ".");
	}
    
    public static Object nodeToObject( RDFNode node ){
        if( node == null ){
            return "";
        }else if( node.isLiteral() ){
            Literal literal = node.asLiteral();
            return literal.getValue();
        }else if( node.isURIResource() ){
            Resource resource = node.asResource();
            return resource.getURI();
        }else if( node.isAnon() ){  
            Resource resource = node.asResource();
            return resource.getId().getLabelString(); //get b-node id
        }else{
            return "";
        }
    }

    public static String nodeToString( RDFNode node ){
        if( node == null ){
            return "";
        }else if( node.isLiteral() ){
            Literal literal = node.asLiteral();
            return literal.getLexicalForm();
        }else if( node.isURIResource() ){
            Resource resource = node.asResource();
            return resource.getURI();
        }else if( node.isAnon() ){  
            Resource resource = node.asResource();
            return resource.getId().getLabelString(); //get b-node id
        }else{
            return "";
        }
    }
    
    /** Manually replace query variables with uris when prebinding causes the query to fail, probably
     * due to a Jena bug.
     */
    public static String subUrisForQueryVars(String queryString, Map<String, String> varsToUris) {
        
        for (String var : varsToUris.keySet()) {
           queryString = subUriForQueryVar(queryString, var, varsToUris.get(var));
        }
        return queryString;
    }

    /** Manually replace a query variable with a uri when prebinding causes the query to fail, probably
     * due to a Jena bug.
     */
    public static String subUriForQueryVar(String queryString, String varName, String uri) {
        return queryString.replaceAll("\\?" + varName + "\\b", "<" + uri + ">");
    }
    
    /**Replace one variable name with another**/
    public static String replaceQueryVar(String queryString, String varName, String newVarName) {
        return queryString.replaceAll("\\?" + varName + "\\b", "?" + newVarName);
    }
    
    public static ResultSet getQueryResults(String queryStr, VitroRequest vreq) {
        return getQueryResults(queryStr, vreq.getRDFService());
    }

    public static ResultSet getQueryResults(String queryStr, QuerySolution initialBindings, RDFService rdfService) {
    	return getQueryResults(bindVariables(queryStr, initialBindings), rdfService);
    }
    
	public static ResultSet getLanguageNeutralQueryResults(String queryStr, VitroRequest vreq) {
    	return getQueryResults(queryStr, vreq.getUnfilteredRDFService());
    }

    /** Already have the dataset, so process the query and return the results. */
	private static ResultSet getQueryResults(String queryStr, RDFService rdfService) {
	    try {
            return ResultSetFactory.fromJSON(
                    rdfService.sparqlSelectQuery(queryStr, RDFService.ResultFormat.JSON));
	    } catch (RDFServiceException e) {
	        throw new RuntimeException(e);
	    }
	}

	/**
	 * The RDFService interface doesn't support initial bindings, so do text
	 * substitutions instead.
	 */
	public static String bindVariables(String queryStr,
			QuerySolution initialBindings) {
		String bound = queryStr;
		for (Iterator<String> it = initialBindings.varNames(); it.hasNext();) {
			String name = it.next();
			RDFNode node = initialBindings.get(name);
			if (node.isLiteral()) {
				bound = bound.replace('?' + name, literalToString(node.asLiteral()));
			} else if (node.isURIResource()) {
				bound = bound.replace('?' + name,  '<'+node.asResource().getURI()+ '>');
			}else {
				log.warn("Failed to bind anonymous resource variable '" + name
						+ "' to query '" + bound + "'");
			}
		}
		return bound;
	}

	private static String literalToString(Literal l) {
        StringWriterI sw = new StringWriterI();
        NodeFormatter fmt = new NodeFormatterTTL(null, null);
        fmt.formatLiteral(sw, l.asNode());
        return sw.toString();
	}


}
