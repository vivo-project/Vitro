/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.utils.dataGetter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individuallist.ListedIndividualBuilder;
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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualListController.SearchException;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.individuallist.IndividualListResults;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.utils.searchengine.SearchQueryUtils;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individuallist.ListedIndividual;

public class SearchIndividualsDataGetter extends DataGetterBase implements DataGetter{    
    String dataGetterURI;
    List<String> vclassUris = null;
    String saveToVar;
    VitroRequest vreq;
    ServletContext context;
    
    
    final static Log log = LogFactory.getLog(SearchIndividualsDataGetter.class);
    //default template
    private final static String defaultTemplate = "menupage--defaultSearchIndividuals.ftl";
    
    /**
     * Constructor with display model and data getter URI that will be called by reflection.
     */
    public SearchIndividualsDataGetter(VitroRequest vreq, Model displayModel, String dataGetterURI){
        this.configure(vreq, displayModel,dataGetterURI);
    }        
    
    //For now, vClassURI should be passed in within model
    //We are also including ability to pass as parameter
    @Override
    public Map<String, Object> getData(Map<String, Object> pageData) { 
    	// Merge the pageData with the request parameters. PageData overrides
    	Map<String, String[]> merged = new HashMap<String, String[]>();
    	merged.putAll(vreq.getParameterMap());
    	for (String key: pageData.keySet()) {
    		merged.put(key, new String[] {String.valueOf(pageData.get(key))});
    	}
    	
        return doSearchQuery( merged);
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
        this.vclassUris = new ArrayList<String>();
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
                                
                    //saveToVar is OPTIONAL
                    Literal saveTo = soln.getLiteral("saveToVar");
                    if( saveTo != null && saveTo.isLiteral() ){
                        this.saveToVar = saveTo.asLiteral().getLexicalForm();                        
                    }else{
                        this.saveToVar = defaultVarNameForResults;
                    }
                    
                  //vclass uri is OPTIONAL
                    //Right now, only anticipating one but will need to change this if this is in fact a list
                    String vclassUriStr = null;
                    Resource vclassUri = soln.getResource("vclassUri");
                    if( vclassUri != null && vclassUri.isResource() ){
                        vclassUriStr = vclassUri.getURI();                        
                    }
                    if(vclassUriStr != null) {
                    	this.vclassUris.add(vclassUriStr);
                    }
                }
            }finally{ qexec.close(); }
        }finally{ displayModel.leaveCriticalSection(); }                
    }
    

    //Partially copied from IndividualListController
    private  Map<String, Object> doSearchQuery( Map<String, String[]> merged) {
    	if(vclassUris.size() == 0) {
    		if(merged.containsKey("vclassuri")) {
    			this.vclassUris = Arrays.asList(merged.get("vclassuri"));
    		} else {
    			log.error("No vclass uri found.  Search query will not work");
    		}
    	}
    	
    	Map<String, Object> body = new HashMap<String, Object>();

    	//Right now, just getting the first vclass uri, but will have to review how to get intersections or multiple classes
    	if(vclassUris.size() > 0) {
	    	String vClassURI = vclassUris.get(0);
	    	//First, get the vclass object
	    	 VClass vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(vClassURI);
	         if (vclass == null) {
	             log.error("Couldn't retrieve vclass " + vClassURI);   
	         }
	         
	        String vclassUri = vclass.getURI();
         	body.put("vclassId", vclassUri);
         	vreq.setAttribute("displayType", vclassUri); // used by the template model object
         	//Am not sure why an attribute is used here instead of just sending this in as data to the template?
	         
	      // Set title and subtitle. 
             VClassGroup classGroup = vclass.getGroup();  
             String title;
             if (classGroup == null) {
                 title = vclass.getName();
             } else {
                 title = classGroup.getPublicName();
                 body.put("subtitle", vclass.getName());
             }
             body.put("title", title);
             populateSearchQueryResults(vclass, body);
             body.put("bodyTemplate", this.defaultTemplate);
    	} else {
    		log.error("No VClass URIs found.  No query will be executed");
    	}
    	return body;
    }

    private void populateSearchQueryResults(VClass vclass, Map<String, Object> body) {
        try {
	    	String alpha = SearchQueryUtils.getAlphaParameter(vreq);
	        int page = SearchQueryUtils.getPageParameter(vreq);
	        IndividualListResults vcResults = IndividualListController.getResultsForVClass(
	                vclass.getURI(), 
	                page, 
	                alpha, 
	                vreq.getWebappDaoFactory().getIndividualDao());                                
	        body.putAll(vcResults.asFreemarkerMap());
	
	        List<Individual> inds = vcResults.getEntities();
	        List<ListedIndividual> indsTm = new ArrayList<ListedIndividual>();
	        if (inds != null) {
	            for ( Individual ind : inds ) {
	                indsTm.add(ListedIndividualBuilder.build(ind,vreq));
	            }
	        }
	        body.put("individuals", indsTm);    
	        body.put("rdfUrl", UrlBuilder.getUrl("/listrdf", "vclass", vclass.getURI())); 
        } catch (SearchException ex) {
            log.error("Error retrieving results for display.", ex);
        }
        catch(Exception ex) {
        	log.error("Error occurred in retrieving results ", ex);
        }
    }

    private static final String saveToVarPropertyURI= "<" + DisplayVocabulary.SAVE_TO_VAR+ ">";
    private static final String vclassIdPropertyURI= "<" + DisplayVocabulary.VCLASSID+ ">";


    public static final String defaultVarNameForResults = "results";
    
    /**
     * Query to get the definition of the search individuals data getter for a given URI.
     */
    private static final String dataGetterQuery =
        "PREFIX display: <" + DisplayVocabulary.DISPLAY_NS +"> \n" +
        "SELECT ?vclassUri ?saveToVar WHERE { \n" +
        "  OPTIONAL{ ?dataGetterURI "+saveToVarPropertyURI+" ?saveToVar } \n " +
        "  OPTIONAL{ ?dataGetterURI "+vclassIdPropertyURI+" ?vclassUri } \n " +
        "}";      

   
}
