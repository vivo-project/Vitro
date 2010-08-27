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
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Params;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.flags.PortalFlag;
import edu.cornell.mannlib.vitro.webapp.search.SearchException;
import edu.cornell.mannlib.vitro.webapp.search.beans.Searcher;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroHighlighter;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQuery;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQueryFactory;
import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneIndexer;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneSetup;
import edu.cornell.mannlib.vitro.webapp.search.lucene.SimpleLuceneHighlighter;
import edu.cornell.mannlib.vitro.webapp.utils.FlagMathUtils;
import edu.cornell.mannlib.vitro.webapp.utils.Html2Text;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.IndividualTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.LinkTemplateModel;
import freemarker.template.Configuration;

/**
 * PagedSearchController is the new search controller that interacts 
 * directly with the lucene API and returns paged, relevance ranked results.
 *  
 * @author bdc34
 * 
 * Rewritten to use Freemarker: rjy7
 *
 */
public class FreemarkerPagedSearchController extends FreemarkerHttpServlet implements Searcher {

    private static final long serialVersionUID = 1L;
    private IndexSearcher searcher = null;
    private static final Log log = LogFactory.getLog(FreemarkerPagedSearchController.class.getName());
    String NORESULT_MSG = "The search returned no results.";    
    private int defaultHitsPerPage = 25;
    private int defaultMaxSearchSize= 1000;   

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        LuceneIndexer indexer=(LuceneIndexer)getServletContext()
        .getAttribute(LuceneIndexer.class.getName());
        indexer.addSearcher(this);

