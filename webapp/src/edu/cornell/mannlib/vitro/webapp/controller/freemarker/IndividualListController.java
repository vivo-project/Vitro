/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

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
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individuallist.ListedIndividual;

/** 
 * Generates a list of individuals for display in a template 
 */
public class IndividualListController extends FreemarkerHttpServlet {
  
    private static final long serialVersionUID = 1L;   
    private static final Log log = LogFactory.getLog(IndividualListController.class.getName());
    
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
            
            if (vclass != null) {
                
            	String vclassUri = vclass.getURI();
            	body.put("vclassId", vclassUri);
            	vreq.setAttribute("displayType", vclassUri); // used by the template model object
            	
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
                List<ListedIndividual> indsTm = new ArrayList<ListedIndividual>();
                if (inds != null) {
                    for ( Individual ind : inds ) {
                        indsTm.add(new ListedIndividual(ind,vreq));
                    }
                }
                body.put("individuals", indsTm);    
                body.put("rdfUrl", UrlBuilder.getUrl("/listrdf", "vclass", vclass.getURI()));    
            }   
        } catch (SearchException e) {
            errorMessage = "Error retrieving results for display.";
        } catch (HelpException help){
            errorMessage = "Request attribute 'vclass' or request parameter 'vclassId' must be set before calling. Its value must be a class uri."; 
        } catch (Throwable e) {
            return new ExceptionResponseValues(e);
        }
        
        if (errorMessage != null) {
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
    
    public static class SearchException extends Throwable {
        private static final long serialVersionUID = 1L;
        
        public SearchException(String string) {
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
    
    public static Map<String,Object> getResultsForVClass(String vclassURI, int page, String alpha, IndividualDao indDao, ServletContext context) 
    throws SearchException{
   	 	Map<String,Object> rvMap = new HashMap<String,Object>();    
   	 	try{
            List<String> classUris = Collections.singletonList(vclassURI);
			IndividualListQueryResults results = buildAndExecuteVClassQuery(classUris, alpha, page, INDIVIDUALS_PER_PAGE, context, indDao);
	        rvMap = getResultsForVClassQuery(results, page, INDIVIDUALS_PER_PAGE, alpha);
   	 	} catch (SolrServerException e) {
   	 	    String msg = "An error occurred retrieving results for vclass query";
   	 	    log.error(msg, e);
   	 	    // Throw this up to processRequest, so the template gets the error message.
   	 	    throw new SearchException(msg);
	   	} catch(Throwable th) {
	   		log.error("An error occurred retrieving results for vclass query", th);
	    }
        return rvMap;
    }
    
    public static Map<String,Object> getResultsForVClassIntersections(List<String> vclassURIs, int page, int pageSize, String alpha, IndividualDao indDao, ServletContext context) {
        Map<String,Object> rvMap = new HashMap<String,Object>();  
        try{
            IndividualListQueryResults results = buildAndExecuteVClassQuery(vclassURIs, alpha, page, pageSize, context, indDao);
	        rvMap = getResultsForVClassQuery(results, page, pageSize, alpha);
        } catch(Throwable th) {
       	    log.error("Error retrieving individuals corresponding to intersection multiple classes." + vclassURIs.toString(), th);
        }
        return rvMap;
    }

	private static IndividualListQueryResults buildAndExecuteVClassQuery(
			List<String> vclassURIs, String alpha, int page, int pageSize,
			ServletContext context, IndividualDao indDao)
			throws SolrServerException {
		 SolrQuery query = getQuery(vclassURIs, alpha, page, pageSize);
		 IndividualListQueryResults results = IndividualListQueryResults.runQuery(query, indDao, context);
		 log.debug("Executed solr query for " + vclassURIs);
		 if (results.getIndividuals().isEmpty()) { 
			 log.debug("entities list is null for vclass " + vclassURIs);
		 }
		return results;
	}
    
    private static Map<String,Object> getResultsForVClassQuery(IndividualListQueryResults results, int page, int pageSize, String alpha) {
        Map<String,Object> rvMap = new HashMap<String,Object>();
        
        long hitCount = results.getHitCount();
        if ( hitCount > pageSize ){
            rvMap.put("showPages", Boolean.TRUE);
            List<PageRecord> pageRecords = makePagesList(hitCount, pageSize, page);
            rvMap.put("pages", pageRecords);                    
        }else{
            rvMap.put("showPages", Boolean.FALSE);
            rvMap.put("pages",  Collections.emptyList());
        }
                         
        rvMap.put("alpha",alpha);
        rvMap.put("totalCount", hitCount);
        rvMap.put("entities", results.getIndividuals());                      
         
        return rvMap;
    }
     
    //Get count of individuals without actually getting the results
    public static long getIndividualCount(List<String> vclassUris, IndividualDao indDao, ServletContext context) {    	    	       
       SolrQuery query = new SolrQuery(makeMultiClassQuery(vclassUris));
       query.setRows(0);
    	try {    	              
            SolrServer solr = SolrSetup.getSolrServer(context);
            QueryResponse response = null;                      
            response = solr.query(query);            
            return response.getResults().getNumFound();                        
    	} catch(Exception ex) {
    		log.error("An error occured in retrieving individual count", ex);
    	}
    	return 0;
    }
    
    /**
     * builds a query with a type clause for each type in vclassUris, NAME_LOWERCASE filetred by
     * alpha, and just the hits for the page for pageSize.
     */
    private static SolrQuery getQuery(List<String> vclassUris, String alpha, int page, int pageSize){
        String queryText = "";
        
        try {            
            queryText = makeMultiClassQuery(vclassUris);
            
        	 // Add alpha filter if applicable
            if ( alpha != null && !"".equals(alpha) && alpha.length() == 1) {      
                queryText += VitroSearchTermNames.NAME_LOWERCASE + ":" + alpha.toLowerCase() + "*";
            }     
            
            SolrQuery query = new SolrQuery(queryText);

            //page count starts at 1, row count starts at 0
            int startRow = (page-1) * pageSize ;            
            query.setStart( startRow ).setRows( pageSize );
            
            // Need a single-valued field for sorting
            query.setSortField(VitroSearchTermNames.NAME_LOWERCASE_SINGLE_VALUED, SolrQuery.ORDER.asc);

            log.debug("Query is " + query.toString());
            return query;
            
        } catch (Exception ex){
            log.error("Could not make Solr query",ex);
            return new SolrQuery();        
        }      
    }    

    private static String makeMultiClassQuery( List<String> vclassUris){
        List<String> queryTypes = new ArrayList<String>();  
        try {            
            // query term for rdf:type - multiple types possible
            for(String vclassUri: vclassUris) {
                queryTypes.add(VitroSearchTermNames.RDFTYPE + ":\"" + vclassUri + "\" ");
            }           
			return StringUtils.join(queryTypes, " AND ");
        } catch (Exception ex){
            log.error("Could not make Solr query",ex);
            return "";
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
            records.add( new PageRecord( "page="+ (endPage+1), Integer.toString(endPage+1), "more...", false));
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
