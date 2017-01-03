/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.utils.dataGetter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

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
import org.apache.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupsForRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;
import edu.cornell.mannlib.vitro.webapp.utils.searchengine.SearchQueryUtils;


public class DataGetterUtils {
    
    final static Log log = LogFactory.getLog(DataGetterUtils.class);
    
    /**
     * Attribute name in request for DataGetters
     */
    public final static String DATA_GETTERS_FOR_PAGE = "data_getters_for_page";
    
    /**
     * Get a list of DataGetter objects that are associated with a page.
     * This should not return PageDataGetters and should not throw an 
     * exception if a page has PageDataGetters.  
     */
	public static List<DataGetter> getDataGettersForPage(VitroRequest vreq, Model displayModel, String pageURI) 
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, SecurityException, InvocationTargetException {

	    if( vreq.getAttribute(DATA_GETTERS_FOR_PAGE) != null){
	        return (List<DataGetter>) vreq.getAttribute(DATA_GETTERS_FOR_PAGE);
	    }else{
    		List<String> dgUris = getDataGetterURIsForAssociatedURI(displayModel, pageURI);
    		List<DataGetter> dgList = dataGettersForURIs(vreq, displayModel, dgUris);
    		log.debug("getDataGettersForPage: " + dgList);
    		vreq.setAttribute( DATA_GETTERS_FOR_PAGE , dgList );
    		return dgList;
	    }
	}
    
    /**
     * Get a list of DataGetter objects that are associated with a Vitro VClass.
     * This allows the individual profile for an individual of a specific class to be returned .  
     */
    public static List<DataGetter> getDataGettersForClass( VitroRequest vreq, Model displayModel, String classURI) 
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, SecurityException, InvocationTargetException {
        List<String> dgUris = getDataGetterURIsForAssociatedURI( displayModel, classURI);
        List<DataGetter> dgList = dataGettersForURIs(vreq, displayModel, dgUris);
        log.debug("getDataGettersForClass: " + dgList);
        return dgList;
    }

    /**
     * Get a list of DataGetter objects that are associated with a Freemarker template.
     * @param templateName a filename like "index.ftl", which will be used as a URI like "freemarker:index.ftl".
     */
    public static List<DataGetter> getDataGettersForTemplate( VitroRequest vreq, Model displayModel, String templateName) 
    		throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, SecurityException, InvocationTargetException {
    	String templateUri = "freemarker:" + templateName;
    	List<String> dgUris = getDataGetterURIsForAssociatedURI( displayModel, templateUri);
    	List<DataGetter> dgList = dataGettersForURIs(vreq, displayModel, dgUris);
    	log.debug("getDataGettersForTemplate '" + templateName + "': " + dgList);
    	return dgList;
    }
    
	/**
	 * Return a list of DataGetters from the list of URIs. Each DataGetter will be configured from information
	 * in the displayModel.
	 * 
	 * Problems instantiating and configuring a particular DataGetter may result in an exception,
	 * or may just mean that there will be no entry in the result for that URI.
	 * 
	 * May return an empty list, but will not return null.
	 */
	private static List<DataGetter> dataGettersForURIs(VitroRequest vreq, Model displayModel, List<String> dgUris)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, InvocationTargetException {
		List<DataGetter> dgList = new ArrayList<DataGetter>();
		for( String dgURI: dgUris){
		    DataGetter dg =dataGetterForURI(vreq, displayModel, dgURI) ;
		    if( dg != null )
		        dgList.add(dg); 
		}
		return dgList;
	}

