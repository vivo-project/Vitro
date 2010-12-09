/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.WebappDaoFactoryFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilterUtils;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.flags.PortalFlag;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.web.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.VClassGroupTemplateModel;
import freemarker.template.Configuration;
import freemarker.template.SimpleSequence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BrowseController extends FreemarkerHttpServlet {
    static final long serialVersionUID=2006030721126L;

    private transient ConcurrentHashMap<Integer, List> _groupListMap
            = new ConcurrentHashMap<Integer, List>();
    private transient ConcurrentLinkedQueue<String> _rebuildQueue
            = new ConcurrentLinkedQueue<String>();
    private RebuildGroupCacheThread _cacheRebuildThread;

    private static final Log log = LogFactory.getLog(BrowseController.class);
    
    private static final String TEMPLATE_DEFAULT = "classGroups.ftl";

    public void init(javax.servlet.ServletConfig servletConfig)
            throws javax.servlet.ServletException {
        super.init(servletConfig);
        ServletContext sContext = servletConfig.getServletContext();

        BrowseControllerChangeListener bccl = new BrowseControllerChangeListener(this);
        ModelContext.getJenaOntModel(sContext).register(bccl);
        ModelContext.getBaseOntModel(sContext).register(bccl);
        ModelContext.getInferenceOntModel(sContext).register(bccl);
        ModelContext.getUnionOntModelSelector(sContext).getABoxModel().register(bccl);

        _rebuildQueue.add(REBUILD_EVERY_PORTAL);
        _cacheRebuildThread = new RebuildGroupCacheThread(this);
        _cacheRebuildThread.setDaemon(true);
        _cacheRebuildThread.start();
        _cacheRebuildThread.informOfQueueChange();
    }
     
    @Override
    protected String getTitle(String siteName) {
    	return "Index to " + siteName + " Contents";
    }

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {

        Map<String, Object> body = new HashMap<String, Object>();
        String message = null;
        String templateName = TEMPLATE_DEFAULT;
        
    	if( vreq.getParameter("clearcache") != null ) //mainly for debugging
    		clearGroupCache();

    	//PortalFlag portalState= vreq.getPortalFlag();

    	int portalId = vreq.getPortal().getPortalId();
    	List<VClassGroup> groups = getGroups(vreq.getWebappDaoFactory().getVClassGroupDao(), portalId);
    	_groupListMap.put(portalId, groups);
    	if (groups == null || groups.isEmpty()) {
    		message = "There are not yet any items in the system.";
    	}
    	else {  	    
    	    List<VClassGroupTemplateModel> vcgroups = new ArrayList<VClassGroupTemplateModel>(groups.size());   	    
    		for (VClassGroup group : groups) {
    		    vcgroups.add(new VClassGroupTemplateModel(group));
    		}
    		body.put("classGroups", vcgroups);
    	} 
    	
    	if (message != null) {
    	    body.put("message", message);
    	    templateName = Template.TITLED_MESSAGE.toString();
    	} 
    	
        return new TemplateResponseValues(templateName, body);
    }

    public void destroy(){
        _cacheRebuildThread.kill();
    }
    
    private List getGroups( VClassGroupDao vcgDao, int portalId) {
    	return getGroups( vcgDao, portalId, INCLUDE_INDIVIDUAL_COUNT);
    }

    private List getGroups( VClassGroupDao vcgDao, int portalId, boolean includeIndividualCount ){
        List grp = _groupListMap.get(portalId);
        if( grp == null ){
            log.debug("needed to build vclassGroups for portal " + portalId);
            // Get all classgroups, each populated with a list of their member vclasses
            List groups = vcgDao.getPublicGroupsWithVClasses(ORDER_BY_DISPLAYRANK, !INCLUDE_UNINSTANTIATED, includeIndividualCount); 

            // remove classes that have been configured to be hidden from search results
            vcgDao.removeClassesHiddenFromSearch(groups);
            
            // now cull out the groups with no populated classes            
            vcgDao.removeUnpopulatedGroups(groups);
            
            return groups;
        } else {
            return grp;
        }
    }   
    
    private static boolean ORDER_BY_DISPLAYRANK = true;
    private static boolean INCLUDE_UNINSTANTIATED = true;
    private static boolean INCLUDE_INDIVIDUAL_COUNT = true;

    void requestCacheUpdate(String portalUri){
        log.debug("requesting update for portal " + portalUri);
        _rebuildQueue.add(portalUri);
        _cacheRebuildThread.informOfQueueChange();
    }

    protected synchronized void refreshGroupCache() {
       long start = System.currentTimeMillis();
       try{
           boolean rebuildAll = false;
           HashSet<String> portalURIsToRebuild = new HashSet<String>();
           String portalUri;
           while ( null != (portalUri = _rebuildQueue.poll()) ){
               if( portalUri.equals(REBUILD_EVERY_PORTAL)){
                   rebuildAll = true;
                   _rebuildQueue.clear();
                   break;
               }else{
                   portalURIsToRebuild.add(portalUri);
               }
           }

           ServletContext sContext = getServletConfig().getServletContext();
           ApplicationBean appBean = new ApplicationBean();
           WebappDaoFactory wdFactory = (WebappDaoFactory)sContext.getAttribute("webappDaoFactory");
           if( wdFactory == null ){
               log.error("Unable to rebuild cache: could not get 'webappDaoFactory' from Servletcontext");
               return;
           }

           Collection<Portal> portals;
           if( rebuildAll ){
               portals = wdFactory.getPortalDao().getAllPortals();
           }   else {
               portals = new LinkedList<Portal>();
               for( String uri : portalURIsToRebuild){
                   Portal p =wdFactory.getPortalDao().getPortalByURI(uri);
                   if( p!= null)
                       portals.add(wdFactory.getPortalDao().getPortalByURI(uri));
               }
           }
           
           for(Portal portal : portals){
               rebuildCacheForPortal(portal,appBean,wdFactory);
           }
           log.info("rebuilt ClassGroup cache in " + (System.currentTimeMillis() - start) + " msec");
       }catch (Exception ex){
           log.error("could not rebuild cache", ex);
       }
    }

    protected synchronized void rebuildCacheForPortalUri(String uri){
        ServletContext sContext = getServletConfig().getServletContext();
        WebappDaoFactory wdFactory = (WebappDaoFactory)sContext.getAttribute("webappDaoFactory");
        if( wdFactory == null ){
            log.error("Unable to rebuild cache: could not get 'webappDaoFactory' from Servletcontext");
            return;
        }
        ApplicationBean appBean = new ApplicationBean();
        Portal portal = wdFactory.getPortalDao().getPortalByURI(uri);
        rebuildCacheForPortal(portal,appBean,wdFactory);
    }

    protected synchronized void rebuildCacheForPortal(Portal portal, ApplicationBean appBean, WebappDaoFactory wdFactory){
        VitroFilters vFilters = null;
        
        boolean singlePortalApplication = wdFactory.getPortalDao().getAllPortals().size() == 1;
        
        if ( singlePortalApplication ) {
        	if ( vFilters == null ) 
        		vFilters = VitroFilterUtils.getDisplayFilterByRoleLevel(RoleLevel.PUBLIC, wdFactory);
        } else if ( portal.isFlag1Filtering() ){
            PortalFlag pflag = new PortalFlag(portal.getPortalId());
            if( vFilters == null)
                vFilters = VitroFilterUtils.getFilterFromPortalFlag(pflag);
            else
                vFilters = vFilters.and( VitroFilterUtils.getFilterFromPortalFlag(pflag));
        }
        
        WebappDaoFactory filteringDaoFactory ;
        
        if( vFilters !=null ){
            filteringDaoFactory = new WebappDaoFactoryFiltering(wdFactory,vFilters);
        }else{
            filteringDaoFactory = wdFactory;
        }
        _groupListMap.remove(portal.getPortalId());
        if ( !singlePortalApplication ) {
	        _groupListMap.put(portal.getPortalId(), 
	        		getGroups(filteringDaoFactory.getVClassGroupDao(),portal.getPortalId()));
        } else {
        	List<VClassGroup> unfilteredGroups = getGroups(wdFactory.getVClassGroupDao(), portal.getPortalId(), INCLUDE_INDIVIDUAL_COUNT);
        	List<VClassGroup> filteredGroups = getGroups(filteringDaoFactory.getVClassGroupDao(),portal.getPortalId(), !INCLUDE_INDIVIDUAL_COUNT);
        	_groupListMap.put(portal.getPortalId(), removeFilteredOutGroupsAndClasses(unfilteredGroups, filteredGroups));
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
        }
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
    

    private void clearGroupCache(){
        _groupListMap = new ConcurrentHashMap<Integer, List>();
    }

    /* ******************  Jena Model Change Listener***************************** */
    private class BrowseControllerChangeListener extends StatementListener {
        private BrowseController controller = null;
        public BrowseControllerChangeListener(BrowseController controller){
            this.controller=controller;
        }

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
                requestCacheUpdate(REBUILD_EVERY_PORTAL);
            } else if( VitroVocabulary.PORTAL_FLAG1FILTERING.equals( stmt.getPredicate().getURI())){
                requestCacheUpdate(stmt.getSubject().getURI());
            } else if( VitroVocabulary.IN_CLASSGROUP.equals( stmt.getPredicate().getURI() )){
                requestCacheUpdate(REBUILD_EVERY_PORTAL);
            }
        }
    }
    /* ******************** RebuildGroupCacheThread **************** */
    protected class RebuildGroupCacheThread extends Thread {
        BrowseController controller;
        boolean die = false;
        boolean queueChange = false;
        long queueChangeMills = 0;
        private boolean awareOfQueueChange = false;

        RebuildGroupCacheThread(BrowseController controller) {
            this.controller = controller;
        }
        public void run() {
            while(true){
                try{
                    synchronized (this){
                        if( _rebuildQueue.isEmpty() ){
                             log.debug("rebuildGroupCacheThread.run() -- queye empty, sleep");
                             wait(1000 * 60 );
                        }
                        if( die ) {
                            log.debug("doing rebuildGroupCacheThread.run() -- die()");
                            return;
                        }
                        if( queueChange && !awareOfQueueChange){
                            log.debug("rebuildGroupCacheThread.run() -- awareOfQueueChange, delay start of rebuild");
                            awareOfQueueChange = true;
                            wait(200);
                        }
                    }

                    if( awareOfQueueChange && System.currentTimeMillis() - queueChangeMills > 200){
                        log.debug("rebuildGroupCacheThread.run() -- refreshGroupCache()");
                        controller.refreshGroupCache();
                        synchronized( this){
                            queueChange = false;
                        }
                        awareOfQueueChange = false;
                    }else {
                        synchronized( this ){
                            wait(200);
                        }
                    }
                }   catch(InterruptedException e){}
            }


        }

        synchronized void informOfQueueChange(){
            queueChange = true;
            queueChangeMills = System.currentTimeMillis();
            this.notifyAll();
        }

        synchronized void kill(){
            die = true;
            notifyAll();
        }
    }

    protected static String REBUILD_EVERY_PORTAL ="Rebuild every portal.";
}
