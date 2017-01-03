/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.listeners.StatementListener;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.Lock;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupsForRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.WebappDaoFactoryFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilterUtils;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.BulkUpdateEvent;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchFacetField;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchFacetField.Count;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;

/**
 * This is a cache of classgroups with classes.  Each class should have a count
 * of individuals. These counts are cached so they don't have to be recomputed. 
 * 
 * The cache is updated asynchronously by the thread RebuildGroupCacheThread. 
 * A synchronous rebuild can be performed with VClassGroupCache.doSynchronousRebuild() 
 * 
 * This class should handle the condition where the search engine is not available.  
 * It will not throw an exception but it will return a empty list of classes
 * or class groups.
 * 
 * VClassGroupCache.doSynchronousRebuild() and the RebuildGroupCacheThread will try
 * to connect to the search engine a couple of times and then give up.  
 * 
 * As of VIVO release 1.4, the counts come from the search index. If the
 * search index is not built or if there were problems building the index,
 * the class counts from VClassGroupCache will be incorrect.
 */
public class VClassGroupCache implements SearchIndexer.Listener {
    private static final Log log = LogFactory.getLog(VClassGroupCache.class);

    private static final String ATTRIBUTE_NAME = "VClassGroupCache";

    private static final boolean ORDER_BY_DISPLAYRANK = true;
    private static final boolean INCLUDE_UNINSTANTIATED = true;
    private static final boolean DONT_INCLUDE_INDIVIDUAL_COUNT = false;

    /**
     * This is the cache of VClassGroups. It is a list of VClassGroups. If this
     * is null then the cache is not built.
     */
    private List<VClassGroup> _groupList;

    /**
     * Also keep track of the classes here, makes it easier to get the counts
     * and other information */
    private Map<String, VClass> VclassMap = new HashMap<String, VClass>();

    
    private final RebuildGroupCacheThread _cacheRebuildThread;
    
    /**
     * Need a pointer to the context to get DAOs and models.
     */
    private final ServletContext context;


    private VClassGroupCache(ServletContext context) {
        this.context = context;
        this._groupList = null;

        if (StartupStatus.getBean(context).isStartupAborted()) {
            _cacheRebuildThread = null;
            return;
        }

        /* Need to register for changes of rdf:type for individuals in abox 
         * and for changes of classgroups for classes. */         
        VClassGroupCacheChangeListener bccl = new VClassGroupCacheChangeListener();
        ModelContext.registerListenerForChanges(context, bccl);

        _cacheRebuildThread = new RebuildGroupCacheThread(this);
        _cacheRebuildThread.setDaemon(true);        
        _cacheRebuildThread.start();        
    }

    public synchronized VClassGroup getGroup(String vClassGroupURI) {        
        if (vClassGroupURI == null || vClassGroupURI.isEmpty())
            return null;                
        List<VClassGroup> cgList = getGroups();
        for (VClassGroup cg : cgList) {
            if (vClassGroupURI.equals(cg.getURI()))
                return cg;
        }
        return null;
    }

    public synchronized List<VClassGroup> getGroups() {
        //try to build the cache if it doesn't exist
        if (_groupList == null){
            doSynchronousRebuild();
        }
        
        if (_groupList == null){
            requestCacheUpdate();
            return Collections.emptyList();
        }else{
            return _groupList;
        }
    }

    // Get specific VClass corresponding to Map
    public synchronized VClass getCachedVClass(String classUri) {
        //try to build the cache if it doesn't exist
        if ( VclassMap == null ){
            doSynchronousRebuild();
        }
        
        if( VclassMap == null){
            requestCacheUpdate();
            return null;
        }else{
            if (VclassMap.containsKey(classUri)) {
                return VclassMap.get(classUri);
            }else{ 
                return null; 
            }
        }    
    }

    protected synchronized void setCache(List<VClassGroup> newGroups, Map<String,VClass> classMap){
        _groupList = newGroups;
        VclassMap = classMap;
    }
    
    private boolean paused = false;
    private boolean updateRequested = false;
    
    public void pause() {
        this.paused = true;
    }
    
