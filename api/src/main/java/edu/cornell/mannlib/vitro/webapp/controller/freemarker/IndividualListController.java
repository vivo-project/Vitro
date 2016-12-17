/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individuallist.ListedIndividualBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.individuallist.IndividualListResults;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.utils.searchengine.SearchQueryUtils;
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
                IndividualListResults vcResults = getResultsForVClass(
                        vclass.getURI(), 
                        page, 
                        alpha, 
                        vreq);                                
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
    
    //TODO: Remove and update reference within JsonServlet
    public static String getAlphaParameter(VitroRequest request){
        return SearchQueryUtils.getAlphaParameter(request);
    }
    
  //TODO: Remove and update reference within JsonServlet
    public static int getPageParameter(VitroRequest request) {
        return SearchQueryUtils.getPageParameter(request);
    }
    
    public static IndividualListResults getResultsForVClass(String vclassURI, int page, String alpha, VitroRequest vreq) 
    throws SearchException{
   	 	try{
            List<String> classUris = Collections.singletonList(vclassURI);
			IndividualListQueryResults results = buildAndExecuteVClassQuery(classUris, alpha, page, INDIVIDUALS_PER_PAGE, vreq.getWebappDaoFactory().getIndividualDao());
	        return getResultsForVClassQuery(results, page, INDIVIDUALS_PER_PAGE, alpha, vreq);
   	 	} catch (SearchEngineException e) {
   	 	    String msg = "An error occurred retrieving results for vclass query";
   	 	    log.error(msg, e);
   	 	    // Throw this up to processRequest, so the template gets the error message.
   	 	    throw new SearchException(msg);
	   	} catch(Throwable th) {
	   		log.error("An error occurred retrieving results for vclass query", th);
	   		return IndividualListResults.EMPTY;
	    }
    }
    
    public static IndividualListResults getResultsForVClassIntersections(List<String> vclassURIs, int page, int pageSize, String alpha, VitroRequest vreq) {
        try{
            IndividualListQueryResults results = buildAndExecuteVClassQuery(vclassURIs, alpha, page, pageSize, vreq.getWebappDaoFactory().getIndividualDao());
	        return getResultsForVClassQuery(results, page, pageSize, alpha, vreq);
        } catch(Throwable th) {
       	    log.error("Error retrieving individuals corresponding to intersection multiple classes." + vclassURIs.toString(), th);
       	    return IndividualListResults.EMPTY;
        }
    }
	
    public static IndividualListResults getRandomResultsForVClass(String vclassURI, int page, int pageSize, VitroRequest vreq) {
   	 	try{
            List<String> classUris = Collections.singletonList(vclassURI);
			IndividualListQueryResults results = buildAndExecuteRandomVClassQuery(classUris, page, pageSize, vreq.getWebappDaoFactory().getIndividualDao());
	        return getResultsForVClassQuery(results, page, pageSize, "", vreq);
   	 	} catch(Throwable th) {
	   		log.error("An error occurred retrieving random results for vclass query", th);
	   		return IndividualListResults.EMPTY;
	    }
    }

	private static IndividualListResults getResultsForVClassQuery(IndividualListQueryResults results, int page, int pageSize, String alpha, VitroRequest vreq) {
        long hitCount = results.getHitCount();
        if ( hitCount > pageSize ){
        	return new IndividualListResults(hitCount, results.getIndividuals(), alpha, true, makePagesList(hitCount, pageSize, page, vreq));
        }else{
        	return new IndividualListResults(hitCount, results.getIndividuals(), alpha, false, Collections.<PageRecord>emptyList());
        }
    }
     
    
    private static IndividualListQueryResults buildAndExecuteVClassQuery(
			List<String> vclassURIs, String alpha, int page, int pageSize, IndividualDao indDao)
			throws SearchEngineException {
		 SearchQuery query = SearchQueryUtils.getQuery(vclassURIs, alpha, page, pageSize);
		 IndividualListQueryResults results = IndividualListQueryResults.runQuery(query, indDao);
		 log.debug("Executed search query for " + vclassURIs);
		 if (results.getIndividuals().isEmpty()) { 
			 log.debug("entities list is null for vclass " + vclassURIs);
		 }
		return results;
	}

    private static IndividualListQueryResults buildAndExecuteRandomVClassQuery(
			List<String> vclassURIs, int page, int pageSize, IndividualDao indDao)
			throws SearchEngineException {
		 SearchQuery query = SearchQueryUtils.getRandomQuery(vclassURIs, page, pageSize);
		 IndividualListQueryResults results = IndividualListQueryResults.runQuery(query, indDao);
		 log.debug("Executed search query for " + vclassURIs);
		 if (results.getIndividuals().isEmpty()) { 
			 log.debug("entities list is null for vclass " + vclassURIs);
		 }
		return results;
	}

    
    public static List<PageRecord> makePagesList( long size, int pageSize,  int selectedPage , VitroRequest vreq) {        

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
            records.add( new PageRecord( "page="+ (MAX_PAGES+1), Integer.toString(MAX_PAGES+1), I18n.text(vreq, "paging_link_more"), false));
        }else if( requiredPages > MAX_PAGES && selectedPage+1 > MAX_PAGES && selectedPage < requiredPages - MAX_PAGES){
            //the selected pages is in the middle of the list of page
            int startPage = selectedPage - MAX_PAGES / 2;
            int endPage = selectedPage + MAX_PAGES / 2;            
            for(int page = startPage; page <= endPage ; page++ ){
                records.add( new PageRecord( "page=" + page, Integer.toString(page), Integer.toString(page), selectedPage == page ) );            
            }
            records.add( new PageRecord( "page="+ (endPage+1), Integer.toString(endPage+1), I18n.text(vreq, "paging_link_more"), false));
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
