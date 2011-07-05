/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.search.solr.SolrSetup;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.BaseListedIndividual;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.ListedIndividual;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModel;

/** 
 * Generates a list of individuals for display in a template 
 */
public class SolrIndividualListController extends FreemarkerHttpServlet {
  
    private static final long serialVersionUID = 1L;   
    private static final Log log = LogFactory.getLog(SolrIndividualListController.class.getName());

    private static final int INDIVIDUAL_LIST_CONTROLLER_MAX_RESULTS = 30000;
    private static final int INDIVIDUALS_PER_PAGE = 30;
    private static final int MAX_PAGES = 40;  // must be even
    
    private static final String TEMPLATE_DEFAULT = "individualList.ftl";

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
 
        String templateName = TEMPLATE_DEFAULT;
        Map<String, Object> body = new HashMap<String, Object>();
        String errorMessage = null;
        
        try {
            Object obj = vreq.getAttribute("vclass");
            VClass vclass = null;
            if ( obj == null ) { // look for vitroclass id parameter
                String vitroClassIdStr = vreq.getParameter("vclassId");
                if ( !StringUtils.isEmpty(vitroClassIdStr)) { 
                    try {
                        //TODO have to change this so vclass's group and entity count are populated
                        vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(vitroClassIdStr);
                        if (vclass == null) {
                            log.error("Couldn't retrieve vclass " + vitroClassIdStr);   
                            errorMessage = "Class " + vitroClassIdStr + " not found";
                        }
                    } catch (Exception ex) {
                        throw new HelpException("IndividualListController: url parameter 'vclassId' must be a URI string.");
                    }
                }
            } else if (obj instanceof VClass) {
                vclass = (VClass)obj;
            } else {
                throw new HelpException("IndividualListController: attribute 'vclass' must be of type "
                        + VClass.class.getName() + ".");
            }
            
            body.put("vclassId", vclass.getURI());
            
            if (vclass != null) {
                String alpha = getAlphaParameter(vreq);
                int page = getPageParameter(vreq);
                Map<String,Object> map = getResultsForVClass(
                        vclass.getURI(), 
                        page, 
                        alpha, 
                        vreq.getWebappDaoFactory().getIndividualDao(), 
                        getServletContext());                                
                body.putAll(map);

                @SuppressWarnings("unchecked")
                List<Individual> inds = (List<Individual>)map.get("entities");
                List<BaseListedIndividual> indsTm = new ArrayList<BaseListedIndividual>();
                for ( Individual ind : inds ) {
                    indsTm.add(new ListedIndividual(ind,vreq));
                }
                body.put("individuals", indsTm);
                
                List<TemplateModel> wpages = new ArrayList<TemplateModel>();
                @SuppressWarnings("unchecked")
                List<PageRecord> pages = (List<PageRecord>)body.get("pages");
                BeansWrapper wrapper = new BeansWrapper();
                for( PageRecord pr: pages ){
                    wpages.add( wrapper.wrap(pr) );
                }

                // Set title and subtitle. Title will be retrieved later in getTitle().   
                VClassGroup classGroup = vclass.getGroup();  
                String title;
                if (classGroup == null) {
                    title = vclass.getName();
                } else {
                    title = classGroup.getPublicName();
                    body.put("subtitle", vclass.getName());
                }
                body.put("title", title);  
                body.put("rdfUrl", UrlBuilder.getUrl("/listrdf", "vclass", vclass.getURI()));                         
            }   

        } catch (HelpException help){
            errorMessage = "Request attribute 'vclass' or request parameter 'vclassId' must be set before calling. Its value must be a class uri."; 
        } catch (Throwable e) {
            return new ExceptionResponseValues(e);
        }

        if (errorMessage != null) {
            templateName = Template.ERROR_MESSAGE.toString();
            body.put("errorMessage", errorMessage);
        }
        
