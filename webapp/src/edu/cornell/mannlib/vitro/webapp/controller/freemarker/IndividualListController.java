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
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneIndexFactory;
import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc.VitroLuceneTermNames;
import edu.cornell.mannlib.vitro.webapp.search.solr.SolrSetup;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.ListedIndividualTemplateModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModel;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/** 
 * Generates a list of individuals for display in a template 
 */
public class IndividualListController extends FreemarkerHttpServlet {
  
    private static final long serialVersionUID = 1L;   
    private static final Log log = LogFactory.getLog(IndividualListController.class.getName());

    public static final int ENTITY_LIST_CONTROLLER_MAX_RESULTS = 30000;
    public static final int INDIVIDUALS_PER_PAGE = 30;
    public static final int MAX_PAGES = 40; //must be even
    
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
                        throw new HelpException("IndividualListController: request parameter 'vclassId' must be a URI string.");
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

                List<Individual> inds = (List<Individual>)map.get("entities");
                List<ListedIndividualTemplateModel> indsTm = new ArrayList<ListedIndividualTemplateModel>();
                for(Individual ind : inds ){
                    indsTm.add(new ListedIndividualTemplateModel(ind,vreq));
                }
                body.put("individuals", indsTm);
                
                List<TemplateModel> wpages = new ArrayList<TemplateModel>();
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
    
    /**
     * This method is now called in a couple of places.  It should be refactored
     * into a DAO or similar object.
     */
     public static Map<String,Object> getResultsForVClass(String vclassURI, int page, String alpha, IndividualDao indDao, ServletContext context) 
     throws CorruptIndexException, IOException, ServletException{
    	 Map<String,Object> rvMap = new HashMap<String,Object>();    
    	 try{
	         //make lucene query for this rdf:type
    		 List<String> classUris = new ArrayList<String>();
    		 classUris.add(vclassURI);
	        Query query = getQuery(classUris, alpha);        
	        rvMap = getResultsForVClassQuery(query, page, alpha, indDao, context);
	        List<Individual> individuals = (List<Individual>) rvMap.get("entities");
	        if (individuals == null) 
	             log.debug("entities list is null for vclass " + vclassURI );                        
    	 } catch(Throwable th) {
    		 log.error("An error occurred retrieving results for vclass query", th);
    	 }
         return rvMap;
     }
     
     /*
      * This method includes what was formerly a part of the method above, allowing for refactoring of code
      * to use for a different number fo classes
      */
     
     public static Map<String,Object> getResultsForVClassQuery(Query query, int page, String alpha, IndividualDao indDao, ServletContext context) 
     throws CorruptIndexException, IOException, ServletException{
         Map<String,Object> rvMap = new HashMap<String,Object>();        
         
         //execute lucene query for individuals of the specified type
         IndexSearcher index = LuceneIndexFactory.getIndexSearcher(context);
         TopDocs docs = null;
         try{
             docs = index.search(query, null, 
                 ENTITY_LIST_CONTROLLER_MAX_RESULTS, 
                 new Sort(Entity2LuceneDoc.term.NAME_LOWERCASE));
         }catch(Throwable th){
             log.error("Could not run search. " + th.getMessage());
             docs = null;
         }
         
         if( docs == null )            
             throw new ServletException("Could not run search in IndividualListController");        
         
         //get list of individuals for the search results
         int size = docs.totalHits;
         log.debug("Number of search results: " + size);
         
         // don't get all the results, only get results for the requestedSize
         List<Individual> individuals = new ArrayList<Individual>(INDIVIDUALS_PER_PAGE);
         int individualsAdded = 0;
         int ii = (page-1)*INDIVIDUALS_PER_PAGE;               
         while( individualsAdded < INDIVIDUALS_PER_PAGE && ii < size ){
             ScoreDoc hit = docs.scoreDocs[ii];
             if (hit != null) {
                 Document doc = index.doc(hit.doc);
                 if (doc != null) {                                                                                        
                     String uri = doc.getField(Entity2LuceneDoc.term.URI).stringValue();
                     Individual ind = indDao.getIndividualByURI( uri );  
                     if( ind != null ){
                         individuals.add( ind );                         
                         individualsAdded++;
                     }
                 } else {
                     log.warn("no document found for lucene doc id " + hit.doc);
                 }
             } else {
                 log.debug("hit was null");
             }                         
             ii++;            
         }   
         
         rvMap.put("count", size);
         
         if( size > INDIVIDUALS_PER_PAGE ){
             rvMap.put("showPages", Boolean.TRUE);
             List<PageRecord> pageRecords = makePagesList(size, INDIVIDUALS_PER_PAGE, page);
             rvMap.put("pages", pageRecords);                    
         }else{
             rvMap.put("showPages", Boolean.FALSE);
             rvMap.put("pages", Collections.emptyList());
         }
                         
         rvMap.put("alpha",alpha);
         
         rvMap.put("totalCount", size);
         rvMap.put("entities",individuals);                     
         
         return rvMap;
     }
     
   
     
