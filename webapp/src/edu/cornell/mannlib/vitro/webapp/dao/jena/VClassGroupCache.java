/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.WebappDaoFactoryFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilterUtils;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.AbortStartup;

public class VClassGroupCache{
    private static final Log log = LogFactory.getLog(VClassGroupCache.class);
    
	private static final String ATTRIBUTE_NAME = "VClassGroupCache";

    private static final boolean ORDER_BY_DISPLAYRANK = true;
    private static final boolean INCLUDE_UNINSTANTIATED = true;
    private static final boolean INCLUDE_INDIVIDUAL_COUNT = true;

    /** 
     * Use getVClassGroupCache(ServletContext) to get a VClassGroupCache.
     */
    public static VClassGroupCache getVClassGroupCache(ServletContext sc){
        return (VClassGroupCache) sc.getAttribute(ATTRIBUTE_NAME);
    }
    
    /** Indicates that a rebuild of the VCLassGroupCache is needed. */
    private Boolean _rebuildRequested;
    
	/** This is the cache of VClassGroups.  It is a list of VClassGroups.  
	 * If this is null then the cache is not built. */
    private List<VClassGroup> _groupList;
                   
    private final RebuildGroupCacheThread _cacheRebuildThread;    
    private final ServletContext context;
    
    
    private VClassGroupCache(ServletContext context) {
        this.context = context;
        this._groupList = null;
       
        if( AbortStartup.isStartupAborted(context)){
            _cacheRebuildThread = null;
            return;
        }
        
        VClassGroupCacheChangeListener bccl = new VClassGroupCacheChangeListener();
        ModelContext.registerListenerForChanges(context, bccl);
//        
//        ModelContext.getJenaOntModel(context).register(bccl);
//        ModelContext.getBaseOntModel(context).register(bccl);
//        ModelContext.getInferenceOntModel(context).register(bccl);
//        ModelContext.getUnionOntModelSelector(context).getABoxModel().register(bccl);
//        ModelContext.getBaseOntModelSelector(context).getABoxModel().register(bccl);
//        ModelContext.getInferenceOntModelSelector(context).getABoxModel().register(bccl);
       
        _rebuildRequested = true;
        _cacheRebuildThread = new RebuildGroupCacheThread(this);
        _cacheRebuildThread.setDaemon(true);
        _cacheRebuildThread.start();
        _cacheRebuildThread.informOfQueueChange();       
    }
     
    public void clearGroupCache(){        
        _groupList.clear();
    }   
    
    public VClassGroup getGroup(  String vClassGroupURI ){
        if( vClassGroupURI == null || vClassGroupURI.isEmpty() )
            return null;
        List<VClassGroup> cgList = getGroups();
        for( VClassGroup cg : cgList ){
            if( vClassGroupURI.equals( cg.getURI()))
                return cg;
        }
        return null;
    }
    
    /**
     * @deprecated use getGroups() instead.
     */
    public List<VClassGroup> getGroups( int portalId ){
        //bdc34: this has been de-portaled.
        if( _groupList == null )
            return rebuildCache();
        else
            return _groupList;
    }  
    
    
    public List<VClassGroup> getGroups( ){     
        if( _groupList == null )
            return rebuildCache();
        else
            return _groupList;
    }     
        
    private synchronized void requestCacheUpdate(){
        log.debug("requesting update");
        _rebuildRequested = true;                
        _cacheRebuildThread.informOfQueueChange();
    }    

    protected synchronized List<VClassGroup> rebuildCache(){
        long start = System.currentTimeMillis();
        try{
            WebappDaoFactory wdFactory = (WebappDaoFactory)context.getAttribute("webappDaoFactory");
            if( wdFactory == null ){
                log.error("Unable to rebuild cache: could not get 'webappDaoFactory' from Servletcontext");
                return Collections.emptyList();
            }
            
            VitroFilters vFilters = VitroFilterUtils.getPublicFilter(context);
            WebappDaoFactory filteringDaoFactory = new WebappDaoFactoryFiltering(wdFactory,vFilters);
        
            // BJL23:  You may be wondering, why this extra method?  
            // Can't we just use the filtering DAO?
            // Yes, but using the filtered DAO involves an expensive method
            // called correctVClassCounts() that requires each individual
            // in a VClass to be retrieved and filtered.  This is fine in memory,
            // but awful when using a database.  We can't (yet) avoid all
            // this work when portal filtering is involved, but we can
            // short-circuit it when we have a single portal by using
            // the filtering DAO only to filter groups and classes,
            // and the unfiltered DAO to get the counts.        
            List<VClassGroup> unfilteredGroups = getGroups(wdFactory.getVClassGroupDao(), INCLUDE_INDIVIDUAL_COUNT);
            List<VClassGroup> filteredGroups = getGroups(filteringDaoFactory.getVClassGroupDao(), !INCLUDE_INDIVIDUAL_COUNT);                                    
            List<VClassGroup> groups = removeFilteredOutGroupsAndClasses(unfilteredGroups, filteredGroups);
            
            // Remove classes that have been configured to be hidden from search results.
            filteringDaoFactory.getVClassGroupDao().removeClassesHiddenFromSearch(groups);
            
            _groupList = groups;
            _rebuildRequested = false;
            
            log.info("rebuilt ClassGroup cache in " + (System.currentTimeMillis() - start) + " msec");
            return _groupList;            
        }catch (Exception ex){
            log.error("could not rebuild cache", ex);
            return Collections.emptyList();
        }
    }    
    
