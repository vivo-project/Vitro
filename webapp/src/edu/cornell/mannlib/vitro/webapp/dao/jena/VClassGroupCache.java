/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.WebappDaoFactoryFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilterUtils;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.search.solr.SolrSetup;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;

/**
 * This is a cache of classgroups with classes.  Each class should have a count
 * of individuals. These counts are cached so they don't have to be recomputed. 
 * 
 * As of VIVO release 1.4, the counts come from the solr index.  Before that they
 * came from the DAOs.  
 */
public class VClassGroupCache {
    private static final Log log = LogFactory.getLog(VClassGroupCache.class);

    private static final String ATTRIBUTE_NAME = "VClassGroupCache";

    private static final boolean ORDER_BY_DISPLAYRANK = true;
    private static final boolean INCLUDE_UNINSTANTIATED = true;
    private static final boolean INCLUDE_INDIVIDUAL_COUNT = true;

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
        if (_groupList == null){
            log.error("VClassGroup cache has not been created");
            requestCacheUpdate();
            return Collections.emptyList();
        }else{
            return _groupList;
        }
    }

    // Get specific VClass corresponding to Map
    public synchronized VClass getCachedVClass(String classUri) {
        if( VclassMap != null){
            if (VclassMap.containsKey(classUri)) {
                return VclassMap.get(classUri);
            }
            return null;
        }else{
            log.error("VClassGroup cache has not been created");
            requestCacheUpdate();
            return null;
        }
    }

    protected synchronized void setCache(List<VClassGroup> newGroups, Map<String,VClass> classMap){
        _groupList = newGroups;
        VclassMap = classMap;
    }
    
    public void requestCacheUpdate() {
        log.debug("requesting update");        
        _cacheRebuildThread.informOfQueueChange();
    }
    
    protected void requestStop() {
        if (_cacheRebuildThread != null) {
            _cacheRebuildThread.kill();
            try {
                _cacheRebuildThread.join();
            } catch (InterruptedException e) {
                log.warn("Waiting for the thread to die, but interrupted.", e);
            }
        }
    }

    protected VClassGroupDao getVCGDao() {
        WebappDaoFactory wdf = (WebappDaoFactory) context.getAttribute("webappDaoFactory");
        if (wdf == null) {
            log.error("Cannot get webappDaoFactory from context");
            return null;
        } else
            return wdf.getVClassGroupDao();
    }
    
