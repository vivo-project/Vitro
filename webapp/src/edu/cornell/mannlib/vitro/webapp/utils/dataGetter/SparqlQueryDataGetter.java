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

public class SparqlQueryDataGetter extends DataGetterBase implements DataGetter{    
    String dataGetterURI;
    String queryText;
    String saveToVar;
    String modelURI;
    VitroRequest vreq;
    ServletContext context;
    
    
    final static Log log = LogFactory.getLog(SparqlQueryDataGetter.class);
    //default template
    private final static String defaultTemplate = "menupage--defaultSparql.ftl";
    
    /**
     * Constructor with display model and data getter URI that will be called by reflection.
     */
    public SparqlQueryDataGetter(VitroRequest vreq, Model displayModel, String dataGetterURI){
        this.configure(vreq, displayModel,dataGetterURI);
    }        
    
    @Override
    public Map<String, Object> getData(Map<String, Object> pageData) {                        
        return doQuery( vreq.getParameterMap(), getModel(context, vreq, modelURI));
    }

    /**
     * Configure this instance based on the URI and display model.
     */
    protected void configure(VitroRequest vreq, Model displayModel, String dataGetterURI) {
    	if( vreq == null ) 
    		throw new IllegalArgumentException("VitroRequest  may not be null.");
        if( displayModel == null ) 
            throw new IllegalArgumentException("Display Model may not be null.");
        if( dataGetterURI == null )
            throw new IllegalArgumentException("PageUri may not be null.");
                
        this.vreq = vreq;
        this.context = vreq.getSession().getServletContext();
        this.dataGetterURI = dataGetterURI;        
        
        QuerySolutionMap initBindings = new QuerySolutionMap();
        initBindings.add("dataGetterURI", ResourceFactory.createResource(this.dataGetterURI));
        
        int count = 0;
        Query dataGetterConfigurationQuery = QueryFactory.create(dataGetterQuery) ;               
        displayModel.enterCriticalSection(Lock.READ);
        try{
            QueryExecution qexec = QueryExecutionFactory.create(
                    dataGetterConfigurationQuery, displayModel, initBindings) ;        
            ResultSet res = qexec.execSelect();
            try{                
                while( res.hasNext() ){
                    count++;
                    QuerySolution soln = res.next();
                    
                    //query is NOT OPTIONAL
                    Literal value = soln.getLiteral("query");
                    if( dataGetterConfigurationQuery == null )
                        log.error("no query defined for page " + this.dataGetterURI);
                    else
                        this.queryText = value.getLexicalForm();                    
                    
                    //model is OPTIONAL                    
                    RDFNode node = soln.get("queryModel");
                    if( node != null && node.isURIResource() ){
                        this.modelURI = node.asResource().getURI();                        
                    }else if( node != null && node.isLiteral() ){
                        this.modelURI = node.asLiteral().getLexicalForm();                        
                    }else{
                        this.modelURI = null;
                    }
                        
                    //saveToVar is OPTIONAL
                    Literal saveTo = soln.getLiteral("saveToVar");
                    if( saveTo != null && saveTo.isLiteral() ){
                        this.saveToVar = saveTo.asLiteral().getLexicalForm();                        
                    }else{
                        this.saveToVar = defaultVarNameForResults;
                    }
                }
            }finally{ qexec.close(); }
        }finally{ displayModel.leaveCriticalSection(); }                
    }
    
    /**
     * Do the query and return a result. This is in its own method 
     * to make testing easy.
     */
    protected Map<String, Object> doQuery(Map<String, String[]>parameterMap, Model queryModel){

        if( this.queryText == null ){            
            log.error("no SPARQL query defined for page " + this.dataGetterURI);
            return Collections.emptyMap();
        }
        
        //this may throw a SPARQL syntax error 
        Query query = QueryFactory.create( this.queryText );                

        //build query bindings
        QuerySolutionMap initialBindings = createBindings( parameterMap);                 
        
        //execute query
        List<Map<String,String>> results = executeQuery( query, queryModel, initialBindings);
        
        //put results in page data, what key to use for results?
        Map<String, Object> rmap = new HashMap<String,Object>();
        rmap.put(this.saveToVar, results);  
        //This will be overridden at page level in display model if template specified there
        rmap.put("bodyTemplate", defaultTemplate);
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



    private QuerySolutionMap createBindings(Map<String, String[]>parameterMap) {        
        QuerySolutionMap initBindings = new QuerySolutionMap();

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

    public static final String defaultVarNameForResults = "results";
    
    /**
     * Query to get the definition of the SparqlDataGetter for a given URI.
     */
    private static final String dataGetterQuery =
        "PREFIX display: <" + DisplayVocabulary.DISPLAY_NS +"> \n" +
        "SELECT ?query ?saveToVar ?queryModel WHERE { \n" +
        "  ?dataGetterURI "+queryPropertyURI+" ?query . \n" +
        "  OPTIONAL{ ?dataGetterURI "+saveToVarPropertyURI+" ?saveToVar } \n " +
        "  OPTIONAL{ ?dataGetterURI "+queryModelPropertyURI+" ?queryModel } \n" +
        "}";      

   
}