    /**
     * Returns a DataGetter using information in the 
     * displayModel for the individual with the URI given by dataGetterURI
     * to configure it. 
     * 
     * May return null.
     * This should not throw an exception if the URI exists and has a type
     * that does not implement the DataGetter interface.
     */
    public static DataGetter dataGetterForURI(VitroRequest vreq, Model displayModel, String dataGetterURI) 
    throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException, SecurityException 
    {
        //get java class for dataGetterURI
        String dgClassName = getJClassForDataGetterURI(displayModel, dataGetterURI);
        
        //figure out if it implements interface DataGetter
        Class<?> clz = Class.forName(dgClassName);
        if( ! DataGetter.class.isAssignableFrom(clz) ){
    		log.debug("Class doesn't implement DataGetter: '" + dgClassName + "'");
            return null;
        }
        
        // we want a constructor that will work for one of these argument lists (in this order)
        Object[][] argLists = new Object[][] {
        		{ vreq, displayModel, dataGetterURI }, 
        		{ displayModel, dataGetterURI }, 
        		{ vreq }, 
        		{}
        	};
        
        // look through the available constructors for the best fit
        for (Object[] argList: argLists) {
        	for (Constructor<?> ct: clz.getConstructors()) {
        		if (isConstructorSuitableForArguments(ct, argList)) {
        			log.debug("Using this constructor: " + ct);
        			return (DataGetter) ct.newInstance(argList);
        		}
        	}
        }
        
		log.debug("Didn't find a suitable constructor for '" + dgClassName + "'");
        return null;
    }
    
    private static boolean isConstructorSuitableForArguments(Constructor<?> ct, Object[] args) {
		Class<?>[] parameterTypes = ct.getParameterTypes();
		if (args.length != parameterTypes.length) {
			return false;
		}
		for (int i = 0; i < args.length; i++) {
			Class<? extends Object> argClass = args[i].getClass();
			if (! parameterTypes[i].isAssignableFrom(argClass)) {
				return false;
			}
		}
		return true;
    }

    public static String getJClassForDataGetterURI(Model displayModel, String dataGetterURI) throws IllegalAccessException {
        String query = prefixes +
        "SELECT ?type WHERE { ?dgURI rdf:type ?type } ";
        Query dgTypeQuery = QueryFactory.create(query);
        
        QuerySolutionMap initialBindings = new QuerySolutionMap();
        initialBindings.add("dgURI", ResourceFactory.createResource( dataGetterURI ));
        
        List<String> types = new ArrayList<String>();         
        displayModel.enterCriticalSection(false);
        try{
            QueryExecution qexec = QueryExecutionFactory.create(dgTypeQuery,displayModel,initialBindings );
            try{                                                    
                ResultSet results = qexec.execSelect();                
                while (results.hasNext()) {
                    QuerySolution soln = results.nextSolution();
                    Resource type = soln.getResource("type");
                    if( type != null && type.getURI() != null){
                        types.add( DataGetterUtils.getClassNameFromUri( type.getURI() ));
                    }
                }
            }finally{ qexec.close(); }
        }finally{ displayModel.leaveCriticalSection(); }
        
        
        return chooseType( types, dataGetterURI);
    }
    
    
    private static List<String> getDataGetterURIsForAssociatedURI(Model displayModel, String associatedURI) {
        String query = prefixes + 
             "SELECT ?dataGetter WHERE { ?associatedURI display:hasDataGetter ?dataGetter }";
        Query dgForUriQuery = QueryFactory.create(query);
        
        QuerySolutionMap initialBindings = new QuerySolutionMap();
        initialBindings.add("associatedURI", ResourceFactory.createResource( associatedURI ));
        
        List<String> dgURIs = new ArrayList<String>();
        displayModel.enterCriticalSection(false);
        try{
            QueryExecution qexec = QueryExecutionFactory.create(dgForUriQuery,displayModel,initialBindings );
            try{                                                    
                ResultSet results = qexec.execSelect();                
                while (results.hasNext()) {
                    QuerySolution soln = results.nextSolution();
                    Resource dg = soln.getResource("dataGetter");
                    if( dg != null && dg.getURI() != null){
                        dgURIs.add( dg.getURI());
                    }
                }
            }finally{ qexec.close(); }
        }finally{ displayModel.leaveCriticalSection(); }
                
		log.debug("Found " + dgURIs.size() +" DataGetter URIs for '" + associatedURI + "': " + dgURIs);
        return dgURIs;
    }
    