    private List<VClassGroup> getGroups( VClassGroupDao vcgDao ,  boolean includeIndividualCount ){                    
        // Get all classgroups, each populated with a list of their member vclasses            
        List<VClassGroup> groups = 
            vcgDao.getPublicGroupsWithVClasses(ORDER_BY_DISPLAYRANK, INCLUDE_UNINSTANTIATED, includeIndividualCount); 

        // remove classes that have been configured to be hidden from search results
        vcgDao.removeClassesHiddenFromSearch(groups);
        
        return groups;
    }
    
    private List<VClassGroup> removeFilteredOutGroupsAndClasses(List<VClassGroup> unfilteredGroups, List<VClassGroup> filteredGroups) {
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
    
	private void requestStop() {
		if( _cacheRebuildThread != null ){
		    _cacheRebuildThread.kill();		
        	try {
        		_cacheRebuildThread.join();
        	} catch (InterruptedException e) {
        		log.warn("Waiting for the thread to die, but interrupted.", e);
        	}
		}
	}

    protected VClassGroupDao getVCGDao(){
        WebappDaoFactory wdf =(WebappDaoFactory)context.getAttribute("webappDaoFactory");
        if( wdf == null ){
            log.error("Cannot get webappDaoFactory from context");
            return null;
        }else
            return wdf.getVClassGroupDao();
    }
    
    /* ******************  Jena Model Change Listener***************************** */
    private class VClassGroupCacheChangeListener extends StatementListener {        
        public void addedStatement(Statement stmt) {
            checkAndDoUpdate(stmt); 
        }
        
        public void removedStatement(Statement stmt) {
            checkAndDoUpdate(stmt);     
        }

        private void checkAndDoUpdate(Statement stmt){
            if( stmt==null ) return;
            if( log.isDebugEnabled()){
                log.debug("subject: " + stmt.getSubject().getURI());
                log.debug("predicate: " + stmt.getPredicate().getURI());
            }
            if( RDF.type.getURI().equals( stmt.getPredicate().getURI())  ){
                requestCacheUpdate();
            } else if( VitroVocabulary.IN_CLASSGROUP.equals( stmt.getPredicate().getURI() )){
                requestCacheUpdate();
            }
        }
    }
    /* ******************** RebuildGroupCacheThread **************** */
    protected class RebuildGroupCacheThread extends Thread {
        private final VClassGroupCache cache;
        private final AtomicLong queueChangeMillis = new AtomicLong();
        private volatile boolean die = false;

        RebuildGroupCacheThread(VClassGroupCache cache) {
        	super("VClassGroupCache.RebuildGroupCacheThread");
            this.cache = cache;
        }
        
        public void run() {
			while (!die) {
				int delay;
				
				if (_rebuildRequested == Boolean.FALSE) {
					log.debug("rebuildGroupCacheThread.run() -- queue empty, sleep");
					delay = 1000 * 60;
				} else if ((System.currentTimeMillis() - queueChangeMillis.get()) < 200) {
					log.debug("rebuildGroupCacheThread.run() -- delay start of rebuild");
					delay = 200;
				} else {
					log.debug("rebuildGroupCacheThread.run() -- refreshGroupCache()");
					cache.rebuildCache();
					delay = 0;
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

        synchronized void informOfQueueChange(){
            queueChangeMillis.set(System.currentTimeMillis());
            this.notifyAll();
        }

        synchronized void kill(){
            die = true;
            this.notifyAll();
        }
    }

    /* ******************** ServletContextListener **************** */	
    public static class Setup implements ServletContextListener {
        @Override
        public void contextInitialized(ServletContextEvent sce) {        
            ServletContext servletContext = sce.getServletContext();
			servletContext.setAttribute(ATTRIBUTE_NAME,  new VClassGroupCache(servletContext) );
        }

        @Override
        public void contextDestroyed(ServletContextEvent sce) {
            Object o = sce.getServletContext().getAttribute(ATTRIBUTE_NAME);
        	if (o instanceof VClassGroupCache ) {
        		((VClassGroupCache) o).requestStop();
        	}
        }
    }
}