    public void unpause() {
        this.paused = false;
        if(updateRequested) {
            updateRequested = false;
            requestCacheUpdate();
        }
    }
    
    public void requestCacheUpdate() {
        log.debug("requesting update");
        if(paused) {
            updateRequested = true;
        } else {
            _cacheRebuildThread.informOfQueueChange();
        }
    }
    
    protected void requestStop() {
        if (_cacheRebuildThread != null) {
            _cacheRebuildThread.kill();
            try {
                _cacheRebuildThread.join();
            } catch (InterruptedException e) {
                //don't log message since shutting down
            }
        }
    }

    protected VClassGroupDao getVCGDao() {
		return ModelAccess.on(context).getWebappDaoFactory().getVClassGroupDao();
    }
    
    public void doSynchronousRebuild(){
        //try to rebuild a couple times since the search engine may not yet be up.
        
        int attempts = 0;
        int maxTries = 5;
        SearchEngineException exception = null;
        
        while( attempts < maxTries ){
            try {
                attempts++;
                rebuildCacheUsingSearch(this);
                break;                
            } catch (SearchEngineException e) {
                exception = e;
                try { Thread.sleep(1000); }
                catch (InterruptedException e1) {/*ignore interrupt*/}
            }
        }
        
        if( exception != null )
            log.error("Could not rebuild cache. " + exception.getCause().getMessage() );
    }
    
    /**
     * Handle notification of events from the IndexBuilder.
     */
    @Override
	public void receiveSearchIndexerEvent(Event event) {
    	switch (event.getType()) {
    	case STOP_URIS:
            log.debug("rebuilding because of IndexBuilder " + event.getType());
            requestCacheUpdate();
            break;            
        default: 
            log.debug("ignoring event type " + event.getType());
            break;
    	}
	}
    
    /* **************** static utility methods ***************** */
    
    /**
     * Use getVClassGroupCache(ServletContext) to get a VClassGroupCache.
     */
    public static VClassGroupCache getVClassGroupCache(ServletContext sc) {
        return (VClassGroupCache) sc.getAttribute(ATTRIBUTE_NAME);
    }

	/**
	 * Use getVClassGroups(HttpServletRequest) to get a language-aware image of
	 * the cached groups and classes.
	 */
    public static VClassGroupsForRequest getVClassGroups(HttpServletRequest req) {
    	return new VClassGroupsForRequest(req, getVClassGroupCache(req.getSession().getServletContext()));
    }
    
    /**
     * Method that rebuilds the cache. This will use a WebappDaoFactory and a SearchEngine.
     */
    protected static void rebuildCacheUsingSearch( VClassGroupCache cache ) throws SearchEngineException{                        
        long start = System.currentTimeMillis();
		WebappDaoFactory wdFactory = ModelAccess.on(cache.context).getWebappDaoFactory();

		SearchEngine searchEngine = ApplicationUtils.instance().getSearchEngine();
        
        VitroFilters vFilters = VitroFilterUtils.getPublicFilter(cache.context);
        VClassGroupDao vcgDao = new WebappDaoFactoryFiltering(wdFactory, vFilters).getVClassGroupDao();
        
        List<VClassGroup> groups = vcgDao.getPublicGroupsWithVClasses(ORDER_BY_DISPLAYRANK, 
                INCLUDE_UNINSTANTIATED, DONT_INCLUDE_INDIVIDUAL_COUNT);
                                
        addCountsUsingSearch(groups, searchEngine);        
        cache.setCache(groups, classMapForGroups(groups));
        
        log.debug("msec to build cache: " + (System.currentTimeMillis() - start));
    }