        return new TemplateResponseValues(templateName, body);
    }
      
    private class HelpException extends Throwable {
        private static final long serialVersionUID = 1L;

        public HelpException(String string) {
            super(string);
        }
    }
    
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
    
    //Pulling out common code that is used for both single (regular) vclass query and multiple (intersection) query
    public static Map<String,Object> getResultsForVClasses(List<String> vclassURIs, int page, String alpha, IndividualDao indDao, ServletContext context) 
    throws IOException, ServletException{
   	 	Map<String,Object> rvMap = new HashMap<String,Object>();    
   	 	try{
	   		 SolrQuery query = getQuery(vclassURIs, alpha);        
	   		 rvMap = getResultsForVClassQuery(query, page, alpha, indDao, context);
		     List<Individual> individuals = (List<Individual>) rvMap.get("entities");
		     if (individuals == null) 
	             log.debug("entities list is null for vclasses " + vclassURIs.toString() );                        
	   	 } catch(Throwable th) {
	   		 log.error("An error occurred retrieving results for vclass query", th);
	   	 }
        return rvMap;
    }
    
    public static Map<String,Object> getResultsForVClass(String vclassURI, int page, String alpha, IndividualDao indDao, ServletContext context) 
    throws IOException, ServletException{
   	 	Map<String,Object> rvMap = new HashMap<String,Object>();    
   	 	try{
		     //make query for this rdf:type
	   		 List<String> classUris = new ArrayList<String>();
	   		 classUris.add(vclassURI);
	   		 SolrQuery query = getQuery(classUris, alpha);        
	   		 rvMap = getResultsForVClassQuery(query, page, alpha, indDao, context);
		     List<Individual> individuals = (List<Individual>) rvMap.get("entities");
		     if (individuals == null) 
	             log.debug("entities list is null for vclass " + vclassURI );                        
	   	 } catch(Throwable th) {
	   		 log.error("An error occurred retrieving results for vclass query", th);
	   	 }
        return rvMap;
    }
    
    public static Map<String,Object> getResultsForVClassIntersections(List<String> vclassURIs, int page, String alpha, IndividualDao indDao, ServletContext context) 
    throws IOException, ServletException{
        Map<String,Object> rvMap = new HashMap<String,Object>();  
        try{
             // make query for multiple rdf types 
	         SolrQuery query = getQuery(vclassURIs, alpha);     
	         rvMap = getResultsForVClassQuery(query, page, alpha, indDao, context);
	         List<Individual> individuals = (List<Individual>) rvMap.get("entities");
		     if (individuals == null) 
		       log.debug("entities list is null for vclass " + vclassURIs.toString() );     
        } catch(Throwable th) {
       	    log.error("Error retrieving individuals corresponding to intersection multiple classes." + vclassURIs.toString(), th);
        }
        return rvMap;
    }
    
    /**
     * This method is now called in a couple of places.  It should be refactored
     * into a DAO or similar object.
     */
    public static Map<String,Object> getResultsForVClassQuery(SolrQuery query, int page, String alpha, IndividualDao indDao, ServletContext context) 
    throws IOException, ServletException {
        Map<String,Object> rvMap = new HashMap<String,Object>();           
        SolrServer solr = SolrSetup.getSolrServer(context);
        QueryResponse response = null;
        
        // Execute query for individuals of the specified type
        try {
            response = solr.query(query);            
        } catch (Throwable t) {
            log.error(t, t);            
        }

        if ( response == null ) {         
            throw new ServletException("Could not run search in SolrIndividualListController");        
        }

        SolrDocumentList docs = response.getResults();
        
        if (docs == null) {
            throw new ServletException("Could not run search in SorlIndividualListController");    
        }

        // get list of individuals for the search results
        long hitCount = docs.getNumFound();
        log.debug("Number of search results: " + hitCount);

        List<Individual> individuals = new ArrayList<Individual>(INDIVIDUALS_PER_PAGE); 
        int individualsAdded = 0;
        int index = (page-1) * INDIVIDUALS_PER_PAGE;
        while (individualsAdded < INDIVIDUALS_PER_PAGE && index < hitCount) {
            SolrDocument doc = docs.get(index);
            if (doc != null) {
                String uri = doc.get(VitroSearchTermNames.URI).toString();
                Individual individual = indDao.getIndividualByURI( uri ); 
                if (individual != null) {
                    individualsAdded++;                    
                    individuals.add(individual);
                    log.debug("Adding individual " + uri + " to individual list display");
                } else {
                    log.debug("No existing individual for search document with uri = " + uri);
                }
            } 
            index++;
        }
         
        // Test index < hitCount ensures that there are still some docs left
        if ( hitCount > INDIVIDUALS_PER_PAGE && index < hitCount ){
            rvMap.put("showPages", Boolean.TRUE);
            List<PageRecord> pageRecords = makePagesList(hitCount, INDIVIDUALS_PER_PAGE, page);
            rvMap.put("pages", pageRecords);                    
        }else{
            rvMap.put("showPages", Boolean.FALSE);
            rvMap.put("pages", Collections.emptyList());
        }
                         
        rvMap.put("alpha",alpha);

        rvMap.put("totalCount", hitCount);
        rvMap.put("entities",individuals);                      
         
        return rvMap;
    }
     
    private static SolrQuery getQuery(List<String> vclassUris, String alpha){

        String queryText = "";
 
        List<String> queryTypes = new ArrayList<String>();
        try {
            
            // query term for rdf:type - multiple types possible
        	for(String vclassUri: vclassUris) {
        		queryTypes.add(VitroSearchTermNames.RDFTYPE + ":\"" + vclassUri + "\" ");
        	} 
        	
        	if (queryTypes.size() > 0) {
        		queryText = StringUtils.join(queryTypes, " AND ");
        	} 
        	
        	 // Add alpha filter if applicable
            if ( alpha != null && !"".equals(alpha) && alpha.length() == 1) {      
                queryText += VitroSearchTermNames.NAME_LOWERCASE + ":" + alpha.toLowerCase() + "*";
            }     
            
            SolrQuery query = new SolrQuery(queryText);
            log.debug("Query text is " + queryText);
           
            // Get all available results from index rather than just those for the current page.
            // Otherwise, if there are a large number of non-existent individuals in the search
            // index, the current page of results might not retrieve any existing individuals,
            // and nothing gets returned.
            query.setStart(0)
                 .setRows(INDIVIDUAL_LIST_CONTROLLER_MAX_RESULTS)
                 // Need a single-valued field for sorting
                 .setSortField(VitroSearchTermNames.NAME_LOWERCASE_SINGLE_VALUED, SolrQuery.ORDER.asc);
            
            return query;
            
        } catch (Exception ex){
            log.error(ex,ex);
            return new SolrQuery();        
        }      
    }    

    public static List<PageRecord> makePagesList( long size, int pageSize,  int selectedPage ) {        

        List<PageRecord> records = new ArrayList<PageRecord>( MAX_PAGES + 1 );
        int requiredPages = (int) (size/pageSize) ;
        int remainder = (int) (size % pageSize) ; 
        if( remainder > 0 )
            requiredPages++;
         
        if( selectedPage < MAX_PAGES && requiredPages > MAX_PAGES ){
            //the selected pages is within the first maxPages, just show the normal pages up to maxPages.
            for(int page = 1; page < requiredPages && page <= MAX_PAGES ; page++ ){
                records.add( new PageRecord( "page=" + page, Integer.toString(page), Integer.toString(page), selectedPage == page ) );            
            }
            records.add( new PageRecord( "page="+ (MAX_PAGES+1), Integer.toString(MAX_PAGES+1), "more...", false));
        }else if( requiredPages > MAX_PAGES && selectedPage+1 > MAX_PAGES && selectedPage < requiredPages - MAX_PAGES){
            //the selected pages is in the middle of the list of page
            int startPage = selectedPage - MAX_PAGES / 2;
            int endPage = selectedPage + MAX_PAGES / 2;            
            for(int page = startPage; page <= endPage ; page++ ){
                records.add( new PageRecord( "page=" + page, Integer.toString(page), Integer.toString(page), selectedPage == page ) );            
            }
            records.add( new PageRecord( "page="+ endPage+1, Integer.toString(endPage+1), "more...", false));
        }else if ( requiredPages > MAX_PAGES && selectedPage > requiredPages - MAX_PAGES ){
            //the selected page is in the end of the list 
            int startPage = requiredPages - MAX_PAGES;      
            double max = Math.ceil(size/pageSize);
            for(int page = startPage; page <= max; page++ ){
                records.add( new PageRecord( "page=" + page, Integer.toString(page), Integer.toString(page), selectedPage == page ) );            
            }          
        }else{
            //there are fewer than maxPages pages.            
            for(int i = 1; i <= requiredPages; i++ ){
                records.add( new PageRecord( "page=" + i, Integer.toString(i), Integer.toString(i), selectedPage == i ) );            
             }    
        }                
        return records;
    }

    public static class PageRecord  {
        public PageRecord(String param, String index, String text, boolean selected) {            
            this.param = param;
            this.index = index;
            this.text = text;
            this.selected = selected;
        }
        public String param;
        public String index;
        public String text;        
        public boolean selected=false;

        public String getParam() {
            return param;
        }
        public String getIndex() {
            return index;
        }
        public String getText() {
            return text;
        }
        public boolean getSelected(){
            return selected;
        }
    }
}
