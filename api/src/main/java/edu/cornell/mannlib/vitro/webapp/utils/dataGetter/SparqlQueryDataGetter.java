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

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.QueryUtils;

public class SparqlQueryDataGetter extends DataGetterBase implements DataGetter{
	private final static Log log = LogFactory.getLog(SparqlQueryDataGetter.class);
	
    private static final String queryPropertyURI = "<" + DisplayVocabulary.QUERY + ">";
    private static final String saveToVarPropertyURI= "<" + DisplayVocabulary.SAVE_TO_VAR+ ">";
    private static final String queryModelPropertyURI= "<" + DisplayVocabulary.QUERY_MODEL+ ">";

    public static final String defaultVarNameForResults = "results";
    private static final String defaultTemplate = "menupage--defaultSparql.ftl";

    String dataGetterURI;
    String queryText;
    String saveToVar;
    String modelURI;
    VitroRequest vreq;
    ServletContext context;
    
    /**
     * Constructor with display model and data getter URI that will be called by reflection.
     */
    public SparqlQueryDataGetter(VitroRequest vreq, Model displayModel, String dataGetterURI){
        this.configure(vreq, displayModel,dataGetterURI);
    }        
    
	/**
     * Configure this instance based on the URI and display model.
     */
    @SuppressWarnings("hiding")
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
        
        Query dataGetterConfigurationQuery = QueryFactory.create(dataGetterQuery) ;               
        displayModel.enterCriticalSection(Lock.READ);
        try{
            QueryExecution qexec = QueryExecutionFactory.create(
                    dataGetterConfigurationQuery, displayModel, initBindings) ;        
            ResultSet res = qexec.execSelect();
            try{                
                while( res.hasNext() ){
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
     * Query to get the definition of the SparqlDataGetter for a given URI.
     */
    private static final String dataGetterQuery =
        "PREFIX display: <" + DisplayVocabulary.DISPLAY_NS +"> \n" +
        "SELECT ?query ?saveToVar ?queryModel WHERE { \n" +
        "  ?dataGetterURI "+queryPropertyURI+" ?query . \n" +
        "  OPTIONAL{ ?dataGetterURI "+saveToVarPropertyURI+" ?saveToVar } \n " +
        "  OPTIONAL{ ?dataGetterURI "+queryModelPropertyURI+" ?queryModel } \n" +
        "}";      

   
    @Override
    public Map<String, Object> getData(Map<String, Object> pageData) { 
    	Map<String, String> merged = mergeParameters(vreq.getParameterMap(), pageData);
    	
    	String boundQueryText = bindParameters(queryText, merged);

    	if (modelURI != null) {
    		return doQueryOnModel(boundQueryText, getModel(context, vreq, modelURI));
    	} else {
    		return doQueryOnRDFService(boundQueryText);
    	}
    }

    /** Merge the pageData with the request parameters. PageData overrides. */
	private Map<String, String> mergeParameters(
			Map<String, String[]> parameterMap, Map<String, Object> pageData) {
		Map<String, String> merged = new HashMap<>();
		for (String key: parameterMap.keySet()) {
			merged.put(key, parameterMap.get(key)[0]);
		}
		for (String key: pageData.keySet()) {
			merged.put(key, String.valueOf(pageData.get(key)));
		}
		if (log.isDebugEnabled()) {
			log.debug("Merging request parameters " + parameterMap
					+ " with page data " + pageData + " results in " + merged);
		}
		return merged;
	}

	/**
	 * InitialBindings don't always work, and besides, RDFService doesn't accept
	 * them. So do a text-based substitution.
	 * 
	 * This assumes that every parameter is a URI. What if we want to substitute
	 * a string value?
	 */
	private String bindParameters(String text, Map<String, String> merged) {
		String bound = text;
		for (String key : merged.keySet()) {
            bound = bound.replaceAll("([?$]" + key + ")([^a-zA-Z0-9_\\-])", "<" + merged.get(key) + ">$2");
		}
		if (log.isDebugEnabled()) {
			log.debug("parameters: " + merged);
			log.debug("query before binding parameters:" + text);
			log.debug("query after binding parameters: " + bound);
		}
		return bound;
	}

	/**
	 * Do the query and return a result. This is in its own method, with
	 * protected access, to make testing easy.
	 */
	protected Map<String, Object> doQueryOnRDFService(String  q) {
		log.debug("Going to RDFService with " + q);
        ResultSet results = QueryUtils.getQueryResults(q, vreq);
        return assembleMap(parseResults(results));
	}

    /**
	 * Do the query and return a result. This is in its own method, with
	 * protected access, to make testing easy.
	 */
    protected Map<String, Object> doQueryOnModel(String q, Model queryModel){
		log.debug("Going to model " + modelURI + " with " + q);
    	if (q == null) {
            return Collections.emptyMap();
        }

		Query query = makeQuery(q);
    	if (query == null) {
            return Collections.emptyMap();
        }
        
        return assembleMap(executeQuery( query, queryModel));
    }
    
	private Query makeQuery(String q) {
		try {
			return QueryFactory.create(q);
		} catch (Exception e) {
			log.error("Failed to build a query from ''", e);
			return null;
		}
	}

	private List<Map<String, String>> executeQuery(Query query, Model model) {
        model.enterCriticalSection(Lock.READ);        
        try{            
            QueryExecution qexec= QueryExecutionFactory.create(query, model );
            ResultSet results = qexec.execSelect();
            try{                
            	return parseResults(results);
            }finally{ qexec.close(); }
        }finally{ model.leaveCriticalSection(); }
    }

    /**
     * Converts a ResultSet into a List of Maps.
	 */
	private List<Map<String, String>> parseResults(ResultSet results) {
        List<Map<String,String>> rows = new ArrayList<Map<String,String>>();
        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            rows.add( toRow( soln ) );
        }                   
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
        }else if( rdfNode.isLiteral() ){
            return rdfNode.asLiteral().getLexicalForm();
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

	private Map<String, Object> assembleMap(List<Map<String, String>> results) {
		Map<String, Object> rmap = new HashMap<String,Object>();
        
        //put results in page data
        rmap.put(this.saveToVar, results);  
        //also store the variable name within which results will be returned
        rmap.put("variableName", this.saveToVar);
        //This will be overridden at page level in display model if template specified there
        rmap.put("bodyTemplate", defaultTemplate);
        
        return rmap;        
	}
	
}