    private static String chooseType(List<String> types, String dataGetterURI) throws IllegalAccessException {
        //currently just get the first one that is not owl:Thing
        for(String type : types){
            if( ! StringUtils.isEmpty( type ) && !type.equals( OWL.Thing.getURI() ))
                return type;
        }
        throw new IllegalAccessException("No useful type defined for <" + dataGetterURI + ">");        
    }
    //Copied from PageDaoJena
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
    
    static final String prefixes = 
        "PREFIX rdf:   <" + VitroVocabulary.RDF +"> \n" +
        "PREFIX rdfs:  <" + VitroVocabulary.RDFS +"> \n" + 
        "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#> \n" +
        "PREFIX display: <" + DisplayVocabulary.DISPLAY_NS +"> \n";

    //Return data getter type to be employed in display model
    public static String generateDataGetterTypeURI(String dataGetterClassName) {
    	return "java:" + dataGetterClassName;
    }
    
    public static final String getClassGroupForDataGetter(Model displayModel, String dataGetterURI) {
    	String classGroupUri = null; 
    	QuerySolutionMap initBindings = new QuerySolutionMap();
         initBindings.add("dataGetterURI", ResourceFactory.createResource(dataGetterURI));
         
         int count = 0;
         //Get the class group
         Query dataGetterConfigurationQuery = QueryFactory.create(classGroupForDataGetterQuery) ;               
         displayModel.enterCriticalSection(Lock.READ);
         try{
             QueryExecution qexec = QueryExecutionFactory.create(
                     dataGetterConfigurationQuery, displayModel, initBindings) ;        
             ResultSet res = qexec.execSelect();
             try{                
                 while( res.hasNext() ){
                     count++;
                     QuerySolution soln = res.next();
                     
                      
                     
                     //model is OPTIONAL
                     RDFNode node = soln.getResource("classGroupUri");
                     if( node != null && node.isURIResource() ){
                         classGroupUri = node.asResource().getURI();                        
                     }else{
                         classGroupUri = null;
                     }
                       
                 }
             }finally{ qexec.close(); }
         }finally{ displayModel.leaveCriticalSection(); }
         return classGroupUri;
    }
    
    private static final String forClassGroupURI = "<" + DisplayVocabulary.FOR_CLASSGROUP + ">";

