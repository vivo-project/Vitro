/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.search.IndexConstants;
import edu.cornell.mannlib.vitro.webapp.search.SearchException;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQuery;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQueryFactory;
import edu.cornell.mannlib.vitro.webapp.search.solr.SolrSetup;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.LinkTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individuallist.BaseListedIndividual;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.searchresult.IndividualSearchResult;
import freemarker.template.Configuration;

/**
 * Paged search controller that uses Solr
 *  
 * @author bdc34, rjy7
 * 
 */

public class SolrPagedSearchController extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(SolrPagedSearchController.class);
    
    private static final int DEFAULT_HITS_PER_PAGE = 25;
    private static final int DEFAULT_MAX_HIT_COUNT = 1000;   

    private static final String PARAM_XML_REQUEST = "xml";
    private static final String PARAM_START_INDEX = "startIndex";
    private static final String PARAM_HITS_PER_PAGE = "hitsPerPage";
    private static final String PARAM_CLASSGROUP = "classgroup";
    private static final String PARAM_RDFTYPE = "type";
    private static final String PARAM_QUERY_TEXT = "querytext";

    protected static final Map<Format,Map<Result,String>> templateTable;

    protected enum Format { 
        HTML, XML; 
    }
    
    protected enum Result {
        PAGED, ERROR, BAD_QUERY         
    }
    
    static{
        templateTable = setupTemplateTable();
    }
         
    /**
     * Overriding doGet from FreemarkerHttpController to do a page template (as
     * opposed to body template) style output for XML requests.
     * 
     * This follows the pattern in AutocompleteController.java.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        boolean wasXmlRequested = isRequestedFormatXml(request);
        if( ! wasXmlRequested ){
            super.doGet(request,response);
        }else{
            try {
                VitroRequest vreq = new VitroRequest(request);
                Configuration config = getConfig(vreq);            
                ResponseValues rvalues = processRequest(vreq);
                
                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/xml;charset=UTF-8");
                writeTemplate(rvalues.getTemplateName(), rvalues.getMap(), config, request, response);
            } catch (Exception e) {
                log.error(e, e);
            }
        }
    }

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
    	
    	log.debug("All parameters present in the request: "+ vreq.getParameterMap().toString());
    	
        //There may be other non-html formats in the future
        Format format = getFormat(vreq);            
        boolean wasXmlRequested = Format.XML == format;
        log.debug("Requested format was " + (wasXmlRequested ? "xml" : "html"));
        boolean wasHtmlRequested = ! wasXmlRequested; 
        
        try {
            
            //make sure an IndividualDao is available 
            if( vreq.getWebappDaoFactory() == null 
                    || vreq.getWebappDaoFactory().getIndividualDao() == null ){
                log.error("Could not get webappDaoFactory or IndividualDao");
                throw new Exception("Could not access model.");
            }
            IndividualDao iDao = vreq.getWebappDaoFactory().getIndividualDao();
            VClassGroupDao grpDao = vreq.getWebappDaoFactory().getVClassGroupDao();
            VClassDao vclassDao = vreq.getWebappDaoFactory().getVClassDao();
            
            ApplicationBean appBean = vreq.getAppBean();
            
            log.debug("IndividualDao is " + iDao.toString() + " Public classes in the classgroup are " + grpDao.getPublicGroupsWithVClasses().toString());
            log.debug("VClassDao is "+ vclassDao.toString() );            
            
            int startIndex = 0;
            try{ 
                startIndex = Integer.parseInt(vreq.getParameter(PARAM_START_INDEX)); 
            }catch (Throwable e) { 
                startIndex = 0; 
            }            
            log.debug("startIndex is " + startIndex);                       
            
            int hitsPerPage = DEFAULT_HITS_PER_PAGE;
            try{ 
                hitsPerPage = Integer.parseInt(vreq.getParameter(PARAM_HITS_PER_PAGE)); 
            } catch (Throwable e) { 
                hitsPerPage = DEFAULT_HITS_PER_PAGE; 
            }                        
            log.debug("hitsPerPage is " + hitsPerPage);
            
            int maxHitCount = DEFAULT_MAX_HIT_COUNT ;
            if( startIndex >= DEFAULT_MAX_HIT_COUNT  - hitsPerPage )
                maxHitCount = startIndex + DEFAULT_MAX_HIT_COUNT ;

            log.debug("maxHitCount is " + maxHitCount);

            String qtxt = vreq.getParameter(VitroQuery.QUERY_PARAMETER_NAME);
            
            log.debug("Query text is \""+ qtxt + "\""); 

            SolrQuery query = getQuery(qtxt, maxHitCount, vreq);            
            SolrServer solr = SolrSetup.getSolrServer(getServletContext());
            QueryResponse response = null;
           
            
            try {
                response = solr.query(query);

            } catch (Throwable t) {
                log.error("in first pass at search: " + t);
                // this is a hack to deal with odd cases where search and index threads interact
                try{
                    wait(150);
                    response = solr.query(query);
                } catch (Exception ex) {
                    log.error(ex);
                    String msg = makeBadSearchMessage(qtxt, ex.getMessage());
                    if (msg == null) {
                        msg = "The search request contained errors.";
                    }
                    return doFailedSearch(msg, qtxt, format);
                }
            }
            
            if (response == null) {
                log.error("Search response was null");                
                String msg = "The search request contained errors.";
                return doFailedSearch(msg, qtxt, format);
            }
            
            SolrDocumentList docs = response.getResults();

            if (docs == null) {
                log.error("Document list for a search was null");                
                String msg = "The search request contained errors.";
                return doFailedSearch(msg, qtxt,format);
            }
                       
            long hitCount = docs.getNumFound();
            log.debug("Number of hits = " + hitCount);
            if ( hitCount < 1 ) {                
                return doNoHits(qtxt,format);
            }            

            long lastHitToShow = 0;
            if ((startIndex + hitsPerPage) > hitCount ) {
                lastHitToShow = hitCount;
            } else {
                lastHitToShow = startIndex + hitsPerPage;
            }
            
            List<Individual> individuals = new LinkedList<Individual>();      
            for(int i = startIndex; i < lastHitToShow; i++){
                try {                    
                    SolrDocument doc = docs.get(i);
                    String uri = doc.get(VitroSearchTermNames.URI).toString();
                    log.debug("Retrieving individual with uri "+ uri);
                    Individual ent = new IndividualImpl();
                    ent.setURI(uri);
                    ent = iDao.getIndividualByURI(uri);
                    if(ent!=null)
                        individuals.add(ent);
                } catch(Exception e) {
                    log.error("Problem getting usable individuals from search hits. " +
                            e.getMessage());
                }
            }          
  
            ParamMap pagingLinkParams = new ParamMap();
            pagingLinkParams.put(PARAM_QUERY_TEXT, qtxt);
            pagingLinkParams.put(PARAM_HITS_PER_PAGE, String.valueOf(hitsPerPage));
            
            if( wasXmlRequested ){
                pagingLinkParams.put(PARAM_XML_REQUEST,"1");                
            }
            
            /* Compile the data for the templates */
            
            Map<String, Object> body = new HashMap<String, Object>();
            
            String classGroupParam = vreq.getParameter(PARAM_CLASSGROUP);    
            boolean classGroupFilterRequested = false;
            if (!StringUtils.isEmpty(classGroupParam)) {
                VClassGroup grp = grpDao.getGroupByURI(classGroupParam);
                classGroupFilterRequested = true;
                if (grp != null && grp.getPublicName() != null)
                    body.put("classGroupName", grp.getPublicName());
            }
            
            String typeParam = vreq.getParameter(PARAM_RDFTYPE);
            boolean typeFilterRequested = false;
            if (!StringUtils.isEmpty(typeParam)) {
                VClass type = vclassDao.getVClassByURI(typeParam);
                typeFilterRequested = true;
                if (type != null && type.getName() != null)
                    body.put("typeName", type.getName());
            }
            
            /* Add classgroup and type refinement links to body */
            if( wasHtmlRequested ){                                
                // Search request includes no classgroup and no type, so add classgroup search refinement links.
                if ( !classGroupFilterRequested && !typeFilterRequested ) { 
                    List<VClassGroup> classgroups = getClassGroups(grpDao, docs, maxHitCount);
                    List<VClassGroupSearchLink> classGroupLinks = new ArrayList<VClassGroupSearchLink>(classgroups.size());
                    for (VClassGroup vcg : classgroups) {
                        if (vcg.getPublicName() != null) {
                            classGroupLinks.add(new VClassGroupSearchLink(qtxt, vcg));
                        }
                    }
                    body.put("classGroupLinks", classGroupLinks);                       
     
                // Search request is for a classgroup, so add rdf:type search refinement links
                // but try to filter out classes that are subclasses
                } else if ( classGroupFilterRequested && !typeFilterRequested ) {  
                    List<VClass> vClasses = getVClasses(vclassDao, docs);
                    List<VClassSearchLink> vClassLinks = new ArrayList<VClassSearchLink>(vClasses.size());
                    for (VClass vc : vClasses) {
                        vClassLinks.add(new VClassSearchLink(qtxt, vc));
                    }
                    body.put("classLinks", vClassLinks);                       
                    pagingLinkParams.put(PARAM_CLASSGROUP, classGroupParam);

                } else {
                    pagingLinkParams.put(PARAM_RDFTYPE, typeParam);
                }
            }           

            body.put("individuals", IndividualSearchResult
                    .getIndividualTemplateModels(individuals, vreq));

            body.put("querytext", qtxt);
            body.put("title", qtxt + " - " + appBean.getApplicationName()
                    + " Search Results");
            
            body.put("hitCount", hitCount);
            body.put("startIndex", startIndex);
            
            body.put("pagingLinks", getPagingLinks(startIndex, hitsPerPage,
                    hitCount, maxHitCount, vreq.getServletPath(),
                    pagingLinkParams));

            if (startIndex != 0) {
                body.put("prevPage", getPreviousPageLink(startIndex,
                        hitsPerPage, vreq.getServletPath(), pagingLinkParams));
            }
            if (startIndex < (hitCount - hitsPerPage)) {
                body.put("nextPage", getNextPageLink(startIndex, hitsPerPage,
                        vreq.getServletPath(), pagingLinkParams));
            }

            String template = templateTable.get(format).get(Result.PAGED);
            
            return new TemplateResponseValues(template, body);
        } catch (Throwable e) {
            return doSearchError(e,format);
        }        
    }


    /**
     * Get the class groups represented for the individuals in the documents.
     */
    private List<VClassGroup> getClassGroups(VClassGroupDao grpDao, SolrDocumentList docs, int maxHitCount) {        
        LinkedHashMap<String,VClassGroup> grpMap = grpDao.getClassGroupMap();
        int n = grpMap.size();
        
        HashSet<String> classGroupsInHits = new HashSet<String>(n);
        int grpsFound = 0;
        
        long maxHits = Math.min(docs.getNumFound(), maxHitCount);
        for(int i = 0; i < maxHits && n > grpsFound ;i++){
            try{
                SolrDocument doc = docs.get(i);        
                Collection<Object> grps = doc.getFieldValues(VitroSearchTermNames.CLASSGROUP_URI);     
                if (grps != null) {
                    for (Object o : grps) {                            
                        String groupUri = o.toString();
                        if( groupUri != null && !classGroupsInHits.contains(groupUri)){
                            classGroupsInHits.add(groupUri);
                            grpsFound++;
                            if( grpsFound >= n )
                                break;
                        }                        
                    }
                }
            } catch(Exception e) {
                log.error("problem getting VClassGroups from search hits " 
                        + e.getMessage() );
                e.printStackTrace();
            }
        }            
        
        List<String> classgroupURIs= Collections.list(Collections.enumeration(classGroupsInHits));        
        List<VClassGroup> classgroups = new ArrayList<VClassGroup>( classgroupURIs.size() );
        for(String cgUri: classgroupURIs){
            if( cgUri != null && ! "".equals(cgUri) ){
                VClassGroup vcg = grpDao.getGroupByURI( cgUri );
                if( vcg == null ){
                    log.debug("could not get classgroup for URI " + cgUri);
                }else{
                    classgroups.add(vcg);
                }
            }          
        }        
        grpDao.sortGroupList(classgroups);     
                
        return classgroups;
    }

    private List<VClass> getVClasses(VClassDao vclassDao, SolrDocumentList docs){        
        HashSet<String> typesInHits = getVClassUrisForHits(docs);                                
        List<VClass> classes = new ArrayList<VClass>(typesInHits.size());
        
        Iterator<String> it = typesInHits.iterator();
        while(it.hasNext()){
            String typeUri = it.next();
            try{
                if( VitroVocabulary.OWL_THING.equals(typeUri))
                    continue;
                VClass type = vclassDao.getVClassByURI(typeUri);
                if( type != null &&
                    ! type.isAnonymous() &&
                      type.getName() != null && !"".equals(type.getName()) &&
                      type.getGroupURI() != null ) //don't display classes that aren't in classgroups                      
                    classes.add(type);
            }catch(Exception ex){
                if( log.isDebugEnabled() )
                    log.debug("could not add type " + typeUri, ex);
            }                        
        }
        Collections.sort(classes, new Comparator<VClass>(){
            public int compare(VClass o1, VClass o2) {                
                return o1.compareTo(o2);
            }});
        return classes;
    }       
        
    private HashSet<String> getVClassUrisForHits(SolrDocumentList docs){
        HashSet<String> typesInHits = new HashSet<String>();  
        for (SolrDocument doc : docs) {
            try {
                Collection<Object> types = doc.getFieldValues(VitroSearchTermNames.RDFTYPE);     
                if (types != null) {
                    for (Object o : types) {
                        String typeUri = o.toString();
                        typesInHits.add(typeUri);
                    }
                }
            } catch (Exception e) {
                log.error("problems getting rdf:type for search hits",e);
            }
        }
        return typesInHits;
    }       

    private SolrQuery getQuery(String queryText, int maxHitCount, VitroRequest vreq) {
        SolrQuery query = new SolrQuery(queryText);
        
        // Solr requires these values, but we don't want them to be the real values for this page
        // of results, else the refinement links won't work correctly: each page of results needs to
        // show refinement links generated for all results, not just for the results on the current page.
        query.setStart(0)
             .setRows(maxHitCount);

        // Classgroup filtering
        String classgroupParam = (String) vreq.getParameter(PARAM_CLASSGROUP);
        if ( ! StringUtils.isBlank(classgroupParam) ) {           
            log.debug("Firing classgroup query ");
            log.debug("request.getParameter(classgroup) is "+ classgroupParam);
            query.addFilterQuery(VitroSearchTermNames.CLASSGROUP_URI + ":\"" + classgroupParam + "\"");
        }

        // rdf:type filtering
        String typeParam = (String) vreq.getParameter(PARAM_RDFTYPE);
        if (  ! StringUtils.isBlank(typeParam) ) {                         
            log.debug("Firing type query ");
            log.debug("request.getParameter(type) is "+ typeParam);   
            query.addFilterQuery(VitroSearchTermNames.RDFTYPE + ":\"" + typeParam + "\"");
        }
                
        //query.setQuery(queryText);
        log.debug("Query = " + query.toString());
        return query;
    }

    private class VClassGroupSearchLink extends LinkTemplateModel {
        
        VClassGroupSearchLink(String querytext, VClassGroup classgroup) {
            super(classgroup.getPublicName(), "/search", PARAM_QUERY_TEXT, querytext, PARAM_CLASSGROUP, classgroup.getURI());
        }
    }
    
    private class VClassSearchLink extends LinkTemplateModel {
        
        VClassSearchLink(String querytext, VClass type) {
            super(type.getName(), "/search", PARAM_QUERY_TEXT, querytext, PARAM_RDFTYPE, type.getURI());
        }
    }
    
    private List<PagingLink> getPagingLinks(int startIndex, int hitsPerPage, long hitCount, int maxHitCount, String baseUrl, ParamMap params) {

        List<PagingLink> pagingLinks = new ArrayList<PagingLink>();
        
        // No paging links if only one page of results
        if (hitCount <= hitsPerPage) {
            return pagingLinks;
        }
        
        for (int i = 0; i < hitCount; i += hitsPerPage) {
            params.put(PARAM_START_INDEX, String.valueOf(i));
            if ( i < maxHitCount - hitsPerPage) {
                int pageNumber = i/hitsPerPage + 1;
                if (i >= startIndex && i < (startIndex + hitsPerPage)) {
                    pagingLinks.add(new PagingLink(pageNumber));
                } else {
                    pagingLinks.add(new PagingLink(pageNumber, baseUrl, params));
                }
            } else {
                pagingLinks.add(new PagingLink("more...", baseUrl, params));
            }
        }   
        
        return pagingLinks;
    }
    
    private String getPreviousPageLink(int startIndex, int hitsPerPage, String baseUrl, ParamMap params) {
        params.put(PARAM_START_INDEX, String.valueOf(startIndex-hitsPerPage));
        return UrlBuilder.getUrl(baseUrl, params);
    }
    
    private String getNextPageLink(int startIndex, int hitsPerPage, String baseUrl, ParamMap params) {
        params.put(PARAM_START_INDEX, String.valueOf(startIndex+hitsPerPage));
        return UrlBuilder.getUrl(baseUrl, params);
    }
    
    private class PagingLink extends LinkTemplateModel {
        
        PagingLink(int pageNumber, String baseUrl, ParamMap params) {
            super(String.valueOf(pageNumber), baseUrl, params);
        }
        
        // Constructor for current page item: not a link, so no url value.
        PagingLink(int pageNumber) {
            setText(String.valueOf(pageNumber));
        }
        
        // Constructor for "more..." item
        PagingLink(String text, String baseUrl, ParamMap params) {
            super(text, baseUrl, params);
        }
    }
   
    private ExceptionResponseValues doSearchError(Throwable e, Format f) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("message", "Search failed: " + e.getMessage());  
        return new ExceptionResponseValues(getTemplate(f,Result.ERROR), body, e);
    }
    