        try{
            String indexDir = getIndexDir(getServletContext());        
            getIndexSearcher(indexDir);
        }catch(Exception ex){

        }                                           
    }

    protected String getBody(VitroRequest vreq, Map<String, Object> body, Configuration config) {  
        try {

            Portal portal = vreq.getPortal();
            PortalFlag portalFlag = vreq.getPortalFlag();
            
            //make sure an IndividualDao is available 
            if( vreq.getWebappDaoFactory() == null 
                    || vreq.getWebappDaoFactory().getIndividualDao() == null ){
                log.error("makeUsableBeans() could not get IndividualDao ");
                return doSearchError("Could not access Model.", config);
            }
            IndividualDao iDao = vreq.getWebappDaoFactory().getIndividualDao();
            VClassGroupDao grpDao = vreq.getWebappDaoFactory().getVClassGroupDao();
            VClassDao vclassDao = vreq.getWebappDaoFactory().getVClassDao();
            String alphaFilter = vreq.getParameter("alpha");
            
            int startIndex = 0;
            try{ 
                startIndex = Integer.parseInt(vreq.getParameter("startIndex")); 
            }catch (Throwable e) { 
                startIndex = 0; 
            }            
            
            int hitsPerPage = defaultHitsPerPage;
            try{ 
                hitsPerPage = Integer.parseInt(vreq.getParameter("hitsPerPage")); 
            } catch (Throwable e) { 
                hitsPerPage = defaultHitsPerPage; 
            }                        
            
            int maxHitSize = defaultMaxSearchSize;
            if( startIndex >= defaultMaxSearchSize - hitsPerPage )
                maxHitSize = startIndex + defaultMaxSearchSize;
            if( alphaFilter != null ){
                maxHitSize = maxHitSize * 2;
                hitsPerPage = maxHitSize;
            }
            
            String indexDir = getIndexDir(getServletContext());
            
            String qtxt = vreq.getParameter(VitroQuery.QUERY_PARAMETER_NAME);
            Analyzer analyzer = getAnalyzer(getServletContext());
            Query query = getQuery(vreq, portalFlag, analyzer, indexDir, qtxt);             
            log.debug("query for '" + qtxt +"' is " + query.toString());

            if (query == null ) {
                return doNoQuery(config, portal);
            }
            
            IndexSearcher searcherForRequest = getIndexSearcher(indexDir);
                                                
            TopDocs topDocs = null;
            try{
                topDocs = searcherForRequest.search(query,null,maxHitSize);
            }catch(Throwable t){
                log.error("in first pass at search: " + t);
                // this is a hack to deal with odd cases where search and index threads interact
                try{
                    wait(150);
                    topDocs = searcherForRequest.search(query,null,maxHitSize);
                }catch (Exception ex){
                    log.error(ex);
                    String msg = makeBadSearchMessage(qtxt,ex.getMessage());
                    if (msg == null) {
                        msg = "The search request contained errors.";
                    }
                    return doFailedSearch(msg, qtxt, config);
                }
            }

            if( topDocs == null || topDocs.scoreDocs == null){
                log.error("topDocs for a search was null");                
                String msg = "The search request contained errors.";
                return doFailedSearch(msg, qtxt, config);
            }
            
            int hitsLength = topDocs.scoreDocs.length;
            if ( hitsLength < 1 ){                
                return doNoHits(qtxt, config);
            }            
            log.debug("found "+hitsLength+" hits");

            int lastHitToShow = 0;
            if((startIndex + hitsPerPage) > hitsLength ) {
                lastHitToShow = hitsLength;
            } else {
                lastHitToShow = startIndex + hitsPerPage - 1;
            }
            
            List<Individual> beans = new LinkedList<Individual>();                        
            for(int i=startIndex; i<topDocs.scoreDocs.length ;i++){
                try{
                    if( (i >= startIndex) && (i <= lastHitToShow) ){                        
                        Document doc = searcherForRequest.doc(topDocs.scoreDocs[i].doc);                    
                        String uri = doc.get(Entity2LuceneDoc.term.URI);
                        Individual ent = new IndividualImpl();
                        ent.setURI(uri);
                        ent = iDao.getIndividualByURI(uri);
                        if(ent!=null)
                            beans.add(ent);
                    }
                }catch(Exception e){
                    log.error("problem getting usable Individuals from search " +
                            "hits" + e.getMessage());
                }
            }            
  
            Params pagingLinkParams = new Params();
            pagingLinkParams.put("querytext", qtxt);

            String classGroupParam = vreq.getParameter("classgroup");
            String typeParam = vreq.getParameter("type");
            
            // Search request includes no classgroup and no type, so add classgroup search refinement links.
            if ( classGroupParam == null && typeParam == null) { 
                List<VClassGroup> classgroups = getClassGroups(grpDao, topDocs, searcherForRequest);
                List<VClassGroupSearchLink> classGroupLinks = new ArrayList<VClassGroupSearchLink>(classgroups.size());
                for (VClassGroup vcg : classgroups) {
                    classGroupLinks.add(new VClassGroupSearchLink(qtxt, vcg));
                }
                body.put("classGroupLinks", classGroupLinks);                       
 
            // Search request is for a classgroup, so add rdf:type search refinement links
            // but try to filter out classes that are subclasses
            } else if ( classGroupParam != null && typeParam == null ) {  
                List<VClass> vClasses = getVClasses(vclassDao,topDocs,searcherForRequest);
                List<VClassSearchLink> vClassLinks = new ArrayList<VClassSearchLink>(vClasses.size());
                for (VClass vc : vClasses) {
                    vClassLinks.add(new VClassSearchLink(qtxt, vc));
                }
                body.put("classLinks", vClassLinks);                       
                pagingLinkParams.put("classgroup", classGroupParam);
                
            // This case is never displayed
            } else if ( !StringUtils.isEmpty(alphaFilter) ) {                    
                body.put("alphas", getAlphas(topDocs, searcherForRequest));
                alphaSortIndividuals(beans);
                
            } else {
                pagingLinkParams.put("type", typeParam);
            }   
            
          beans = highlightBeans( beans , 
              vreq.getWebappDaoFactory().getDataPropertyDao(),
              vreq.getWebappDaoFactory().getObjectPropertyDao(),
              new SimpleLuceneHighlighter(query,analyzer) );            

            // Convert search result individuals to template model objects
            List<IndividualTemplateModel> individuals = new ArrayList<IndividualTemplateModel>(beans.size());
            for (Individual i : beans) {
              individuals.add(new IndividualTemplateModel(i));
            }
            body.put("individuals", individuals);            
            
            body.put("querytext", qtxt);
            body.put("title", qtxt+" - "+portal.getAppName()+" Search Results" );

            if ( !StringUtils.isEmpty(classGroupParam) ) {
                VClassGroup grp = grpDao.getGroupByURI(classGroupParam);
                if( grp != null && grp.getPublicName() != null )
                    body.put("classGroupName", grp.getPublicName());
            }
            
            if ( !StringUtils.isEmpty(typeParam) ) {
                VClass type = vclassDao.getVClassByURI(typeParam);
                if( type != null && type.getName() != null )
                    body.put("typeName", type.getName());
            }
            
            body.put("pagingLinks", getPagingLinks(startIndex, hitsPerPage,  hitsLength,  maxHitSize, vreq.getServletPath(), pagingLinkParams));
             
        } catch (Throwable e) {
            log.error(e, e);  
            return doSearchError(e.getMessage(), config);
        }
        
        return mergeBodyToTemplate("pagedSearchResults.ftl", body, config);
    }

    private void alphaSortIndividuals(List<Individual> beans) {
        Collections.sort(beans, new Comparator< Individual >(){
            public int compare(Individual o1, Individual o2) {
                if( o1 == null || o1.getName() == null )
                    return 1;
                else
                    return o1.getName().compareTo(o2.getName());
            }});        
    }

    private List<String> getAlphas(TopDocs topDocs, IndexSearcher searcher) {
        Set<String> alphas = new HashSet<String>();
        for(int i=0;i<topDocs.scoreDocs.length; i++){
            Document doc;
            try {
                doc = searcher.doc(topDocs.scoreDocs[i].doc);
                String name =doc.get(Entity2LuceneDoc.term.NAME);
                if( name != null && name.length() > 0)
                    alphas.add( name.substring(0, 1));                
            } catch (CorruptIndexException e) {
                log.debug("Could not get alphas for document",e);
            } catch (IOException e) {
                log.debug("Could not get alphas for document",e);
            }
        
        }
        return new ArrayList<String>(alphas);
    }

    /**
     * Get the class groups represented for the individuals in the topDocs.
     */
    private List<VClassGroup> getClassGroups(VClassGroupDao grpDao, TopDocs topDocs,
            IndexSearcher searcherForRequest) {        
        LinkedHashMap<String,VClassGroup> grpMap = grpDao.getClassGroupMap();
        int n = grpMap.size();
        
        HashSet<String> classGroupsInHits = new HashSet<String>(n);
        int grpsFound = 0;
        
        for(int i=0; i<topDocs.scoreDocs.length && n > grpsFound ;i++){
            try{
                Document doc = searcherForRequest.doc(topDocs.scoreDocs[i].doc);                    
                Field[] grps = doc.getFields(Entity2LuceneDoc.term.CLASSGROUP_URI);                
                if(grps != null || grps.length > 0){
                    for(int j=0;j<grps.length;j++){
                        String groupUri = grps[j].stringValue();
                        if( groupUri != null && ! classGroupsInHits.contains(groupUri)){
                            classGroupsInHits.add(groupUri);
                            grpsFound++;
                            if( grpsFound >= n )
                                break;
                        }                        
                    }                    
                }
            }catch(Exception e){
                log.error("problem getting VClassGroups from search hits " 
                        + e.getMessage());
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

    private class VClassGroupSearchLink extends LinkTemplateModel {
 
        VClassGroupSearchLink(String querytext, VClassGroup classgroup) {
            super(classgroup.getPublicName(), "/search", "querytext", querytext, "classgroup", classgroup.getURI());
        }
    }
    
    private class VClassSearchLink extends LinkTemplateModel {
        
        VClassSearchLink(String querytext, VClass type) {
            super(type.getName(), "/search", "querytext", querytext, "type", type.getURI());
        }
    }
    
    private List<PagingLink> getPagingLinks(int startIndex, int hitsPerPage, int hitsLength, int maxHitSize, String baseUrl, Params params) {

        List<PagingLink> pagingLinks = new ArrayList<PagingLink>();
        
        // No paging links if only one page of results
        if (hitsLength <= hitsPerPage) {
            return pagingLinks;
        }
        
        int pageNumber;

        params.put("hitsPerPage", String.valueOf(hitsPerPage));
        
        for (int i = 0; i < hitsLength; i += hitsPerPage) {
            params.put("startIndex", String.valueOf(i));
            if ( i < maxHitSize - hitsPerPage) {
                pageNumber = i/hitsPerPage + 1;
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
    
    private class PagingLink extends LinkTemplateModel {
        
        PagingLink(int pageNumber, String baseUrl, Params params) {
            super(String.valueOf(pageNumber), baseUrl, params);
        }
        
        // Constructor for current page item: not a link, so no url value.
        PagingLink(int pageNumber) {
            setText(String.valueOf(pageNumber));
        }
        
        // Constructor for "more..." item
        PagingLink(String text, String baseUrl, Params params) {
            super(text, baseUrl, params);
        }
        


    }
   
    private List<VClass> getVClasses(VClassDao vclassDao, TopDocs topDocs,
            IndexSearcher searherForRequest){        
        HashSet<String> typesInHits = getVClassUrisForHits(topDocs,searherForRequest);                                
        List<VClass> classes = new ArrayList<VClass>(typesInHits.size());
        
        Iterator<String> it = typesInHits.iterator();
        while(it.hasNext()){
            String typeUri = it.next();
            try{
                if( VitroVocabulary.OWL_THING.equals(typeUri))
                    continue;
                VClass type = vclassDao.getVClassByURI(typeUri);
                if( ! type.isAnonymous() &&
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
        
    private HashSet<String> getVClassUrisForHits(TopDocs topDocs, 
            IndexSearcher searcherForRequest){
        HashSet<String> typesInHits = new HashSet<String>();        
        for(int i=0; i<topDocs.scoreDocs.length; i++){
            try{
                Document doc=searcherForRequest.doc(topDocs.scoreDocs[i].doc);
                Field[] types = doc.getFields(Entity2LuceneDoc.term.RDFTYPE);
                if(types != null ){
                    for(int j=0;j<types.length;j++){
                        String typeUri = types[j].stringValue();
                        typesInHits.add(typeUri);
                    }
                }
            }catch(Exception e){
                log.error("problems getting rdf:type for search hits",e);
            }
        }
        return typesInHits;
    }
    
    private String getIndexDir(ServletContext servletContext) throws SearchException {
        Object obj = servletContext.getAttribute(LuceneSetup.INDEX_DIR);
        if( obj == null || !(obj instanceof String) )
            throw new SearchException("Could not get IndexDir for luecene index");
        else
            return (String)obj;
    }

    private Analyzer getAnalyzer(ServletContext servletContext) throws SearchException {
        Object obj = servletContext.getAttribute(LuceneSetup.ANALYZER);
        if( obj == null || !(obj instanceof Analyzer) )
            throw new SearchException("Could not get anlyzer");
        else
            return (Analyzer)obj;        
    }

    private Query getQuery(VitroRequest request, PortalFlag portalState,
                       Analyzer analyzer, String indexDir, String querystr ) throws SearchException{
        Query query = null;
        try{
            //String querystr = request.getParameter(VitroQuery.QUERY_PARAMETER_NAME);
            if( querystr == null){
                log.error("There was no Parameter '"+VitroQuery.QUERY_PARAMETER_NAME            
                    +"' in the request.");                
                return null;
            }else if( querystr.length() > MAX_QUERY_LENGTH ){
                log.debug("The search was too long. The maximum " +
                        "query length is " + MAX_QUERY_LENGTH );
                return null;
            }               
            QueryParser parser = getQueryParser(analyzer);
            query = parser.parse(querystr);

            String alpha = request.getParameter("alpha");
            if( alpha != null && !"".equals(alpha) && alpha.length() == 1){
                BooleanQuery boolQuery = new BooleanQuery();
                boolQuery.add( query, BooleanClause.Occur.MUST );
                boolQuery.add( 
                    new WildcardQuery(new Term(Entity2LuceneDoc.term.NAME, alpha+'*')),
                    BooleanClause.Occur.MUST);
                query = boolQuery;
            }
            
            //check if this is classgroup filtered
            Object param = request.getParameter("classgroup");
            if( param != null && !"".equals(param)){                         
                  BooleanQuery boolQuery = new BooleanQuery();
                  boolQuery.add( query, BooleanClause.Occur.MUST);
                  boolQuery.add(  new TermQuery(
                                      new Term(Entity2LuceneDoc.term.CLASSGROUP_URI, 
                                              (String)param)),
                                  BooleanClause.Occur.MUST);
                  query = boolQuery;
            }

            //check if this is rdf:type filtered
            param = request.getParameter("type");
            if(  param != null && !"".equals(param)){                
                BooleanQuery boolQuery = new BooleanQuery();
                boolQuery.add( query, BooleanClause.Occur.MUST);
                boolQuery.add(  new TermQuery(
                                    new Term(Entity2LuceneDoc.term.RDFTYPE, 
                                            (String)param)),
                                BooleanClause.Occur.MUST);
                query = boolQuery;
            }

            //if we have a flag/portal query then we add
            //it by making a BooelanQuery.
            Query flagQuery = makeFlagQuery( portalState );
            if( flagQuery != null ){
                BooleanQuery boolQuery = new BooleanQuery();
                boolQuery.add( query, BooleanClause.Occur.MUST);
                boolQuery.add( flagQuery, BooleanClause.Occur.MUST);
                query = boolQuery;
            }
            
            log.debug("Query: " + query);
            
        }catch (Exception ex){
            throw new SearchException(ex.getMessage());
        }

        return query;
    }
    
    @SuppressWarnings("static-access")
    private QueryParser getQueryParser(Analyzer analyzer){
        //defaultSearchField indicates which field search against when there is no term
        //indicated in the query string.
        //The analyzer is needed so that we use the same analyzer on the search queries as
        //was used on the text that was indexed.
        QueryParser qp = new QueryParser(defaultSearchField,analyzer);
        //this sets the query parser to AND all of the query terms it finds.
        qp.setDefaultOperator(QueryParser.AND_OPERATOR);
        //set up the map of stemmed field names -> unstemmed field names
//        HashMap<String,String> map = new HashMap<String, String>();
//        map.put(Entity2LuceneDoc.term.ALLTEXT,Entity2LuceneDoc.term.ALLTEXTUNSTEMMED);
//        qp.setStemmedToUnstemmed(map);
        return qp;
    }
 
    /**
     * Makes a flag based query clause.  This is where searches can filtered
     * by portal.
     *
     * If you think that search is not working correctly with protals and
     * all that kruft then this is a method you want to look at.
     *
     * It only takes into account "the portal flag" and flag1Exclusive must
     * be set.  Where does that stuff get set?  Look in vitro.flags.PortalFlag
     * 
     * One thing to keep in mind with portal filtering and search is that if
     * you want to search a portal that is different then the portal the user
     * is 'in' then the home parameter should be set to force the user into
     * the new portal.  
     * 
     * Ex.  Bob requests the search page for vivo in portal 3.  You want to
     * have a drop down menu so bob can search the all CALS protal, id 60.
     * You need to have a home=60 on your search form. If you don't set 
     * home=60 with your search query, then the search will not be in the
     * all portal AND the WebappDaoFactory will be filtered to only show 
     * things in portal 3.    
     * 
     * Notice: flag1 as a parameter is ignored. bdc34 2009-05-22.
     */
    @SuppressWarnings("static-access")
    private Query makeFlagQuery( PortalFlag flag){        
        if( flag == null || !flag.isFilteringActive() 
                || flag.getFlag1DisplayStatus() == flag.SHOW_ALL_PORTALS )
            return null;

        // make one term for each bit in the numeric flag that is set
        Collection<TermQuery> terms = new LinkedList<TermQuery>();
        int portalNumericId = flag.getFlag1Numeric();        
        Long[] bits = FlagMathUtils.numeric2numerics(portalNumericId);
        for (Long bit : bits) {
            terms.add(new TermQuery(new Term(Entity2LuceneDoc.term.PORTAL, Long
                    .toString(bit))));
        }

        // make a boolean OR query for all of those terms
        BooleanQuery boolQuery = new BooleanQuery();
        if (terms.size() > 0) {
            for (TermQuery term : terms) {
                    boolQuery.add(term, BooleanClause.Occur.SHOULD);
            }
            return boolQuery;
        } else {
            //we have no flags set, so no flag filtering
            return null;
        }
    }

    private synchronized IndexSearcher getIndexSearcher(String indexDir) {
        if( searcher == null ){
            try {                
                Directory fsDir = FSDirectory.getDirectory(indexDir);
                searcher = new IndexSearcher(fsDir);
            } catch (IOException e) {
                log.error("LuceneSearcher: could not make indexSearcher "+e);
                log.error("It is likely that you have not made a directory for the lucene index.  "+
                          "Create the directory indicated in the error and set permissions/ownership so"+
                          " that the tomcat server can read/write to it.");
                //The index directory is created by LuceneIndexer.makeNewIndex()
            }
        }
        return searcher;
    }
    
    private List<Individual> highlightBeans(List<Individual> beans, 
            DataPropertyDao dpDao, ObjectPropertyDao opDao, VitroHighlighter highlighter) {
        if( beans == null ){
            log.debug("List of beans passed to highlightBeans() was null");
            return Collections.EMPTY_LIST;
        }else if( highlighter == null ){
            log.debug("Null highlighter passed to highlightBeans()");
            return beans;
        }            
        Iterator<Individual> it = beans.iterator();
        while(it.hasNext()){
            Individual ent = it.next();
            try{
                dpDao.fillDataPropertiesForIndividual(ent);
                opDao.fillObjectPropertiesForIndividual(ent);
                fragmentHighlight(ent, highlighter);
            }catch( Exception ex ){
                log.debug("Error while doing search highlighting" , ex);
            }            
        }
        return beans;
    }  
    
    /**
     * Highlights the name and then replaces the description with
     * highlighted fragments.
     * @param ent
     * @param highlighter 
     */
    public void fragmentHighlight(Individual ent, VitroHighlighter hl){
        try{
            if( ent == null ) return;

            Html2Text h2t = new Html2Text();        
            StringBuffer sb = new StringBuffer("");
            if(ent.getBlurb() != null)
                sb.append(ent.getBlurb()).append(' ');

            if(ent.getDescription() != null )
                sb.append(ent.getDescription()).append(' ');

            if(ent.getDataPropertyList() != null) {
                Iterator edIt = ent.getDataPropertyList().iterator();
                while (edIt.hasNext()) {
                    try{
                    DataProperty dp = (DataProperty)edIt.next();                    
                    if( getDataPropertyBlacklist().contains(dp.getURI()))
                        continue;
                    for(DataPropertyStatement dps : dp.getDataPropertyStatements()){
                        sb.append(dp.getPublicName()).append(' ')
                          .append(dps.getData()).append(' ');    
                    }    
                    }catch(Throwable e){
                        log.debug("Error highlighting data property statment " +
                                "for individual "+ent.getURI());
                    }
                }
            }

            if(ent.getObjectPropertyList() != null) {
                Iterator edIt = ent.getObjectPropertyList().iterator();
                String t = null;
                while (edIt.hasNext()) {
                    try {                
                        ObjectProperty op = (ObjectProperty)edIt.next();
                        if( getObjectPropertyBlacklist().contains(op.getURI()))
                            continue;
                        for( ObjectPropertyStatement stmt : op.getObjectPropertyStatements()){                                            
                            sb.append( ( (t = op.getDomainPublic()) != null) ? t : "" );
                            sb.append(' ');
                            sb.append( ( (t = stmt.getObject().getName()) != null) ? t : "" );
                            sb.append(' ');
                        }
                    } catch (Throwable e) {
                        log.debug("Error highlighting object property " +
                                "statement for individual "+ent.getURI());
                    }
                }
            }

            String keywords = ent.getKeywordString();
            if( keywords != null )
                sb.append(keywords);

            ent.setDescription(hl.getHighlightFragments(  h2t.stripHtml( sb.toString() )));
        }catch(Throwable th){
            log.debug("could not hightlight for entity " + ent.getURI(),th);
        }
    }        

    private String doSearchError(String message, Configuration config) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("message", "Search failed: " + message);
        return mergeBodyToTemplate("searchError.ftl", body, config);
    }
    
    private String doNoQuery(Configuration config, Portal portal) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("title", "Search " + portal.getAppName());
        body.put("message", "No query entered.");
        return mergeBodyToTemplate("searchError.ftl", body, config);
    }
    
    private String doFailedSearch(String message, String querytext, Configuration config) {
        Map<String, Object> body = new HashMap<String, Object>();       
        body.put("title", "Search for '" + querytext + "'");        
        if ( StringUtils.isEmpty(message) ) {
            message = "Search failed.";
        }        
        body.put("message", message);
        return mergeBodyToTemplate("searchError.ftl", body, config);
    }

    private String doNoHits(String querytext, Configuration config) {
        Map<String, Object> body = new HashMap<String, Object>();       
        body.put("title", "Search for '" + querytext + "'");        
        body.put("message", "No matching results.");     
        return mergeBodyToTemplate("searchError.ftl", body, config);
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
    
    @SuppressWarnings("unchecked")
    private HashSet<String> getDataPropertyBlacklist(){
        HashSet<String>dpBlacklist = (HashSet<String>)
        getServletContext().getAttribute(LuceneSetup.SEARCH_DATAPROPERTY_BLACKLIST);
        return dpBlacklist;        
    }
    
    @SuppressWarnings("unchecked")
    private HashSet<String> getObjectPropertyBlacklist(){
        HashSet<String>opBlacklist = (HashSet<String>)
        getServletContext().getAttribute(LuceneSetup.SEARCH_OBJECTPROPERTY_BLACKLIST);
        return opBlacklist;        
    }
    
    
    private final String defaultSearchField = "ALLTEXT";
    public static final int MAX_QUERY_LENGTH = 500;

    
    /**
     * Need to accept notification from indexer that the index has been changed.
     */
    public void close() {
        searcher = null;        
    }

    public VitroHighlighter getHighlighter(VitroQuery q) {
        throw new Error("PagedSearchController.getHighlighter() is unimplemented");
    }

    public VitroQueryFactory getQueryFactory() {
        throw new Error("PagedSearchController.getQueryFactory() is unimplemented");
    }

    public List search(VitroQuery query) throws SearchException {
        throw new Error("PagedSearchController.search() is unimplemented");
    }

}