     //Solr based version
     public static Map<String,Object> getResultsForVClassQuery(SolrQuery query, int page, String alpha, IndividualDao indDao, ServletContext context) 
     throws CorruptIndexException, IOException, ServletException{
         Map<String,Object> rvMap = new HashMap<String,Object>();        
         //Execute solr query
         SolrServer solr = SolrSetup.getSolrServer(context);
         QueryResponse response = null;
         
         try {
             response = solr.query(query);

         } catch (Throwable t) {
             log.error("in first pass at search: " + t);
             // this is a hack to deal with odd cases where search and index threads interact
             try{
            	 //Can't use the method below in a static class?
                 //wait(150);
                 response = solr.query(query);
             } catch (Exception ex) {
                 log.error(ex);
                 //doFailedSearch()
                 //return doFailedSearch(msg, qtxt, format);
             }
         }
         
        
         SolrDocumentList docs = response.getResults();
         if( docs == null )            
             throw new ServletException("Could not run search in IndividualListController");        
         
         //get list of individuals for the search results
         
         int size = new Long(docs.getNumFound()).intValue();
         log.debug("Number of search results: " + size);
         
         // don't get all the results, only get results for the requestedSize
         List<Individual> individuals = new ArrayList<Individual>(INDIVIDUALS_PER_PAGE);
         int individualsAdded = 0;
         int ii = (page-1)*INDIVIDUALS_PER_PAGE;               
         while( individualsAdded < INDIVIDUALS_PER_PAGE && ii < size ){
        	 SolrDocument doc = docs.get(ii);
        	 if (doc != null) {   
	        	 String uri = doc.get(VitroLuceneTermNames.URI).toString();
	             log.debug("Retrieving individual with uri "+ uri);
	             Individual ind = indDao.getIndividualByURI(uri);
	             if( ind != null ){
	                 individuals.add( ind );                         
	                 individualsAdded++;
	             } 
	         } else {
                 log.warn("no document found for lucene doc id " + doc);
             }
             ii++;            
         }   
         
         rvMap.put("count", size);
         
         if( size > INDIVIDUALS_PER_PAGE ){
             rvMap.put("showPages", Boolean.TRUE);
             List<PageRecord> pageRecords = makePagesList(size, INDIVIDUALS_PER_PAGE, page);
             rvMap.put("pages", pageRecords);                    
         }else{
             rvMap.put("showPages", Boolean.FALSE);
             rvMap.put("pages", Collections.emptyList());
         }
                         
         rvMap.put("alpha",alpha);
         
         rvMap.put("totalCount", size);
         rvMap.put("entities",individuals);                     
         
         return rvMap;
     }
     
    
    