//    protected void doSynchronousRebuild(){
//        _cacheRebuildThread.rebuildCacheUsingSolr(this);        
//    }
    
    /* **************** static utility methods ***************** */
    
    protected static List<VClassGroup> getGroups(VClassGroupDao vcgDao,
            boolean includeIndividualCount) {
        // Get all classgroups, each populated with a list of their member vclasses
        List<VClassGroup> groups = vcgDao.getPublicGroupsWithVClasses(
                ORDER_BY_DISPLAYRANK, INCLUDE_UNINSTANTIATED,
                includeIndividualCount);

        // remove classes that have been configured to be hidden from search results
        vcgDao.removeClassesHiddenFromSearch(groups);

        return groups;
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
    
    protected static List<VClassGroup> removeFilteredOutGroupsAndClasses(
            List<VClassGroup> unfilteredGroups, List<VClassGroup> filteredGroups) {
        List<VClassGroup> groups = new ArrayList<VClassGroup>();
        Set<String> allowedGroups = new HashSet<String>();
        Set<String> allowedVClasses = new HashSet<String>();
        for (VClassGroup group : filteredGroups) {
            if (group.getURI() != null) {
                allowedGroups.add(group.getURI());
            }
            for (VClass vcl : group) {
                if (vcl.getURI() != null) {
                    allowedVClasses.add(vcl.getURI());
                }
            }
        }
        for (VClassGroup group : unfilteredGroups) {
            if (allowedGroups.contains(group.getURI())) {
                groups.add(group);
            }
            List<VClass> tmp = new ArrayList<VClass>();
            for (VClass vcl : group) {
                if (allowedVClasses.contains(vcl.getURI())) {
                    tmp.add(vcl);
                }
            }
            group.setVitroClassList(tmp);
        }
        return groups;
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
        
    /**
     * Removes classes from groups that are prohibited from search. 
     */
    protected static void removeClassesHiddenFromSearch(List<VClassGroup> groups,ProhibitedFromSearch pfs) {         
        for (VClassGroup group : groups) {
            List<VClass> classList = new ArrayList<VClass>();
            for (VClass vclass : group.getVitroClassList()) {
                if (!pfs.isClassProhibitedFromSearch(vclass.getURI())) {
                    classList.add(vclass);
                }
            }
            group.setVitroClassList(classList);
        }        
    }
    
    
    /**
     * Add the Individual count to classes in groups.
     * @throws SolrServerException 
     */
    private void addCountsUsingSolr(List<VClassGroup> groups, SolrServer solrServer) throws SolrServerException {
        
        if( groups == null || solrServer == null ) 
            return;
        
        for( VClassGroup group : groups){            
            addClassCountsToGroup(group, solrServer);            
        }
    }
    
    
    private void addClassCountsToGroup(VClassGroup group, SolrServer solrServer) throws SolrServerException {
        if( group == null ) return;
        
        String groupUri = group.getURI();
        
        SolrQuery query = new SolrQuery( ).
            setRows(0).
            //make a query for the  group URI in the solr classgroup field
            setQuery(VitroSearchTermNames.CLASSGROUP_URI + ":" + groupUri ).        
            //facet on type to get counts for classes in classgroup
            setFacet(true).
            addFacetField( VitroSearchTermNames.RDFTYPE ).
            setFacetMinCount(0);
        
        log.debug("query: " + query);
        
        QueryResponse rsp = solrServer.query(query);
        
        //get counts for classes
        group.setIndividualCount(0);
        FacetField ff = rsp.getFacetField( VitroSearchTermNames.RDFTYPE );
        List<Count> counts = ff.getValues();        
        for( Count ct: counts){                    
            String classUri = ct.getName();
            long individualsInClass = ct.getCount();            
            setClassCount( group, classUri, individualsInClass);            
        }
    }

    private void setClassCount(VClassGroup group, String classUri,
            long individualsInClass) {
        for( VClass clz : group){
            if( clz.getURI().equals(classUri)){
                clz.setEntityCount( (int) individualsInClass );
                group.setIndividualCount( ((int)individualsInClass + group.getIndividualCount()));
            }
        }
        
    }


    /* ******************** RebuildGroupCacheThread **************** */
    
    protected class RebuildGroupCacheThread extends VitroBackgroundThread {
        private final VClassGroupCache cache;
        private long queueChangeMillis = 0L;
        private long timeToBuildLastCache = 100L; //in msec 
        private boolean rebuildRequested = false;
        private volatile boolean die = false;

        RebuildGroupCacheThread(VClassGroupCache cache) {
            super("VClassGroupCache.RebuildGroupCacheThread");
            this.cache = cache;
        }

        public void run() {
            while (!die) {
                int delay;

                if ( !rebuildRequested ) {
                    log.debug("rebuildGroupCacheThread.run() -- queue empty, sleep");
                    delay = 1000 * 60;
                } else if ((System.currentTimeMillis() - queueChangeMillis ) < 500) {
                    log.debug("rebuildGroupCacheThread.run() -- delay start of rebuild");
                    delay = 500;
                } else {
                    log.debug("rebuildGroupCacheThread.run() -- starting rebuildCache()");                    
                    long start = System.currentTimeMillis();
                    
                    setWorkLevel(WorkLevel.WORKING);
                    rebuildRequested = false;                    
                    try {
                        rebuildCacheUsingSolr( cache );
                        timeToBuildLastCache = System.currentTimeMillis() - start;
                        log.debug("rebuildGroupCacheThread.run() -- rebuilt cache in " 
                                + timeToBuildLastCache + " msec");
                        delay = 0;
                    } catch (SolrServerException e) {
                        //wait a couple seconds and try again.
                        log.error("Will attempt to rebuild cache once solr comes up.");
                        rebuildRequested = true;
                        delay = 1000;                        
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

        protected void rebuildCacheUsingSolr(VClassGroupCache cache ) throws SolrServerException{                        
            long start = System.currentTimeMillis();
            WebappDaoFactory wdFactory = (WebappDaoFactory) cache.context.getAttribute("webappDaoFactory");
            if (wdFactory == null) 
                log.error("Unable to rebuild cache: could not get 'webappDaoFactory' from Servletcontext");                
            
            SolrServer solrServer = (SolrServer)cache.context.getAttribute(SolrSetup.SOLR_SERVER);
            if( solrServer == null)
                log.error("Unable to rebuild cache: could not get solrServer from ServletContext");              
            
            ProhibitedFromSearch pfs = (ProhibitedFromSearch)cache.context.getAttribute(SolrSetup.PROHIBITED_FROM_SEARCH);
            if(pfs==null)
                log.error("Unable to rebuild cache: could not get ProhibitedFromSearch from ServletContext");
            
            VitroFilters vFilters = VitroFilterUtils.getPublicFilter(context);
            WebappDaoFactory filteringDaoFactory = new WebappDaoFactoryFiltering(wdFactory, vFilters);
            
            List<VClassGroup> groups = getGroups( 
                    filteringDaoFactory.getVClassGroupDao(), !INCLUDE_INDIVIDUAL_COUNT);
            
            // Remove classes that have been configured to be hidden from search results.                
            removeClassesHiddenFromSearch(groups,pfs);

            addCountsUsingSolr(groups, solrServer);
            
            cache.setCache(groups, classMapForGroups(groups));
                log.debug("msec to build cache: " + (System.currentTimeMillis() - start));
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
    protected class VClassGroupCacheChangeListener extends StatementListener {        
        public void addedStatement(Statement stmt) {
            checkAndDoUpdate(stmt);
        }

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
            if (RDF.type.getURI().equals(stmt.getPredicate().getURI())) {
                requestCacheUpdate();
            } else if (VitroVocabulary.IN_CLASSGROUP.equals(stmt.getPredicate().getURI())) {
                requestCacheUpdate();
            } else if(VitroVocabulary.DISPLAY_RANK.equals(stmt.getPredicate().getURI())){
            	requestCacheUpdate();
            } else {
                OntModel jenaOntModel = ModelContext.getJenaOntModel(context);
                if( isClassNameChange(stmt, jenaOntModel) ) {            
                    requestCacheUpdate();
                }
            }
        }       
    }
    
    /* ******************** ServletContextListener **************** */
    public static class Setup implements ServletContextListener {
        @Override
        public void contextInitialized(ServletContextEvent sce) {            
            ServletContext servletContext = sce.getServletContext();
            VClassGroupCache vcgc = new VClassGroupCache(servletContext);
            vcgc.requestCacheUpdate();
            servletContext.setAttribute(ATTRIBUTE_NAME,vcgc);           
            log.info("VClassGroupCache added to context");            
        }

        @Override
        public void contextDestroyed(ServletContextEvent sce) {
            Object o = sce.getServletContext().getAttribute(ATTRIBUTE_NAME);
            if (o instanceof VClassGroupCache) {
                ((VClassGroupCache) o).requestStop();
            }
        }
    }

    
    /**
     * Use getVClassGroupCache(ServletContext) to get a VClassGroupCache.
     */
    public static VClassGroupCache getVClassGroupCache(ServletContext sc) {
        return (VClassGroupCache) sc.getAttribute(ATTRIBUTE_NAME);
    }

}