    protected static Map<String,VClass> classMapForGroups( List<VClassGroup> groups){
        Map<String,VClass> newClassMap = new HashMap<String,VClass>();
        for (VClassGroup vcg : groups) {
            List<VClass> vclasses = vcg.getVitroClassList();
            for (VClass vclass : vclasses) {
                String classUri = vclass.getURI();
                if (!newClassMap.containsKey(classUri)) {
                    newClassMap.put(classUri, vclass);
                }
            }
        }
        return newClassMap;
    }      

    
    /**
     * Add the Individual count to classes in groups.
     * @throws SearchEngineException 
     */
    protected static void addCountsUsingSearch(List<VClassGroup> groups, SearchEngine searchEngine) 
    throws SearchEngineException {        
        if( groups == null || searchEngine == null ) 
            return;       
        for( VClassGroup group : groups){            
            addClassCountsToGroup(group, searchEngine);            
        }
    }    
    
    protected static void addClassCountsToGroup(VClassGroup group, SearchEngine searchEngine)
    throws SearchEngineException {
        if( group == null ) return;
        
        String groupUri = group.getURI();
        String facetOnField = VitroSearchTermNames.RDFTYPE;
        
        SearchQuery query = searchEngine.createQuery().
            setRows(0).
            setQuery(VitroSearchTermNames.CLASSGROUP_URI + ":" + groupUri ).        
            addFacetFields( facetOnField ). //facet on type to get counts for classes in classgroup
            setFacetMinCount(0);
        
        log.debug("query: " + query);
        
        SearchResponse rsp = searchEngine.query(query);

        //Get individual count
        long individualCount = rsp.getResults().getNumFound();
        log.debug("Number of individuals found " + individualCount);
        group.setIndividualCount((int) individualCount);
        
        //get counts for classes
        SearchFacetField ff = rsp.getFacetField( facetOnField );
        if( ff != null ){
            List<Count> counts = ff.getValues();
            if( counts != null ){
                for( Count ct: counts){
                    if( ct != null ){
                        String classUri = ct.getName();
                        long individualsInClass = ct.getCount();            
                        setClassCount( group, classUri, individualsInClass);
                    }
                }
            }else{
               log.debug("no Counts found for FacetField " + facetOnField);   
            }            
        }else{
            log.debug("no FaccetField found for " + facetOnField);
        }
    }

    protected static void setClassCount(VClassGroup group, String classUri,
            long individualsInClass) {
        for( VClass clz : group){
            if( clz.getURI().equals(classUri)){
                clz.setEntityCount( (int) individualsInClass );
            }
        }        
    }

    protected static boolean isClassNameChange(Statement stmt, OntModel jenaOntModel) {
        // Check if the stmt is a rdfs:label change and that the
        // subject is an owl:Class.
        if( RDFS.label.equals( stmt.getPredicate() )) {                
            jenaOntModel.enterCriticalSection(Lock.READ);
            try{                                                  
                return jenaOntModel.contains(
                        ResourceFactory.createStatement(
                                ResourceFactory.createResource(stmt.getSubject().getURI()), 
                                RDF.type, 
                                OWL.Class));
            }finally{
                jenaOntModel.leaveCriticalSection();
            }
        }else{
            return false;
        }
    }
    /* ******************** RebuildGroupCacheThread **************** */
    
    protected class RebuildGroupCacheThread extends VitroBackgroundThread {
        private final VClassGroupCache cache;
        private long queueChangeMillis = 0L; 
        private boolean rebuildRequested = false;
        private volatile boolean die = false;
        private int failedAttempts = 0;
        private final int maxFailedAttempts = 5;
        
        RebuildGroupCacheThread(VClassGroupCache cache) {
            super("VClassGroupCache.RebuildGroupCacheThread");
            this.cache = cache;
        }

