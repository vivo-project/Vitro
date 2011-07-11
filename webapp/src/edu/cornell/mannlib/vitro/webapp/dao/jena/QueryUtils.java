/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/** 
 * Utilities for executing queries and working with query results. 
 * 
 */

public class QueryUtils {
    
    private static final Log log = LogFactory.getLog(QueryUtils.class);
    
    protected static Map<String,Object> querySolutionToObjectValueMap( QuerySolution soln){
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
 
    protected static Map<String,String> querySolutionToStringValueMap( QuerySolution soln ){
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
    
    protected static Object nodeToObject( RDFNode node ){
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

    protected static String nodeToString( RDFNode node ){
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
    
    public static ResultSet getQueryResults(String queryStr, VitroRequest vreq) {
        
        Dataset dataset = vreq.getDataset();
        dataset.getLock().enterCriticalSection(Lock.READ);
queryStr = " SELECT ?x WHERE { ?x ?p ?y } LIMIT 10";
        QueryExecution qexec = null;
        ResultSet results = null;
        try {
            qexec = QueryExecutionFactory.create(queryStr, dataset);                    
            results = qexec.execSelect();
        } catch (Exception e) {
            log.error(e, e);
        } finally {
            dataset.getLock().leaveCriticalSection();
            if (qexec != null) {
                qexec.close();
            }
        } 
         try {
        /* DEBUGGING */
        int maxRank = 0;
        if (results.hasNext()) { // there is at most one result
            log.debug("found a rank");
            QuerySolution soln = results.next(); 
            RDFNode node = soln.get("rank");
            if (node != null && node.isLiteral()) {
                log.debug("node value =" + node.asLiteral().getLexicalForm());
                try {
                    int rank = node.asLiteral().getInt(); 
                    if (rank > maxRank) {  
                        log.debug("setting maxRank to " + rank);
                        maxRank = rank;
                    }
                } catch (Exception e) {
                    log.error("Error getting int value for rank: " + e.getMessage());
                }
            }
        }
         } catch (Exception e) {
             log.error(e, e);
         }
        
        return results;
    }
    

}
