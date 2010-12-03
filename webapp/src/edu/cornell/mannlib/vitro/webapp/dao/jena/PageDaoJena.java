/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

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

import edu.cornell.mannlib.vitro.webapp.dao.PageDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class PageDaoJena extends JenaBaseDao implements PageDao {

    static protected Query pageQuery;
    static protected Query pageMappingsQuery;
    static protected Query homePageUriQuery;
    
    static final String prefixes = 
        "PREFIX rdf:   <" + VitroVocabulary.RDF +"> \n" +
        "PREFIX rdfs:  <" + VitroVocabulary.RDFS +"> \n" + 
        "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#> \n" +
        "PREFIX display: <" + VitroVocabulary.DISPLAY +"> \n";
    
    
    static final protected String pageQueryString = 
        prefixes + "\n" +
        "SELECT ?pageUri ?bodyTemplate ?urlMapping ?title WHERE {\n" +
//        "  GRAPH ?g{\n"+
        "    ?pageUri rdf:type display:Page .\n"+               
        "    OPTIONAL { ?pageUri display:requiresBodyTemplate ?bodyTemplate }.\n"+
        "    OPTIONAL { ?pageUri display:title ?title }.\n"+
        "    OPTIONAL { ?pageUri display:urlMapping ?urlMapping . }\n"+                
//        "  }\n"+
        "} \n" ;
    
    static final protected String pageMappingsQueryString = 
        prefixes + "\n" +
        "SELECT ?pageUri ?urlMapping WHERE {\n" +
//        "  GRAPH ?g{\n"+
        "    ?pageUri rdf:type display:Page .\n"+                       
        "    ?pageUri display:urlMapping ?urlMapping . \n"+                
//        "  }\n"+
        "} \n" ;
                          
    static final protected String homePageUriQueryString = 
        prefixes + "\n" +
        "SELECT ?pageUri WHERE {\n" +
//        "  GRAPH ?g{\n"+
        "    ?pageUri rdf:type display:HomePage .\n"+                
//        "  }\n"+
        "} \n" ;

    
    static{
        try{    
            pageQuery=QueryFactory.create(pageQueryString);
        }catch(Throwable th){
            log.error("could not create SPARQL query for pageQuery " + th.getMessage());
            log.error(pageQueryString);
        }
        try{    
            pageMappingsQuery=QueryFactory.create(pageMappingsQueryString);
        }catch(Throwable th){
            log.error("could not create SPARQL query for pageMappingsQuery " + th.getMessage());
            log.error(pageMappingsQueryString);
        }   
        try{    
            homePageUriQuery=QueryFactory.create(homePageUriQueryString);
        }catch(Throwable th){
            log.error("could not create SPARQL query for homePageUriQuery " + th.getMessage());
            log.error(homePageUriQueryString);
        }  
    }        
    
    public PageDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }
   
    @Override
    public Map<String, String> getPageMappings() {
        Model displayModel = getOntModelSelector().getDisplayModel();
        QueryExecution qexec = QueryExecutionFactory.create( pageQuery, displayModel );
        
        Map<String,String> rv = new HashMap<String,String>();
        ResultSet resultSet = qexec.execSelect();
        while(resultSet.hasNext()){
            QuerySolution soln = resultSet.next();
            rv.put(nodeToString(soln.get("urlMapping")) , nodeToString( soln.get("pageUri") ));
        }
        return rv; 
    }
    
    /**
     * Gets information about a page identified by a URI.
     */
    @Override
    public Map<String, Object> getPage(String pageUri) {     
      //setup query parameters
      QuerySolutionMap initialBindings = new QuerySolutionMap();
      initialBindings.add("pageUri", ResourceFactory.createResource(pageUri));
        
      Model displayModel = getOntModelSelector().getDisplayModel();
      QueryExecution qexec = QueryExecutionFactory.create(pageQuery,displayModel,initialBindings );
      List<Map<String, Object>> list = executeQueryToCollection( qexec );
      if( list == null ){
          log.error("executeQueryToCollection returned null.");
          return Collections.emptyMap();
      }
      if( list.size() == 0 ){
          log.debug("no page found for " + pageUri);
          return Collections.emptyMap();          
      }
      if( list.size() > 1 ){
          log.debug("multiple results found for " + pageUri + " using only the first.");
          return list.get(0);
      }else{
          return list.get(0);
      }      
    }
    
    @Override
    public String getHomePageUri(){
        Model displayModel = getOntModelSelector().getDisplayModel();
        QueryExecution qexec = QueryExecutionFactory.create( homePageUriQuery, displayModel );
        
        List<String> rv = new ArrayList<String>();
        ResultSet resultSet = qexec.execSelect();        
        while(resultSet.hasNext()){
            QuerySolution soln = resultSet.next();
            rv.add( nodeToString(soln.get("pageUri")) );        
        }
        if( rv.size() == 0 ){
            log.error("No display:HomePage defined in display model.");
            return null;
        }
        if( rv.size() > 1 ){
            log.error("More than one display:HomePage defined in display model.");
            for( String hp : rv ){
                log.error("home page: " + hp);
            }
        }
        return rv.get(0);
    }
    
    
    /* ****************************************************************************** */
    
    /**
     * Converts a sparql query that returns a multiple rows to a list of maps.
     * The maps will have column names as keys to the values.
     */
    protected List<Map<String, Object>> executeQueryToCollection(
            QueryExecution qexec) {
        List<Map<String, Object>> rv = new ArrayList<Map<String, Object>>();
        ResultSet results = qexec.execSelect();
        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            rv.add(querySolutionToMap(soln));
        }
        return rv;
    }
    
    protected Map<String,Object> querySolutionToMap( QuerySolution soln ){
        Map<String,Object> map = new HashMap<String,Object>();
        Iterator<String> varNames = soln.varNames();
        while(varNames.hasNext()){
            String varName = varNames.next();
            map.put(varName, nodeToObject( soln.get(varName)));
        }
        return map;
    }
    
    static protected Object nodeToObject( RDFNode node ){
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

    static protected String nodeToString( RDFNode node ){
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
    protected Map<String,Object> resultsToMap(){
        return null;
    }

}
