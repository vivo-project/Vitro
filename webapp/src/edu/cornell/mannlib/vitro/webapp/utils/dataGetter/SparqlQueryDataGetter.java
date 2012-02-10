/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.utils.dataGetter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;

public class SparqlQueryDataGetter implements DataGetter{    
    String pageUri;
    String query;
    String saveToVar;
    String modelUri;
    
    final static Log log = LogFactory.getLog(SparqlQueryDataGetter.class);
    
    @Override
    public Map<String, Object> getData(ServletContext context, VitroRequest vreq, Map<String, Object> pageData) {
        if( pageUri == null )
            throw new IllegalAccessError("configure() must be called before getData()");
                
        return doQuery( vreq.getParameterMap(),getModelToRunQueryOn(context, vreq ));
    }

    @Override
    public void configure(Model model, String dataGetterURI) {
        if( model == null ) 
            throw new IllegalArgumentException("Display Model may not be null.");
        if( dataGetterURI == null )
            throw new IllegalArgumentException("PageUri may not be null.");
                
        this.pageUri = dataGetterURI;        
        
        QuerySolutionMap initBindings = new QuerySolutionMap();
        initBindings.add("pageUri", ResourceFactory.createResource(this.pageUri));
        
        int count = 0;
        Query query = QueryFactory.create(pageQuery) ;               
        model.enterCriticalSection(Lock.READ);
        try{
            QueryExecution qexec = QueryExecutionFactory.create(query, model, initBindings) ;        
            ResultSet res = qexec.execSelect();
            try{                
                while( res.hasNext() ){
                    count++;
                    QuerySolution soln = res.next();
                    
                    //query is not OPTIONAL
                    Literal value = soln.getLiteral("query");
                    if( query == null )
                        log.error("no query defined for page " + this.pageUri);
                    else
                        this.query = value.getLexicalForm();                    
                    
                    //model is OPTIONAL
                    RDFNode node = soln.getResource("model");
                    if( node != null && node.isURIResource() ){
                        this.modelUri = node.asResource().getURI();                        
                    }else if( node != null && node.isLiteral() ){
                        this.modelUri = node.asLiteral().getLexicalForm();                        
                    }
                        
                    //saveToVar is OPTIONAL
                    node = soln.getResource("saveToVar");
                    if( node != null && node.isLiteral() ){
                        this.saveToVar= node.asLiteral().getLexicalForm();                        
                    }
                }
            }finally{ qexec.close(); }
        }finally{ model.leaveCriticalSection(); }                
    }
    
    /**
     * Do the query and return a result. This is in its own method 
     * to make testing easy.
     */
    private Map<String, Object> doQuery(Map<String, String[]>parameterMap, Model queryModel){

        if( this.query == null ){            
            log.error("no SPARQL query defined for page " + this.pageUri);
            //TODO: return an error message?
            return Collections.emptyMap();
        }
        
        Query query = null;
        try{
             query = QueryFactory.create( this.query );
        }catch(Exception ex){
            //TODO: return an error message?
            log.error( "for page " + this.pageUri, ex );
            return Collections.emptyMap();
        }        

        //build query bindings
        QuerySolutionMap initialBindings = createBindings( parameterMap);                 
        
        //execute query
        List<Map<String,String>> results = executeQuery( query, queryModel, initialBindings);
        
        //put results in page data, what key to use for results?
        Map<String, Object> rmap = new HashMap<String,Object>();
        rmap.put(this.saveToVar, results);       
        return rmap;        
    }
    
    private List<Map<String, String>> executeQuery(Query query, Model model,
            QuerySolutionMap initialBindings) {
        
        List<Map<String,String>> rows = new ArrayList<Map<String,String>>();
                
        model.enterCriticalSection(Lock.READ);        
        try{            
            QueryExecution qexec= QueryExecutionFactory.create(query, model,initialBindings );
            ResultSet results = qexec.execSelect();
            try{                
                while (results.hasNext()) {
                    QuerySolution soln = results.nextSolution();
                    rows.add( toRow( soln ) );
                }                   
            }finally{ qexec.close(); }
        }finally{ model.leaveCriticalSection(); }
        
        return rows;        
    }

    /**
     * Converts a row from a QuerySolution to a Map<String,String> 
     */
    private Map<String, String> toRow(QuerySolution soln) {
        HashMap<String,String> row = new HashMap<String,String>();        
        Iterator<String> varNames = soln.varNames();
        while( varNames.hasNext()){
            String varname = varNames.next();            
            row.put(varname, toCell( soln.get(varname)));            
        }
        return row;
    }
    
    private String toCell(RDFNode rdfNode) {
        if( rdfNode == null){
            return "";
        }else if( rdfNode.canAs( Literal.class )){
            return ((Literal)rdfNode.as(Literal.class)).getLexicalForm();
        }else if( rdfNode.isResource() ){
            Resource resource = (Resource)rdfNode;
            if( ! resource.isAnon() ){
                return resource.getURI();
            }else{
                return resource.getId().getLabelString();
            }                
        }else{
            return rdfNode.toString();
        }   
    }

    
    private Model getModelToRunQueryOn(ServletContext context, VitroRequest vreq ) {
        //just use JenaOntModel for now. in the future specify the
        //query model from the DataGetter's RDF configuration.
        return vreq.getJenaOntModel();        
    }

    private Model getDisplayModel(VitroRequest vreq, ServletContext context) {
        return vreq.getDisplayModel();
    }
    
    private QuerySolutionMap createBindings(Map<String, String[]>parameterMap) {
        
        QuerySolutionMap initBindings = new QuerySolutionMap();
        initBindings.add("pageUri", ResourceFactory.createResource(pageUri));

        //could have bindings from HTTP parameters
        for( String var : parameterMap.keySet() ) {
            
            String[] values =  parameterMap.get(var);
            if( values != null && values.length == 1 ){
                //what do do when we don't want a Resource?
                initBindings.add(var, ResourceFactory.createResource(values[0]) );
            }else if( values.length > 1){
                log.error("more than 1 http parameter for " + var);                
            }
        }
        
        return initBindings;
    }

    private static final String queryPropertyURI = "<" + DisplayVocabulary.QUERY + ">";
    private static final String saveToVarPropertyURI= "<" + DisplayVocabulary.SAVE_TO_VAR+ ">";
    private static final String queryModelPropertyURI= "<" + DisplayVocabulary.QUERY_MODEL+ ">";
    
    private static final String pageQuery =
        "PREFIX display: <" + DisplayVocabulary.DISPLAY_NS +"> \n" +
        "SELECT ?query ?saveToVar ?model WHERE { \n" +
        "  ?pageUri "+queryPropertyURI+" ?query . \n" +
        "  OPTIONAL{ ?pageUri "+saveToVarPropertyURI+" ?saveToVar } \n " +
        "  OPTIONAL{ ?pageUri "+queryModelPropertyURI+" ?queryModel } \n" +
        "}";      

   
}