    private static final String classGroupForDataGetterQuery =
        "PREFIX display: <" + DisplayVocabulary.DISPLAY_NS +"> \n" +
        "SELECT ?classGroupUri WHERE { \n" +
        "  ?dataGetterURI "+forClassGroupURI+" ?classGroupUri . \n" +
        "}";      
    
    
    /**
     * 
     * Convert data to JSON for page uri based on type and related datagetters
     * TODO: How to handle different data getters?  Will this replace json fields or add to them?
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public static JSONObject covertDataToJSONForPage(VitroRequest vreq, String pageUri, Model displayModel) throws InstantiationException, IllegalAccessException, ClassNotFoundException {       
        //Get PageDataGetter types associated with pageUri
        JSONObject rObj = null;   
        try{
	        List<DataGetter> dataGetters = getDataGettersForPage(vreq, displayModel, pageUri);
	        for(DataGetter getter: dataGetters) {
	        	 JSONObject typeObj = null;
	             try{
	            	 //Assumes the data getter itself will have a convert to json method
	            	 /*
	                 typeObj = getter.convertToJSON(data, vreq);
	                 if( typeObj != null) {
	                     //Copy over everything from this type Obj to 
	                     //TODO: Review how to handle duplicate keys, etc.
	                     if(rObj != null) {
	                         //For now, just nests as separate entry
	                         rObj.put(getter.getType(), typeObj);
	                     } else {
	                         rObj = typeObj;
	                     }
	                 } */     
	        	
	            } catch(Throwable th){
	                log.error(th,th);
	            }
	        }     
        } catch(Throwable th) {
        	log.error(th, th);
        }
        return rObj;
    }
    
    
    /***
     * For the page, get the actual Data Getters to be employed.
     */
    /*
    public static List<PageDataGetter> DataGetterObjects(VitroRequest vreq, String pageUri) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    	List<PageDataGetter> dataGetterObjects = new ArrayList<PageDataGetter>();
    	
    	List<String> dataGetterClassNames = vreq.getWebappDaoFactory().getPageDao().getDataGetterClass(pageUri);
    	if( dataGetterClassNames == null )
    	    return Collections.emptyList();
    	
    	for(String dgClassName: dataGetterClassNames) {
    		String className = getClassNameFromUri(dgClassName);
    		Class clz =  Class.forName(className);
    		
    		if( DataGetterUtils.isInstanceOfInterface(clz, PageDataGetter.class)){    		        		
    		    Object obj = clz.newInstance();
    		    if(obj != null && obj instanceof PageDataGetter) {
    		        PageDataGetter pg = (PageDataGetter) obj;
    		        dataGetterObjects.add(pg);
    		    }	    		
    		}// else skip if class does not implement PageDataGetter
    	} 
	        
    	return dataGetterObjects;
    }
    */
    
    //Class URIs returned include "java:" and to instantiate object need to remove java: portion
    public static String getClassNameFromUri(String dataGetterClassUri) {
    	if( !StringUtils.isEmpty(dataGetterClassUri) && dataGetterClassUri.contains("java:")) {
    		String[] splitArray = dataGetterClassUri.split("java:");
    		if(splitArray.length > 1) {
    			return splitArray[1];
    		}
    	}
    	return dataGetterClassUri;
    }
    

    
    /*
     * Copied from JSONServlet as expect this to be related to VitroClassGroup
     */
    public static JSONObject processVClassGroupJSON(VClassGroup vcg) {
        JSONObject map = new JSONObject();           
        try {
            ArrayList<JSONObject> classes = new ArrayList<JSONObject>(vcg.size());
            for( VClass vc : vcg){
                JSONObject vcObj = new JSONObject();
                vcObj.put("name", vc.getName());
                vcObj.put("URI", vc.getURI());
                vcObj.put("entityCount", vc.getEntityCount());
                classes.add(vcObj);
            }
            map.put("classes", classes);                
            map.put("classGroupName", vcg.getPublicName());
            map.put("classGroupUri", vcg.getURI());
        
        } catch(Exception ex) {
            log.error("Error occurred in processing VClass group ", ex);
        }
        return map;        
    }
    
	
    //Get All VClass Groups information
    //Used within menu management and processing
    //TODO: Check if more appropriate location possible
    public static List<HashMap<String, String>> getClassGroups(HttpServletRequest req) {
    	//Wanted this to be 
    	VClassGroupsForRequest vcgc = VClassGroupCache.getVClassGroups(req);
        List<VClassGroup> vcgList = vcgc.getGroups();
        //For now encoding as hashmap with label and URI as trying to retrieve class group
        //results in errors for some reason
        List<HashMap<String, String>> classGroups = new ArrayList<HashMap<String, String>>();
        for(VClassGroup vcg: vcgList) {
        	HashMap<String, String> hs = new HashMap<String, String>();
        	hs.put("publicName", vcg.getPublicName());
        	hs.put("URI", vcg.getURI());
        	classGroups.add(hs);
        }
        return classGroups;
    }
    
    
   //TODO: Check whether this needs to be put here or elsewhere, as this is data getter specific
    //with respect to class groups
  //Need to use VClassGroupCache to retrieve class group information - this is the information returned from "for class group"
	public static void getClassGroupForDataGetter(HttpServletRequest req, Map<String, Object> pageData, Map<String, Object> templateData) {
    	//Get the class group from VClassGroup, this is the same as the class group for the class group page data getter
		//and the associated class group (not custom) for individuals datagetter
		String classGroupUri = (String) pageData.get("classGroupUri");
		VClassGroupsForRequest vcgc = VClassGroupCache.getVClassGroups(req);
    	VClassGroup group = vcgc.getGroup(classGroupUri);

		templateData.put("classGroup", group);
		templateData.put("associatedPage", group.getPublicName());
		templateData.put("associatedPageURI", group.getURI());
    }
    
	
	
    
}
