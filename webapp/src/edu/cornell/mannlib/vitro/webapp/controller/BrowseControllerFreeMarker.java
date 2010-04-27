/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.beans.display.VClassGroupDisplay;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.WebappDaoFactoryFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilterUtils;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;
import edu.cornell.mannlib.vitro.webapp.flags.PortalFlag;
import edu.cornell.mannlib.vitro.webapp.servlet.template.freemarker.FreeMarkerHttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.template.*;

import javax.servlet.ServletContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BrowseControllerFreeMarker extends FreeMarkerHttpServlet {
    static final long serialVersionUID=2006030721126L;

    private transient ConcurrentHashMap<Integer, List> _groupListMap
            = new ConcurrentHashMap<Integer, List>();
    private transient ConcurrentLinkedQueue<String> _rebuildQueue
            = new ConcurrentLinkedQueue<String>();
    private RebuildGroupCacheThread _cacheRebuildThread;

    private static final Log log = LogFactory.getLog(BrowseControllerFreeMarker.class.getName());

    public void init(javax.servlet.ServletConfig servletConfig)
            throws javax.servlet.ServletException {
        super.init(servletConfig);
        ServletContext sContext = servletConfig.getServletContext();

        //BJL23: I'll work on a strategy for avoiding all this craziness.
        OntModel model = (OntModel)sContext.getAttribute("jenaOntModel");
        OntModel baseModel = (OntModel)sContext.getAttribute("baseOntModel");
        OntModel infModel = (OntModel)sContext.getAttribute("inferenceOntModel");
	
        BrowseControllerChangeListener bccl = new BrowseControllerChangeListener(this);
        model.register(bccl);
        baseModel.register(bccl);
        infModel.register(bccl);

        _rebuildQueue.add(REBUILD_EVERY_PORTAL);
        _cacheRebuildThread = new RebuildGroupCacheThread(this);
        _cacheRebuildThread.setDaemon(true);
        _cacheRebuildThread.start();
        _cacheRebuildThread.informOfQueueChange();
    }
      
    protected String getTitle() {
    	return "Index to " + portal.getAppName() + " Contents";
    }

    protected String getBody() {

    	Map body = new HashMap();
    	
    	// Set main page template attributes specific to this page
    	root.put("contentClass", "siteMap");
        
    	if( vreq.getParameter("clearcache") != null ) //mainly for debugging
    		clearGroupCache();

    	//PortalFlag portalState= vreq.getPortalFlag();

    	String message = "";
    	List<VClassGroup> groups = getGroups(vreq.getWebappDaoFactory().getVClassGroupDao(), vreq.getPortal().getPortalId());

    	if (groups == null || groups.isEmpty()) {
    		message = "There are not yet any items in the system.";
    		body.put("message", message); 
    	}
    	else {
    		// Create a list of VClassGroupDisplay objects, each of which wraps a VClassGroup object.
    		// This allows EL to access VClassGroup properties like publicName, which it can't do
    		// if passed a linked list.
    	    
    	    // Don't need to explicitly create SimpleSequence object. FreeMarker will automatically wrap
    	    // the list in a SimpleSequence.
    		//SimpleSequence vcgroups = new SimpleSequence();
    	    List<VClassGroupDisplay> vcgroups = new ArrayList<VClassGroupDisplay>();
    		Iterator i = groups.iterator();
    		VClassGroup group;
    		VClassGroupDisplay displayGroup;
    		while (i.hasNext()) {
    			group = (VClassGroup) i.next();
    			displayGroup = new VClassGroupDisplay(group);
    			vcgroups.add(displayGroup);
    		}
    		body.put("classGroups", vcgroups);
    	}     
    	body.put("entityListUri", getUrl("/entitylistFM?vclassId="));

        String templateName = "browseGroup.ftl";       
        return mergeBodyToTemplate(templateName, body);
    }

    public void destroy(){
        _cacheRebuildThread.kill();
    }

    private List getGroups( VClassGroupDao vcgDao, int portalId ){
        List grp = _groupListMap.get(portalId);
        if( grp == null ){
            log.debug("needed to build vclassGroups for portal " + portalId);
            // Get all classgroups, each populated with a list of their member vclasses
            List groups = vcgDao.getPublicGroupsWithVClasses(ORDER_BY_DISPLAYRANK, !INCLUDE_UNINSTANTIATED); 

            // now cull out the groups with no populated classes
            //removeUnpopulatedClasses( groups);
            vcgDao.removeUnpopulatedGroups(groups);

            _groupListMap.put(portalId, groups);
            return groups;
        } else {
            return grp;
        }
    }
    
    private static boolean ORDER_BY_DISPLAYRANK = true;
    private static boolean INCLUDE_UNINSTANTIATED = true;

//  private void removeUnpopulatedClasses( List<VClassGroup> groups){
//          if( groups == null || groups.size() == 0 ) return;
//            for( VClassGroup grp : groups ){
//                ListIterator it = grp.listIterator();
//                while(it.hasNext()){
//                    VClass claz = (VClass)it.next();
//                    if( claz.getEntityCount() < 1 )
//                        it.remove();
//                }
//            }
//    }

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
        
        if( portal.isFlag1Filtering() ){
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
        getGroups(filteringDaoFactory.getVClassGroupDao(),portal.getPortalId());
    }

    private void clearGroupCache(){
        _groupListMap = new ConcurrentHashMap<Integer, List>();
    }

    /* ******************  Jena Model Change Listener***************************** */
    private class BrowseControllerChangeListener extends StatementListener {
        private BrowseControllerFreeMarker controller = null;
        public BrowseControllerChangeListener(BrowseControllerFreeMarker controller){
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
        BrowseControllerFreeMarker controller;
        boolean die = false;
        boolean queueChange = false;
        long queueChangeMills = 0;
        private boolean awareOfQueueChange = false;

        RebuildGroupCacheThread(BrowseControllerFreeMarker controller) {
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
