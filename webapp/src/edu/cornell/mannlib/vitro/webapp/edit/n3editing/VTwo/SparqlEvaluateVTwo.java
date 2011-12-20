/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SparqlEvaluateVTwo {
    private static Log log = LogFactory.getLog( SparqlEvaluateVTwo.class );

    Model model;
    public SparqlEvaluateVTwo(Model model){
        if( model == null ) throw new Error("SparqlEvaluate must be passed a Model");
         this.model = model;
    }

    public void evaluateForAdditionalUris( EditConfigurationVTwo editConfig ){
    	log.debug("Evaluating for Additional URIS");
        Map<String,List<String>> varsToUris = sparqlEvaluateForUris(editConfig, editConfig.getSparqlForAdditionalUrisInScope());
        editConfig.getUrisInScope().putAll(varsToUris);
    }

    public void evalulateForAdditionalLiterals( EditConfigurationVTwo editConfig ){
    	log.debug("Evaluating for Additional Literals");
        Map<String,List<Literal>> varsToLiterals = sparqlEvaluateForLiterals(editConfig, editConfig.getSparqlForAdditionalLiteralsInScope());
        editConfig.getLiteralsInScope().putAll(varsToLiterals);
    }

    public void evaluateForExistingUris( EditConfigurationVTwo editConfig){
    	log.debug("Evaluating for existing URIS");
        Map<String,List<String>> varsToUris = sparqlEvaluateForUris(editConfig, editConfig.getSparqlForExistingUris());
        editConfig.getUrisInScope().putAll(varsToUris);
    }

    public void evaluateForExistingLiterals( EditConfigurationVTwo editConfig){
    	log.debug("Evaluating for existing literals");
        Map<String,List<Literal>> varsToLiterals = sparqlEvaluateForLiterals(editConfig, editConfig.getSparqlForExistingLiterals());
        editConfig.getLiteralsInScope().putAll(varsToLiterals);
    }

//    public  Map<String,String> sparqlEvaluateForExistingToUris(Map<String,String> varToSpqrql){
//        Map<String,String> varToUris = new HashMap<String,String>();
//        for(String var : varToSpqrql.keySet()){
//            varToUris.put(var, queryToUri( varToSpqrql.get(var) ));
//        }
//        return varToUris;
//    }
//
//    public  Map<String,String> sparqlEvaluateForAdditionalLiterals(Map<String,String> varToSpqrql){
//        Map<String,String> varToLiterals = new HashMap<String,String>();
//        for(String var : varToSpqrql.keySet()){
//            varToLiterals.put(var, queryToLiteral( varToSpqrql.get(var) ));
//        }
//        return varToLiterals;
//    }

//    private  Map<String,String> sparqlEvaluateForExistingToUris( EditConfiguration editConfig ) {
//        Map<String,String> varToSpqrql = editConfig.getSparqlForExistingUris();
//        Map<String,String> uriScope = editConfig.getUrisInScope();
//        Map<String,Literal> literalScope = editConfig.getLiteralsInScope();
//
//        Map<String,String> varToUris = new HashMap<String,String>();
//
//        for(String var : varToSpqrql.keySet()){
//            String query = varToSpqrql.get(var);
//            List<String> queryStrings = new ArrayList <String>();
//            queryStrings.add(query);
//            queryStrings= editConfig.getN3Generator().subInUris(uriScope, queryStrings);
//            queryStrings = editConfig.getN3Generator().subInLiterals(literalScope,queryStrings);
//            varToUris.put(var, queryToUri(  queryStrings.get(0) ));           //might result in (key -> null)
//        }
//
//        return varToUris;
//    }

    public  Map<String,List<Literal>> sparqlEvaluateForLiterals( EditConfigurationVTwo editConfig, Map<String,String> varToSparql)  {
        Map<String,List<String>> uriScope = editConfig.getUrisInScope();
        Map<String,List<Literal>> literalScope = editConfig.getLiteralsInScope();

        Map<String,List<Literal>> varToLiterals = new HashMap<String,List<Literal>>();
        for(String var : varToSparql.keySet()){
            String query = varToSparql.get(var);
            log.debug("Var name " + var + " and query = " + query);           
            /* skip if var set to use a system generated value */
            if( query == null || EditConfigurationVTwo.USE_SYSTEM_VALUE.equals( query )) {
            	log.debug("Query is null or using system value so will not continue with rest of method");
                continue;
            }
            List<String> queryStrings = new ArrayList <String>();
            queryStrings.add( query );
            editConfig.getN3Generator().subInMultiUris(uriScope, queryStrings);
            log.debug("Query after substituting uris in scope: " + queryStrings.toString());
            editConfig.getN3Generator().subInMultiLiterals(literalScope,queryStrings);
            log.debug("Query after substituting literals in scope: " + queryStrings.toString());
            varToLiterals.put(var, queryToLiteral(  queryStrings.get(0) ));   //might result in (key -> null)
        }

        return varToLiterals;
    }

    public Map<String,List<String>> sparqlEvaluateForUris( EditConfigurationVTwo editConfig, Map<String,String>varToSparql) {
        Map<String,List<String>> uriScope = editConfig.getUrisInScope();
        Map<String,List<Literal>> literalScope = editConfig.getLiteralsInScope();

        Map<String,List<String>> varToUris = new HashMap<String,List<String>>();

        for(String var : varToSparql.keySet()){
            String query = varToSparql.get(var);
            log.debug("Var name " + var + " and query = " + query);           
            /* skip if var set to use a system generated value */
            if( query == null || EditConfigurationVTwo.USE_SYSTEM_VALUE.equals( query )) {
            	log.debug("Query is null or using system value so will not continue with rest of method");
                continue;
            }
            List<String> queryStrings = new ArrayList <String>();
            queryStrings.add(query);
            editConfig.getN3Generator().subInMultiUris(uriScope, queryStrings);
            log.debug("Query after substituting uris in scope: " + queryStrings.toString());
            editConfig.getN3Generator().subInMultiLiterals(literalScope,queryStrings);
            log.debug("Query after substituting literals in scope: " + queryStrings.toString());
            List<String> uriFromQuery = queryToUri(  queryStrings.get(0) );
            if( uriFromQuery != null )
            {    
            	//Added parens and output
            	varToUris.put(var, uriFromQuery);
            }
            else 
                log.debug("sparqlEvaluateForUris(): for var " + var 
                        + " the following query evaluated to null:\n"+queryStrings.get(0)+"\n(end of query)\n");                            
        }

        return varToUris;
    }

//    public Map<String,Literal> sparqlEvaluateForAdditionalLiterals( EditConfiguration editConfig)  {
//        Map<String,String> varToSpqrql = editConfig.getSparqlForAdditionalLiteralsInScope();
//        Map<String,String> uriScope = editConfig.getUrisInScope();
//        Map<String,Literal> literalScope = editConfig.getLiteralsInScope();
//
//        Map<String,Literal> varToLiterals = new HashMap<String,Literal>();
//        for(String var : varToSpqrql.keySet()){
//            String query = varToSpqrql.get(var);
//            List<String> queryStrings = new ArrayList <String>();
//            queryStrings.add( query );
//            queryStrings= editConfig.getN3Generator().subInUris(uriScope, queryStrings);
//            queryStrings = editConfig.getN3Generator().subInLiterals(literalScope,queryStrings);
//            Literal literalFromQuery = queryToLiteral(  queryStrings.get(0) );
//            if( literalFromQuery != null )
//                varToLiterals.put(var, literalFromQuery );
//            else
//                log.debug("sparqlEvaluateForAdditionalLiterals(): for var " + var 
//                        + "query evaluated to null. query: '" + queryStrings.get(0) +"'");
//        }
//
//        return varToLiterals;
//    }
    
    //now can return multiple uris
    public  List<String> queryToUri(String querystr){
        log.debug("Query string in queryToUri():" + querystr);
        String value = null;
        List<String> values = new ArrayList<String>();
        QueryExecution qe = null;
        try{
            Query query = QueryFactory.create(querystr);
            qe = QueryExecutionFactory.create(query, model);
            if( query.isSelectType() ){
                ResultSet results = null;
                results = qe.execSelect();
                if( results.hasNext()){
                    List<String> vars = results.getResultVars();
                    if( vars == null )
                      throw new Error("sparql had no result variables");
                    for(String var: vars) {
                    	QuerySolution qs = results.nextSolution();
                        Resource resource = qs.getResource(var);
                        value =  resource.getURI();
                        values.add(value);
                    }
                    
                    
                   
                }else{
                	log.debug("Query had no results");
                    return null;
                }
            } else {
                throw new Error("only SELECT type SPARQL queries are supported");
            }
        }catch(Exception ex){
            throw new Error("could not parse SPARQL in queryToUri: \n" + querystr + '\n' + ex.getMessage());
        }finally{
            if( qe != null)
                qe.close();
        }
        if( log.isDebugEnabled() ) log.debug("queryToUri() query: '"+ querystr +"'\nvalue: '" + values.toString() +"'");
        return values;
    }


    public  List<Literal> queryToLiteral(String querystr){
    	log.debug("Executing query " + querystr);
        Literal value = null;
        List<Literal> values = new ArrayList<Literal>();
        QueryExecution qe = null;
        try{
            Query query = QueryFactory.create(querystr);
            qe = QueryExecutionFactory.create(query, model);
            if( query.isSelectType() ){
                ResultSet results = null;
                results = qe.execSelect();
                if( results.hasNext()){
                    List<String> vars = results.getResultVars();
                    if( vars == null )
                        throw new Error("sparql had no result variables");
                    for(String var: vars){
                    	QuerySolution qs = results.nextSolution();
                    	value = qs.getLiteral(var);
                    	values.add(value);
                    }
                }else{
                	log.debug("Query had no results");
                    return null;
                }
            } else {
                throw new Error("only SELECT type SPARQL queries are supported");
            }
        }catch(Exception ex){
            throw new Error("could not parse SPARQL in queryToLiteral: \n" + querystr + '\n' + ex.getMessage());
        }finally{
            if( qe != null)
                qe.close();
        }

        if( log.isDebugEnabled() ) log.debug("queryToLiteral() query: '"+ querystr +"'\nvalue: '" + values.toString() +"'");
        return values;
    }


}