        @Override
		public void run() {
            while (!die) {
                int delay;

                if ( !rebuildRequested ) {
                    log.debug("rebuildGroupCacheThread.run() -- nothing to do, sleep");
                    delay = 1000 * 60;
                } else if ((System.currentTimeMillis() - queueChangeMillis ) < 500) {
                    log.debug("rebuildGroupCacheThread.run() -- delay start of rebuild");
                    delay = 500;
                } else {                                        
                    setWorkLevel(WorkLevel.WORKING);
                    rebuildRequested = false;                    
                    try {
                        rebuildCacheUsingSearch( cache );                        
                        log.debug("rebuildGroupCacheThread.run() -- rebuilt cache ");
                        failedAttempts = 0;
                        delay = 100;
                    } catch (SearchEngineException e) {                        
                        failedAttempts++;
                        if( failedAttempts >= maxFailedAttempts ){                                                        
                            log.error("Could not build VClassGroupCache. " +
                            		  "Could not connect with the search engine after " + 
                            		   failedAttempts + " attempts.", e.getCause());
                            rebuildRequested = false;
                            failedAttempts = 0;
                            delay = 1000;
                        }else{
                            rebuildRequested = true;
                            delay = (int) (( Math.pow(2, failedAttempts) ) * 1000);
                            log.debug("Could not connect with the search engine, will attempt " +
                            		  "again in " + delay + " msec.");                            
                        }
                    }catch(Exception ex){
                        log.error("could not build cache",ex);
                        delay = 1000;
                    }
                    setWorkLevel(WorkLevel.IDLE);                    
                }

                if (delay > 0) {
                    synchronized (this) {
                        try {
                            wait(delay);
                        } catch (InterruptedException e) {
                            log.warn("Waiting " + delay
                                    + " milliseconds, but interrupted.", e);
                        }
                    }
                }
            }
            log.debug("rebuildGroupCacheThread.run() -- die()");
        }        

        synchronized void informOfQueueChange() {
            queueChangeMillis = System.currentTimeMillis();
            rebuildRequested = true;
            this.notifyAll();
        }

        synchronized void kill() {
            die = true;
            this.notifyAll();
        }
    }        

    /* ****************** Jena Model Change Listener***************************** */
    
    /**
     * Listen for changes to what class group classes are in and their display rank.
     */
    protected class VClassGroupCacheChangeListener extends StatementListener {        
        @Override
		public void addedStatement(Statement stmt) {
            checkAndDoUpdate(stmt);
        }

        @Override
		public void removedStatement(Statement stmt) {
            checkAndDoUpdate(stmt);
        }

        protected void checkAndDoUpdate(Statement stmt) {
            if (stmt == null)
                return;
            if (log.isDebugEnabled()) {
                log.debug("subject: " + stmt.getSubject().getURI());
                log.debug("predicate: " + stmt.getPredicate().getURI());
            }
            if (RDF.type.equals(stmt.getPredicate())) {
                requestCacheUpdate();
            } else if (VitroVocabulary.IN_CLASSGROUP.equals(stmt.getPredicate().getURI())) {
                requestCacheUpdate();
            } else if(VitroVocabulary.DISPLAY_RANK.equals(stmt.getPredicate().getURI())){
            	requestCacheUpdate();
            } else if (RDFS.label.equals(stmt.getPredicate())){
                OntModel jenaOntModel = ModelAccess.on(context).getOntModelSelector().getTBoxModel();
                if( isClassNameChange(stmt, jenaOntModel) ) {            
                    requestCacheUpdate();
                }
            }
        }       
        
        public void notifyEvent(Model model, Object event) {
            if (event instanceof BulkUpdateEvent) { 
                if(((BulkUpdateEvent) event).getBegin()) {
                    pause();
                } else {
                    unpause();
                }
            }
        }
       
    }
    
    /* ******************** ServletContextListener **************** */
    public static class Setup implements ServletContextListener {
        @Override
        public void contextInitialized(ServletContextEvent sce) {            
            ServletContext context = sce.getServletContext();
            VClassGroupCache vcgc = new VClassGroupCache(context);
            vcgc.requestCacheUpdate();
            context.setAttribute(ATTRIBUTE_NAME,vcgc);           
            log.info("VClassGroupCache added to context");   
            
            SearchIndexer searchIndexer = ApplicationUtils.instance().getSearchIndexer();
            searchIndexer.addListener(vcgc);
            log.info("VClassGroupCache set to listen to events from IndexBuilder");
        }

        @Override
        public void contextDestroyed(ServletContextEvent sce) {
            Object o = sce.getServletContext().getAttribute(ATTRIBUTE_NAME);
            if (o instanceof VClassGroupCache) {
                ((VClassGroupCache) o).requestStop();
            }
        }
    }

  

}