     public static Map<String,Object> getResultsForVClassIntersections(List<String> vclassURIs, int page, String alpha, IndividualDao indDao, ServletContext context) 
     throws CorruptIndexException, IOException, ServletException{
         Map<String,Object> rvMap = new HashMap<String,Object>();  
         try{
             //make lucene query for multiple rdf types 
        	 //change to solr
	         SolrQuery query = getSolrQuery(vclassURIs, alpha);     
	         //get results corresponding to this query
	         rvMap = getResultsForVClassQuery(query, page, alpha, indDao, context);
	         List<Individual> individuals = (List<Individual>) rvMap.get("entities");
		     if (individuals == null) 
		       log.debug("entities list is null for vclass " + vclassURIs.toString() );     
         } catch(Throwable th) {
        	 log.error("Error retrieving individuals corresponding to intersection multiple classes." + vclassURIs.toString(), th);
         }
         return rvMap;
     }
     
     
     /*
      * This method creates a query to search for terms with rdf type corresponding to vclass Uri.
      * The original version allowed only for one class URI but needed to be extended to enable multiple
      *  vclass Uris to be passed
      */
     
     private static BooleanQuery getQuery(List<String>vclassUris,  String alpha){
         BooleanQuery query = new BooleanQuery();
         try{      
            //query term for rdf:type - multiple types possible
        	for(String vclassUri: vclassUris) {
	            query.add(
	                    new TermQuery( new Term(Entity2LuceneDoc.term.RDFTYPE, vclassUri)),
	                    BooleanClause.Occur.MUST );                          
        	}                           
            //Add alpha filter if it is needed
            Query alphaQuery = null;
            if( alpha != null && !"".equals(alpha) && alpha.length() == 1){      
                alphaQuery =    
                    new PrefixQuery(new Term(Entity2LuceneDoc.term.NAME_LOWERCASE, alpha.toLowerCase()));
                query.add(alphaQuery,BooleanClause.Occur.MUST);
            }                      
                            
            log.debug("Query: " + query);
            return query;
        } catch (Exception ex){
            log.error(ex,ex);
            return new BooleanQuery();        
        }        
     }  
     
     //how to extend for solr query
     //Alpha handling taken from SolrIndividualListController
     private static SolrQuery getSolrQuery(List<String>vclassUris,  String alpha){
    	 //SolrQuery query = getQuery(qtxt, maxHitCount, vreq);            
         //SolrServer solr = SolrSetup.getSolrServer(getServletContext());
    	 SolrQuery query = new SolrQuery();
         
         // Solr requires these values, but we don't want them to be the real values for this page
         // of results, else the refinement links won't work correctly: each page of results needs to
         // show refinement links generated for all results, not just for the results on the current page.
         query.setStart(0)
              .setRows(1000);
         String queryText = "";
         //Query text should be of form: 
         List<String> queryTypes = new ArrayList<String>();
         try{      
             //query term for rdf:type - multiple types possible
         	for(String vclassUri: vclassUris) {
         		queryTypes.add(VitroLuceneTermNames.RDFTYPE + ":\"" + vclassUri + "\" ");
         		
         	} 
         	
         	if(queryTypes.size() > 1) {
         		queryText = StringUtils.join(queryTypes, " AND ");
         	} else {
         		if(queryTypes.size() > 0) {
         			queryText = queryTypes.get(0);
         		}
         	}
         	
         	 // Add alpha filter if it is needed
            if ( alpha != null && !"".equals(alpha) && alpha.length() == 1) {      
                queryText += VitroLuceneTermNames.NAME_LOWERCASE + ":" + alpha.toLowerCase() + "*";
            }     
            
         	log.debug("Query text is " + queryText);
         	query.setQuery(queryText);
            log.debug("Query: " + query);
             return query;
         } catch (Exception ex){
             log.error(ex,ex);
         
             return new SolrQuery();        
         }       
     
     }  
     
     public static List<PageRecord> makePagesList( int count, int pageSize,  int selectedPage){        
         
         List<PageRecord> records = new ArrayList<PageRecord>( MAX_PAGES + 1 );
         int requiredPages = count/pageSize ;
         int remainder = count % pageSize ; 
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
             double max = Math.ceil(count/pageSize);
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