//    private TemplateResponseValues doBadQuery(ApplicationBean appBean, String query, Format f) {
//        Map<String, Object> body = new HashMap<String, Object>();
//        body.put("title", "Search " + appBean.getApplicationName());
//        body.put("query", query);
//        return new TemplateResponseValues(getTemplate(f,Result.BAD_QUERY), body);
//    }
    
    private TemplateResponseValues doFailedSearch(String message, String querytext, Format f) {
        Map<String, Object> body = new HashMap<String, Object>();       
        body.put("title", "Search for '" + querytext + "'");        
        if ( StringUtils.isEmpty(message) ) {
            message = "Search failed.";
        }        
        body.put("message", message);
        return new TemplateResponseValues(getTemplate(f,Result.ERROR), body);
    }

    private TemplateResponseValues doNoHits(String querytext, Format f) {
        Map<String, Object> body = new HashMap<String, Object>();       
        body.put("title", "Search for '" + querytext + "'");        
        body.put("message", "No matching results.");     
        return new TemplateResponseValues(getTemplate(f,Result.ERROR), body);        
    }

    /**
     * Makes a message to display to user for a bad search term.
     * @param query
     * @param exceptionMsg
     */
    private String makeBadSearchMessage(String querytext, String exceptionMsg){
        String rv = "";
        try{
            //try to get the column in the search term that is causing the problems
            int coli = exceptionMsg.indexOf("column");
            if( coli == -1) return "";
            int numi = exceptionMsg.indexOf(".", coli+7);
            if( numi == -1 ) return "";
            String part = exceptionMsg.substring(coli+7,numi );
            int i = Integer.parseInt(part) - 1;

            // figure out where to cut preview and post-view
            int errorWindow = 5;
            int pre = i - errorWindow;
            if (pre < 0)
                pre = 0;
            int post = i + errorWindow;
            if (post > querytext.length())
                post = querytext.length();
            // log.warn("pre: " + pre + " post: " + post + " term len:
            // " + term.length());

            // get part of the search term before the error and after
            String before = querytext.substring(pre, i);
            String after = "";
            if (post > i)
                after = querytext.substring(i + 1, post);

            rv = "The search term had an error near <span class='searchQuote'>"
                + before + "<span class='searchError'>" + querytext.charAt(i)
                + "</span>" + after + "</span>";
        } catch (Throwable ex) {
            return "";
        }
        return rv;
    }
    
    @SuppressWarnings({ "unchecked", "unused" })
    private HashSet<String> getDataPropertyBlacklist(){
        HashSet<String>dpBlacklist = (HashSet<String>)
        getServletContext().getAttribute(IndexConstants.SEARCH_DATAPROPERTY_BLACKLIST);
        return dpBlacklist;        
    }
    
    @SuppressWarnings({ "unchecked", "unused" })
    private HashSet<String> getObjectPropertyBlacklist(){
        HashSet<String>opBlacklist = (HashSet<String>)
        getServletContext().getAttribute(IndexConstants.SEARCH_OBJECTPROPERTY_BLACKLIST);
        return opBlacklist;        
    }
    
    public static final int MAX_QUERY_LENGTH = 500;

    public VitroQueryFactory getQueryFactory() {
        throw new Error("PagedSearchController.getQueryFactory() is unimplemented");
    }

    @SuppressWarnings("rawtypes")
    public List search(VitroQuery query) throws SearchException {
        throw new Error("PagedSearchController.search() is unimplemented");
    }

    protected boolean isRequestedFormatXml(HttpServletRequest req){
        if( req != null ){
            String param = req.getParameter(PARAM_XML_REQUEST);
            if( param != null && "1".equals(param)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    protected Format getFormat(HttpServletRequest req){
        if( req != null && req.getParameter("xml") != null && "1".equals(req.getParameter("xml")))
            return Format.XML;
        else 
            return Format.HTML;
    }
    
    protected static String getTemplate(Format format, Result result){
        if( format != null && result != null)
            return templateTable.get(format).get(result);
        else{
            log.error("getTemplate() must not have a null format or result.");
            return templateTable.get(Format.HTML).get(Result.ERROR);
        }
    }
    
    protected static Map<Format,Map<Result,String>> setupTemplateTable(){
        Map<Format,Map<Result,String>> templateTable = 
            new HashMap<Format,Map<Result,String>>();
        
        HashMap<Result,String> resultsToTemplates = new HashMap<Result,String>();
        
        // set up HTML format
        resultsToTemplates.put(Result.PAGED, "search-pagedResults.ftl");
        resultsToTemplates.put(Result.ERROR, "search-error.ftl");
        // resultsToTemplates.put(Result.BAD_QUERY, "search-badQuery.ftl");        
        templateTable.put(Format.HTML, Collections.unmodifiableMap(resultsToTemplates));
        
        // set up XML format
        resultsToTemplates = new HashMap<Result,String>();
        resultsToTemplates.put(Result.PAGED, "search-xmlResults.ftl");
        resultsToTemplates.put(Result.ERROR, "search-xmlError.ftl");
        // resultsToTemplates.put(Result.BAD_QUERY, "search-xmlBadQuery.ftl");        
        templateTable.put(Format.XML, Collections.unmodifiableMap(resultsToTemplates));
        
        return Collections.unmodifiableMap(templateTable);
    }
}
